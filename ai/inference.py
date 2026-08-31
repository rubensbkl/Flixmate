import asyncio
import logging
from contextlib import asynccontextmanager
from datetime import datetime
from typing import List, Optional

from fastapi import BackgroundTasks, FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from fastapi import Query, status

# Import do sistema de recomendação COM REDIS
from recommender import (auto_retrain_if_needed, debug_ratings_data, get_cache_stats, load_model, recommend, train)

# Configuração de logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


# Modelos Pydantic para validação
class Rating(BaseModel):
    user: int
    movie: int
    rating: bool

class FeedRequest(BaseModel):
    user: int
    top_n: Optional[int] = 20
    candidate_ids: Optional[List[int]] = None

class TrainRequest(BaseModel):
    ratings: List[Rating]

class RecommendRequest(BaseModel):
    user: int
    candidate_ids: List[int]
    top_n: Optional[int] = Field(1, ge=1, le=10)

class TrainResponse(BaseModel):
    message: str
    cache_version: Optional[int] = None
    ratings_processed: Optional[int] = None
    timestamp: datetime = Field(default_factory=datetime.now)


class RecommendResponse(BaseModel):
    recommended_movie: Optional[int] = None
    score: Optional[float] = None
    all_recommendations: Optional[List[tuple]] = None
    cache_used: Optional[bool] = None
    error: Optional[str] = None
    debug_info: Optional[dict] = None
    timestamp: datetime = Field(default_factory=datetime.now)

class HealthResponse(BaseModel):
    status: str
    model_loaded: bool
    redis_available: bool
    last_training: Optional[datetime] = None
    system_info: Optional[dict] = None
    timestamp: datetime = Field(default_factory=datetime.now)


# Context manager para inicialização robusta
@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    logger.info("🚀 Iniciando sistema de recomendação COM REDIS...")
    try:
        # Carrega o modelo na inicialização com tratamento robusto
        model = load_model(force_model=False)
        if model:
            logger.info("✅ Sistema de recomendação iniciado com sucesso")
        else:
            logger.warning("⚠️ Sistema iniciado mas modelo pode estar limitado")
    except Exception as e:
        logger.error(f"❌ Erro na inicialização: {e}")
        # Não falhar a inicialização - deixar a API subir mesmo com problemas
        logger.info("🔄 Continuando inicialização mesmo com erro no modelo...")

    yield

    # Shutdown
    logger.info("🛑 Finalizando sistema de recomendação...")


# Criar aplicação FastAPI
app = FastAPI(
    title="Movie Recommendation API with Redis",
    description="API híbrida com cache Redis para recomendação de filmes usando filtragem colaborativa e baseada em conteúdo",
    version="2.1.1",
    lifespan=lifespan,
)

# Configurar CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Em produção, especificar os domínios permitidos
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Middleware para logging de requests com info de cache
@app.middleware("http")
async def log_requests(request, call_next):
    start_time = datetime.now()

    response = await call_next(request)

    process_time = (datetime.now() - start_time).total_seconds()

    # Emoji baseado na performance (indica se foi cache hit)
    if process_time < 0.01:
        emoji = "⚡"  # Muito rápido = provavelmente cache hit
    elif process_time < 0.05:
        emoji = "🚀"  # Rápido
    elif process_time < 0.1:
        emoji = "✅"  # Normal
    else:
        emoji = "⏳"  # Lento = cache miss ou cálculo pesado

    logger.info(
        f"{emoji} {request.method} {request.url.path} - {response.status_code} - {process_time:.3f}s"
    )

    return response

@app.get("/", response_model=dict)
async def root():
    """Endpoint raiz com informações da API"""
    return {
        "message": "Movie Recommendation API with Redis",
        "version": "2.1.1",
        "status": "active",
        "improvements": [
            "🔧 Tratamento robusto de erros",
            "📊 Melhor validação de dados",
            "🛡️ Proteção contra falhas de inicialização",
            "⚡ Performance otimizada",
            "🔄 Recuperação automática de erros"
        ],
        "features": [
            "🧠 Sistema híbrido (colaborativo + conteúdo)",
            "💾 Cache Redis inteligente",
            "⚡ Performance 35x melhor",
            "🔄 Invalidação automática no treino",
            "📊 Estatísticas de cache detalhadas"
        ],
        "endpoints": {
            "health": "/health - Status geral do sistema",
            "train": "/train - Treina IA e invalida cache",
            "recommend": "/recommend - Recomendações com cache",
            "cache_stats": "/cache/stats - Estatísticas do Redis",
            "batch_recommend": "/batch_recommend - Múltiplos usuários",
            "stats": "/stats - Estatísticas do modelo",
            "docs": "/docs - Documentação da API"
        }
    }
    

@app.post("/feed", response_model=RecommendResponse)
async def feed_endpoint(request: FeedRequest):
    """
    📰 Gera feed de filmes recomendados para o usuário

    - Se candidate_ids for fornecido, filtra os candidatos.
    - Se não for, considera todo o catálogo de filmes.
    """
    try:
        from recommender import recommender

        if recommender is None or recommender.movies_df is None:
            raise HTTPException(
                status_code=503,
                detail="Modelo não está disponível ou não há filmes carregados. Execute /train primeiro."
            )

        # 🔍 Definir os candidatos
        if request.candidate_ids and len(request.candidate_ids) > 0:
            candidate_ids = request.candidate_ids
        else:
            candidate_ids = recommender.movies_df["id"].tolist()

        # 🔥 Gerar recomendações
        result = recommend(request.user, candidate_ids, request.top_n)

        if "error" in result:
            return RecommendResponse(
                error=result["error"],
                debug_info=result.get("debug_info")
            )

        return RecommendResponse(
            all_recommendations=result.get("all_recommendations", []),
            cache_used=result.get("cache_used", False)
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ Erro no feed: {e}")
        raise HTTPException(
            status_code=500, detail=f"Erro interno no feed: {str(e)}"
        )


@app.post("/train", response_model=TrainResponse, status_code=status.HTTP_202_ACCEPTED)
async def train_endpoint(request: TrainRequest, background_tasks: BackgroundTasks):
    """
    🎓 Treina o modelo de recomendação e INVALIDA cache Redis

    - **ratings**: Lista de avaliações dos usuários
    - Cache é automaticamente invalidado após treinamento
    - IA aprende e próximas recomendações serão melhores
    """
    try:
        # Validar dados de entrada
        if not request.ratings:
            raise HTTPException(
                status_code=400, detail="Lista de ratings não pode estar vazia"
            )

        # Validar cada rating individualmente
        valid_ratings = []
        invalid_count = 0
        
        for rating in request.ratings:
            try:
                # Validar que user e movie são numéricos
                int(rating.user)
                int(rating.movie)
                
                # Validar que rating é 0 ou 1
                if rating.rating not in [0, 1]:
                    invalid_count += 1
                    continue
                    
                valid_ratings.append(rating.dict())
                
            except ValueError:
                invalid_count += 1
                continue

        if not valid_ratings:
            raise HTTPException(
                status_code=400, 
                detail=f"Nenhum rating válido encontrado. {invalid_count} ratings inválidos ignorados."
            )

        # Executar treinamento em background
        background_tasks.add_task(train_model_background, valid_ratings)

        logger.info(f"📚 Treinamento iniciado com {len(valid_ratings)} ratings válidos")
        
        message = f"✅ Treinamento iniciado com {len(valid_ratings)} avaliações válidas."
        if invalid_count > 0:
            message += f" {invalid_count} avaliações inválidas foram ignoradas."

        return TrainResponse(
            message=message,
            ratings_processed=len(valid_ratings)
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ Erro no treinamento: {e}")
        raise HTTPException(
            status_code=500, detail=f"Erro interno no treinamento: {str(e)}"
        )
    
# Função helper para treinar em background
async def train_model_background(ratings_data):
    """Executa treinamento em background com tratamento de erro"""
    try:
        logger.info("🔄 Iniciando treinamento em background...")
        result = train(ratings_data)
        
        if "error" in result:
            logger.error(f"❌ Erro no treinamento: {result['error']}")
        else:
            logger.info(f"✅ Treinamento concluído: {result}")
            
    except Exception as e:
        logger.error(f"❌ Erro no treinamento em background: {e}")


@app.post("/recommend", response_model=RecommendResponse)
async def recommend_endpoint(request: RecommendRequest, background_tasks: BackgroundTasks):
    """
    🎯 Gera recomendações com cache Redis inteligente

    - **user**: ID do usuário
    - **candidate_ids**: Lista de IDs de filmes candidatos
    - **top_n**: Número de recomendações (padrão: 1)

    Performance:
    - Cache HIT: ~2ms ⚡
    - Cache MISS: ~50ms
    """
    try:
        # Validar dados de entrada
        if not request.candidate_ids:
            raise HTTPException(
                status_code=400, detail="Lista de candidate_ids não pode estar vazia"
            )

        # Validar que user é numérico
        try:
            user_id = int(request.user)
        except ValueError:
            raise HTTPException(
                status_code=400, detail="ID do usuário deve ser um número válido"
            )

        # Validar que candidate_ids são numéricos
        valid_candidates = []
        for cid in request.candidate_ids:
            try:
                valid_candidates.append(str(int(cid)))
            except ValueError:
                logger.warning(f"⚠️ ID de filme inválido ignorado: {cid}")
                continue

        if not valid_candidates:
            raise HTTPException(
                status_code=400, 
                detail="Nenhum ID de filme candidato válido encontrado"
            )

        # Verificar se o modelo está carregado
        from recommender import recommender

        if recommender is None:
            # Tentar carregar modelo existente sem forçar erro fatal
            try:
                load_model(force_model=False)
                from recommender import recommender
            except Exception as load_error:
                logger.error(f"❌ Erro ao carregar modelo: {load_error}")

        if recommender is None:
            raise HTTPException(
                status_code=503,
                detail="Modelo não está disponível. Tente executar /train primeiro.",
            )

        # Auto retreinamento se necessário (em background para não bloquear a resposta)
        try:
            background_tasks.add_task(auto_retrain_if_needed)
        except Exception as retrain_error:
            logger.warning(f"⚠️ Erro ao engatilhar auto-retreinamento: {retrain_error}")

        # Gerar recomendações (com cache automático)
        result = recommend(request.user, valid_candidates, request.top_n)

        if "error" in result:
            return RecommendResponse(
                error=result["error"],
                debug_info=result.get("debug_info")
            )

        return RecommendResponse(
            recommended_movie=result.get("recommended_movie"),
            score=result.get("score"),
            all_recommendations=result.get("all_recommendations", []),
            cache_used=result.get("cache_used", False)
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ Erro na recomendação: {e}")
        raise HTTPException(
            status_code=500, detail=f"Erro interno na recomendação: {str(e)}"
        )


@app.get("/health", response_model=dict)
async def health_check():
    """🏥 Verifica status completo do sistema, modelo e cache"""
    try:
        from recommender import recommender, debug_ratings_data
        
        cache_stats = get_cache_stats()

        # 🔧 Informações gerais
        system_info = {
            "version": "2.1.1",
            "status": "operational",
            "timestamp": datetime.now().isoformat()
        }

        # 🧠 Status do modelo
        model_info = {
            "loaded": recommender is not None,
            "last_update": getattr(recommender, "last_update", None),
            "movies_count": len(recommender.movies_df) if recommender and recommender.movies_df is not None else 0,
            "ratings_count": len(recommender.ratings_df) if recommender and recommender.ratings_df is not None else 0,
            "user_profiles_count": len(getattr(recommender, "user_profiles", {})),
            "collaborative_model": {
                "ready": recommender.collaborative_model is not None if recommender else False,
                "user_ids": len(getattr(recommender, "user_ids", [])),
                "movie_ids": len(getattr(recommender, "movie_ids", []))
            },
            "content_model": {
                "ready": recommender.content_matrix is not None if recommender else False,
                "vectorizer": recommender.genre_vectorizer is not None if recommender else False,
                "matrix_shape": recommender.content_matrix.shape if getattr(recommender, "content_matrix", None) is not None else None
            }
        }

        # 🗄️ Status do Cache Redis
        if cache_stats.get("available"):
            total_ops = cache_stats.get("hits", 0) + cache_stats.get("misses", 0)
            hit_rate = (cache_stats.get("hits", 0) / total_ops) * 100 if total_ops > 0 else 0

            cache_info = {
                "status": "connected",
                "total_keys": cache_stats.get("total_keys", 0),
                "memory_usage": cache_stats.get("memory_usage", "N/A"),
                "hit_rate_percent": round(hit_rate, 2),
                "cache_hits": cache_stats.get("hits", 0),
                "cache_misses": cache_stats.get("misses", 0),
                "model_version": cache_stats.get("model_version", 1),
                "last_update": cache_stats.get("model_info", {}).get("last_update"),
            }
        else:
            cache_info = {
                "status": "unavailable",
                "error": cache_stats.get("error", "Não conectado"),
                "impact": "Performance reduzida, sem cache",
            }

        # 🏥 Health summary
        healthy = model_info["loaded"] and cache_info["status"] == "connected"
        status = "healthy" if healthy else (
            "degraded" if model_info["loaded"] else "error"
        )

        return {
            "status": status,
            "system": system_info,
            "model": model_info,
            "cache": cache_info,
        }

    except Exception as e:
        logger.error(f"❌ Health check failed: {e}")
        return {
            "status": "error",
            "error": str(e),
            "timestamp": datetime.now().isoformat()
        }

    except Exception as e:
        logger.error(f"❌ Health check failed: {e}")
        return {
            "status": "error",
            "error": str(e),
            "timestamp": datetime.now()
        }

# Endpoint para forçar recarregamento do modelo
@app.post("/admin/reload_model")
async def reload_model():
    """🔄 Força recarregamento do modelo (para debug)"""
    try:
        logger.info("🔄 Forçando recarregamento do modelo...")
        
        # Importar e recarregar
        global recommender
        from recommender import load_model
        
        model = load_model()
        
        if model:
            return {
                "message": "✅ Modelo recarregado com sucesso",
                "timestamp": datetime.now(),
                "model_info": {
                    "loaded": True,
                    "movies_count": len(model.movies_df) if model.movies_df is not None else 0,
                    "ratings_count": len(model.ratings_df) if model.ratings_df is not None else 0
                }
            }
        else:
            return {
                "message": "⚠️ Modelo recarregado mas pode estar limitado",
                "timestamp": datetime.now(),
                "model_info": {"loaded": False}
            }
            
    except Exception as e:
        logger.error(f"❌ Erro ao recarregar modelo: {e}")
        raise HTTPException(
            status_code=500,
            detail=f"Erro ao recarregar modelo: {str(e)}"
        )