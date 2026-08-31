import hashlib
import json
import logging
import os
import pickle
import threading
import warnings
from datetime import datetime, timedelta

warnings.filterwarnings('ignore', message='.*Downcasting object dtype arrays.*')

import numpy as np
import pandas as pd
from scipy.sparse import csr_matrix
from sklearn.decomposition import TruncatedSVD
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sqlalchemy import create_engine

# Redis import with fallback
try:
    import redis
    REDIS_AVAILABLE = True
    REDIS_HOST = os.getenv("REDIS_HOST", "redis")
    REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))
    redis_client = redis.Redis(
        host=REDIS_HOST, 
        port=REDIS_PORT, 
        db=0, 
        decode_responses=True,
        socket_connect_timeout=5,
        socket_timeout=5
    )
    # Testar conexão
    redis_client.ping()
    logging.info("✅ Redis conectado com sucesso")
except (ImportError, redis.ConnectionError, Exception) as e:
    REDIS_AVAILABLE = False
    redis_client = None
    logging.warning(f"⚠️ Redis não disponível: {e}")

# Configuração do banco
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = os.getenv("DB_PORT", "5432")
DB_NAME = os.getenv("DB_NAME", "cinematch")
DB_USER = os.getenv("DB_USER", "cinematch")
DB_PASSWORD = os.getenv("DB_PASSWORD", "cinematch")

# Configuração de logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Diretório e caminhos do modelo
MODEL_DIR = os.getenv("MODEL_DIR", "./model_data")
os.makedirs(MODEL_DIR, exist_ok=True)
MODEL_PATH = f"{MODEL_DIR}/hybrid_model.pkl"

model_lock = threading.Lock()

class RedisCache:
    """Gerenciador de cache Redis com fallback"""
    
    def __init__(self):
        self.available = REDIS_AVAILABLE
        self.client = redis_client
        self.model_version = 1
    
    def _get_key(self, prefix, *args):
        """Gera chave de cache"""
        key_content = f"{prefix}:{':'.join(map(str, args))}:v{self.model_version}"
        return hashlib.md5(key_content.encode()).hexdigest()[:16]
    
    def get(self, prefix, *args, default=None):
        """Busca valor do cache"""
        if not self.available:
            return default
        
        try:
            key = self._get_key(prefix, *args)
            value = self.client.get(key)
            if value:
                return json.loads(value)
        except Exception as e:
            logger.warning(f"⚠️ Erro ao buscar cache: {e}")
        
        return default
    
    def set(self, prefix, *args, value=None, ttl=600):
        """Salva valor no cache"""
        if not self.available:
            return False
        
        try:
            key = self._get_key(prefix, *args)
            self.client.setex(key, ttl, json.dumps(value))
            return True
        except Exception as e:
            logger.warning(f"⚠️ Erro ao salvar cache: {e}")
            return False
    
    def delete_pattern(self, pattern):
        """Remove chaves por padrão"""
        if not self.available:
            return 0
        
        try:
            keys = self.client.keys(f"*{pattern}*")
            if keys:
                return self.client.delete(*keys)
        except Exception as e:
            logger.warning(f"⚠️ Erro ao limpar cache: {e}")
        
        return 0
    
    def increment_version(self):
        """Incrementa versão do modelo (invalida cache)"""
        self.model_version += 1
        logger.info(f"🔄 Cache invalidado - Nova versão: {self.model_version}")
        
        # Limpar cache antigo
        if self.available:
            try:
                cursor = '0'
                deleted = 0
                while cursor != 0:
                    cursor, keys = self.client.scan(cursor=cursor, match='*:v*', count=500)
                    if keys:
                        deleted += self.client.delete(*keys)
                logger.info(f"🗑️ Removidas {deleted} chaves antigas")
            except Exception as e:
                logger.warning(f"⚠️ Erro ao limpar cache antigo: {e}")
    
    def get_stats(self):
        """Estatísticas do Redis"""
        if not self.available:
            return {"available": False}
        
        try:
            info = self.client.info()
            cache_keys = self.client.keys("*")
            
            return {
                "available": True,
                "total_keys": len(cache_keys),
                "memory_usage": info.get('used_memory_human', 'N/A'),
                "connected_clients": info.get('connected_clients', 0),
                "model_version": self.model_version,
                "hits": info.get('keyspace_hits', 0),
                "misses": info.get('keyspace_misses', 0)
            }
        except Exception as e:
            return {"available": False, "error": str(e)}

# Instância global do cache
cache = RedisCache()

class HybridRecommender:
    def __init__(self):
        self.collaborative_model = None
        self.content_matrix = None
        self.user_item_matrix = None
        self.user_profiles = {}
        self.genre_vectorizer = None
        self.last_update = None
        self.movies_df = None
        self.ratings_df = None
        self.user_genres_df = None

    def load_data_from_db(self):
        """Carrega dados do banco com cache Redis"""
        logger.info("🔄 Carregando dados do banco...")

        # Tentar cache primeiro
        cached_data = cache.get("db_data", "all")
        if cached_data:
            logger.info("💾 Dados carregados do cache Redis")
            self.movies_df = pd.DataFrame(cached_data['movies'])
            self.ratings_df = pd.DataFrame(cached_data['ratings'])
            self.user_genres_df = pd.DataFrame(cached_data['user_genres'])
            
            # Garantir que as colunas existem e têm tipos corretos
            self._ensure_data_integrity()
            return

        # Se não tem cache, carregar do banco
        try:
            engine = create_engine(
                f"postgresql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
            )

            # Queries otimizadas com tratamento de dados ausentes
            movies_query = """
                SELECT 
                    m.id,
                    COALESCE(m.title, 'Título não disponível') as title,
                    COALESCE(m.overview, '') as overview,
                    COALESCE(m.rating, 0) as rating,
                    COALESCE(m.popularity, 0) as popularity,
                    m.release_date,
                    COALESCE(m.original_language, 'en') as original_language,
                    COALESCE(STRING_AGG(g.name, '|'), '') as genres
                FROM movies m
                LEFT JOIN movie_genres mg ON m.id = mg.movie_id
                LEFT JOIN genres g ON mg.genre_id = g.id
                GROUP BY m.id, m.title, m.overview, m.rating, m.popularity, m.release_date, m.original_language
                ORDER BY m.id
            """

            feedbacks_query = """
                SELECT 
                    user_id,
                    movie_id,
                    CASE WHEN feedback = true THEN 1 ELSE 0 END as rating
                FROM feedbacks
                WHERE user_id IS NOT NULL AND movie_id IS NOT NULL
            """

            user_genres_query = """
                SELECT 
                    ug.user_id,
                    COALESCE(STRING_AGG(g.name, '|'), '') as preferred_genres
                FROM user_genres ug
                JOIN genres g ON ug.genre_id = g.id
                WHERE ug.user_id IS NOT NULL
                GROUP BY ug.user_id
            """

            # Carregar dados com tratamento de erro
            self.movies_df = pd.read_sql(movies_query, engine)
            logger.info(f"📽️ Carregados {len(self.movies_df)} filmes")
            
            try:
                self.ratings_df = pd.read_sql(feedbacks_query, engine)
                logger.info(f"⭐ Carregados {len(self.ratings_df)} feedbacks")
            except Exception as e:
                logger.warning(f"⚠️ Erro ao carregar feedbacks: {e}")
                self.ratings_df = pd.DataFrame(columns=['user_id', 'movie_id', 'rating'])
            
            try:
                self.user_genres_df = pd.read_sql(user_genres_query, engine)
                logger.info(f"👤 Carregados {len(self.user_genres_df)} perfis de usuário")
            except Exception as e:
                logger.warning(f"⚠️ Erro ao carregar perfis de usuário: {e}")
                self.user_genres_df = pd.DataFrame(columns=['user_id', 'preferred_genres'])

            # Garantir integridade dos dados
            self._ensure_data_integrity()

            # Cachear dados por 30 minutos
            cache_data = {
                'movies': self.movies_df.to_dict('records'),
                'ratings': self.ratings_df.to_dict('records'),
                'user_genres': self.user_genres_df.to_dict('records')
            }
            cache.set("db_data", "all", value=cache_data, ttl=1800)

            logger.info("✅ Dados carregados e cacheados com sucesso")

        except Exception as e:
            logger.error(f"❌ Erro ao carregar dados do banco: {e}")
            # Criar DataFrames vazios como fallback
            self._create_empty_dataframes()

    def _ensure_data_integrity(self):
        """Garante que os dados têm a estrutura correta"""
        
        # Verificar e corrigir movies_df
        if self.movies_df is not None and len(self.movies_df) > 0:
            required_movie_cols = ['id', 'title', 'overview', 'rating', 'popularity', 'original_language', 'genres']
            for col in required_movie_cols:
                if col not in self.movies_df.columns:
                    if col == 'genres':
                        self.movies_df[col] = ''
                    elif col in ['rating', 'popularity']:
                        self.movies_df[col] = 0
                    else:
                        self.movies_df[col] = 'Unknown'
            
            # Garantir que genres não é None
            self.movies_df['genres'] = self.movies_df['genres'].fillna('')
            
        # Verificar e corrigir ratings_df
        if self.ratings_df is not None and len(self.ratings_df) > 0:
            required_rating_cols = ['user_id', 'movie_id', 'rating']
            for col in required_rating_cols:
                if col not in self.ratings_df.columns:
                    self.ratings_df[col] = 0
        
        # Verificar e corrigir user_genres_df  
        if self.user_genres_df is not None and len(self.user_genres_df) > 0:
            required_user_cols = ['user_id', 'preferred_genres']
            for col in required_user_cols:
                if col not in self.user_genres_df.columns:
                    if col == 'preferred_genres':
                        self.user_genres_df[col] = ''
                    else:
                        self.user_genres_df[col] = 0
            
            # Garantir que preferred_genres não é None
            self.user_genres_df['preferred_genres'] = self.user_genres_df['preferred_genres'].fillna('')

    def _create_empty_dataframes(self):
        """Cria DataFrames vazios como fallback"""
        self.movies_df = pd.DataFrame(columns=[
            'id', 'title', 'overview', 'rating', 'popularity', 'release_date', 'original_language', 'genres'
        ])
        self.ratings_df = pd.DataFrame(columns=['user_id', 'movie_id', 'rating'])
        self.user_genres_df = pd.DataFrame(columns=['user_id', 'preferred_genres'])
        logger.warning("⚠️ Criados DataFrames vazios como fallback")

    def prepare_content_features(self):
        """Prepara features de conteúdo com cache e tratamento robusto"""
        logger.info("🎭 Preparando features de conteúdo...")

        if self.movies_df is None or len(self.movies_df) == 0:
            logger.warning("⚠️ Nenhum filme disponível para features de conteúdo")
            self.content_matrix = None
            return

        try:
            # Verificar cache da matriz de conteúdo
            cached_matrix = cache.get("content_matrix", "tfidf")
            if cached_matrix:
                logger.info("💾 Matriz TF-IDF carregada do cache")
                self.content_matrix = csr_matrix(
                    (cached_matrix["data"], cached_matrix["indices"], cached_matrix["indptr"]),
                    shape=cached_matrix["shape"]
                )
                self.genre_vectorizer = None
                return

            # Garantir que as colunas existem
            if 'genres' not in self.movies_df.columns:
                self.movies_df['genres'] = ''
            if 'overview' not in self.movies_df.columns:
                self.movies_df['overview'] = ''
            if 'original_language' not in self.movies_df.columns:
                self.movies_df['original_language'] = 'en'

            # Criar features combinadas com tratamento seguro
            self.movies_df["content_features"] = (
                self.movies_df["genres"].fillna("").astype(str)
                + " "
                + self.movies_df["overview"].fillna("").astype(str).str[:200]
                + " "
                + self.movies_df["original_language"].fillna("en").astype(str)
            )

            # Verificar se há conteúdo válido
            valid_content = self.movies_df["content_features"].str.strip()
            if valid_content.str.len().sum() == 0:
                logger.warning("⚠️ Nenhum conteúdo válido encontrado para TF-IDF")
                self.content_matrix = None
                return

            # TF-IDF otimizado com tratamento de erro
            tfidf = TfidfVectorizer(
                max_features=500,
                stop_words="english",
                ngram_range=(1, 1),
                max_df=0.95,
                min_df=1,  # Reduzido para lidar com poucos dados
                lowercase=True,
                strip_accents='unicode'
            )

            content_matrix = tfidf.fit_transform(self.movies_df["content_features"])
            self.content_matrix = content_matrix
            self.genre_vectorizer = tfidf
            
            # Salvar no cache
            cache_data = {
                "data": content_matrix.data.tolist(),
                "indices": content_matrix.indices.tolist(),
                "indptr": content_matrix.indptr.tolist(),
                "shape": content_matrix.shape
            }
            cache.set("content_matrix", "tfidf", value=cache_data, ttl=3600)
            
            logger.info(f"📊 Matriz de conteúdo criada: {content_matrix.shape}")
            logger.info("✅ Features de conteúdo preparadas com sucesso")

        except Exception as e:
            logger.error(f"❌ Erro ao processar features de conteúdo: {e}")
            self.content_matrix = None
            self.genre_vectorizer = None

    def prepare_collaborative_model(self):
        """Prepara modelo colaborativo com cache e tratamento robusto - VERSÃO CORRIGIDA"""
        logger.info("👥 Preparando modelo colaborativo...")

        if self.ratings_df is None or len(self.ratings_df) == 0:
            logger.warning("⚠️ Sem dados para filtragem colaborativa")
            self.collaborative_model = None
            return

        try:
            # Log para debug - verificar dados de entrada
            logger.info(f"📊 Dados disponíveis: {len(self.ratings_df)} ratings")
            logger.info(f"👥 Usuários únicos: {self.ratings_df['user_id'].nunique()}")
            logger.info(f"🎬 Filmes únicos: {self.ratings_df['movie_id'].nunique()}")
            
            # Verificar cache do modelo colaborativo
            cached_model = cache.get("collaborative", "model")
            if cached_model:
                logger.info("💾 Modelo colaborativo carregado do cache")
                # Reconstruir modelo a partir do cache se necessário
                try:
                    self.user_factors = np.array(cached_model['user_factors'])
                    self.item_factors = np.array(cached_model['item_factors'])
                    self.user_ids = cached_model['user_ids']
                    self.movie_ids = cached_model['movie_ids']
                    
                    # Criar modelo dummy para compatibilidade
                    self.collaborative_model = TruncatedSVD(n_components=len(cached_model['user_factors'][0]))
                    logger.info(f"✅ Modelo colaborativo restaurado do cache")
                    return
                except Exception as cache_error:
                    logger.warning(f"⚠️ Erro ao restaurar cache: {cache_error}")
                    # Continuar para criar novo modelo

            # Criar matriz usuário-item otimizada com CSR direto sem pivot
            users = self.ratings_df["user_id"].astype("category")
            movies = self.ratings_df["movie_id"].astype("category")
            
            self.user_ids = users.cat.categories.tolist()
            self.movie_ids = movies.cat.categories.tolist()
            
            user_indices = users.cat.codes.values
            movie_indices = movies.cat.codes.values
            ratings = self.ratings_df["rating"].values
            
            self.user_item_matrix = csr_matrix((ratings, (user_indices, movie_indices)),
                                             shape=(len(self.user_ids), len(self.movie_ids)))
            
            # Debug da matriz criada
            logger.info(f"📐 Matriz usuário-item: {self.user_item_matrix.shape}")
            non_zero_entries = self.user_item_matrix.nnz
            logger.info(f"📊 Valores não-zero: {non_zero_entries}")

            # SVD com condições mais permissivas
            min_shape = min(self.user_item_matrix.shape)
            
            logger.info(f"🔍 Min shape: {min_shape}")
            logger.info(f"🔍 Entradas não-zero: {non_zero_entries}")
            
            # Condições mais realistas para criar o modelo
            if min_shape >= 3 and non_zero_entries >= 5:  # Reduzido de 5 para 3
                # Usar no máximo min_shape-1 componentes, mas pelo menos 2
                n_components = min(min(20, min_shape - 1), non_zero_entries // 2)
                n_components = max(2, n_components)  # Mínimo de 2 componentes
                
                logger.info(f"🎯 Criando modelo SVD com {n_components} componentes")
                
                self.collaborative_model = TruncatedSVD(
                    n_components=n_components,
                    random_state=42,
                    algorithm='randomized'  # Mais estável para matrizes pequenas
                )
                
                self.user_factors = self.collaborative_model.fit_transform(
                    self.user_item_matrix
                )
                self.item_factors = self.collaborative_model.components_.T

                # Cachear fatores do modelo por 1 hora
                model_cache = {
                    'user_factors': self.user_factors.tolist(),
                    'item_factors': self.item_factors.tolist(),
                    'user_ids': self.user_ids,
                    'movie_ids': self.movie_ids,
                    'n_components': n_components
                }
                cache.set("collaborative", "model", value=model_cache, ttl=3600)

                logger.info(f"✅ Modelo colaborativo preparado com {n_components} componentes")
                logger.info(f"📊 Variância explicada: {self.collaborative_model.explained_variance_ratio_.sum():.2%}")
                
            else:
                logger.warning(f"⚠️ Insuficientes dados para modelo colaborativo robusto")
                logger.warning(f"   - Min shape: {min_shape} (necessário >= 3)")
                logger.warning(f"   - Entradas não-zero: {non_zero_entries} (necessário >= 5)")
                logger.warning(f"   - Usando apenas recomendações baseadas em conteúdo")
                self.collaborative_model = None

        except Exception as e:
            logger.error(f"❌ Erro ao preparar modelo colaborativo: {e}")
            logger.error(f"   - Tipo do erro: {type(e).__name__}")
            logger.error(f"   - Detalhes: {str(e)}")
            self.collaborative_model = None

    
    def create_user_profile(self, user_id):
        """Cria perfil do usuário com cache Redis"""
        
        # Verificar cache do perfil
        cached_profile = cache.get("profile", user_id)
        if cached_profile:
            logger.debug(f"💾 Perfil do usuário {user_id} carregado do cache")
            return cached_profile

        if user_id in self.user_profiles:
            return self.user_profiles[user_id]

        profile = {
            "preferred_genres": [],
            "liked_movies": [],
            "disliked_movies": [],
            "avg_rating": 0.5,
            "genre_weights": {},
        }

        try:
            # Gêneros preferidos declarados
            if self.user_genres_df is not None and len(self.user_genres_df) > 0:
                user_genre_prefs = self.user_genres_df[
                    self.user_genres_df["user_id"] == user_id
                ]
                if not user_genre_prefs.empty:
                    genres_str = user_genre_prefs.iloc[0]["preferred_genres"]
                    if genres_str and genres_str.strip():
                        profile["preferred_genres"] = genres_str.split("|")

            # Filmes curtidos/não curtidos
            if self.ratings_df is not None and len(self.ratings_df) > 0:
                user_ratings = self.ratings_df[self.ratings_df["user_id"] == user_id]
                if not user_ratings.empty:
                    profile["liked_movies"] = user_ratings[user_ratings["rating"] == 1][
                        "movie_id"
                    ].tolist()
                    profile["disliked_movies"] = user_ratings[user_ratings["rating"] == 0][
                        "movie_id"
                    ].tolist()
                    profile["avg_rating"] = user_ratings["rating"].mean()

                    # Calcular pesos dos gêneros apenas se há filmes e dados de filmes
                    if profile["liked_movies"] and self.movies_df is not None and len(self.movies_df) > 0:
                        liked_movies_data = self.movies_df[
                            self.movies_df["id"].isin(profile["liked_movies"])
                        ]
                        genre_counts = {}

                        for _, movie in liked_movies_data.iterrows():
                            genres_str = movie.get("genres", "")
                            if genres_str and genres_str.strip():
                                for genre in genres_str.split("|"):
                                    if genre.strip():
                                        genre_counts[genre.strip()] = genre_counts.get(genre.strip(), 0) + 1

                        total_likes = len(profile["liked_movies"])
                        if total_likes > 0 and genre_counts:
                            profile["genre_weights"] = {
                                g: c / total_likes for g, c in genre_counts.items()
                            }

        except Exception as e:
            logger.warning(f"⚠️ Erro ao criar perfil do usuário {user_id}: {e}")

        # Cachear perfil por 1 hora
        cache.set("profile", user_id, value=profile, ttl=3600)
        self.user_profiles[user_id] = profile
        return profile

    def get_content_similarity(self, movie_idx1, movie_idx2):
        if self.content_matrix is None:
            return 0.0

        try:
            vec1 = self.content_matrix[movie_idx1]
            vec2 = self.content_matrix[movie_idx2]
            similarity = cosine_similarity(vec1, vec2)[0][0]
            return similarity
        except Exception as e:
            logger.warning(f"⚠️ Erro ao calcular similaridade: {e}")
            return 0.0


    def get_content_recommendations(self, user_id, candidate_movies, top_n=10):
        """Recomendações baseadas em conteúdo com tratamento robusto"""
        profile = self.create_user_profile(user_id)
        scores = {}

        if self.movies_df is None or len(self.movies_df) == 0:
            logger.warning("⚠️ Nenhum filme disponível para recomendações de conteúdo")
            return []

        for movie_id in candidate_movies:
            try:
                if movie_id not in self.movies_df["id"].values:
                    continue

                movie_idx = self.movies_df[self.movies_df["id"] == movie_id].index[0]
                movie_data = self.movies_df.iloc[movie_idx]

                score = 0.0

                # Score baseado em gêneros preferidos
                movie_genres_str = movie_data.get("genres", "")
                if movie_genres_str and movie_genres_str.strip():
                    movie_genres = set([g.strip() for g in movie_genres_str.split("|") if g.strip()])
                    preferred_genres = set(profile["preferred_genres"])

                    if preferred_genres:
                        genre_match = len(
                            movie_genres.intersection(preferred_genres)
                        ) / len(preferred_genres)
                        score += genre_match * 0.4

                    # Score de pesos de gêneros baseado no histórico
                    for genre in movie_genres:
                        if genre in profile["genre_weights"]:
                            score += profile["genre_weights"][genre] * 0.3

                # Similaridade com filmes curtidos (limitado e com tratamento de erro)
                if profile["liked_movies"] and self.content_matrix is not None:
                    liked_similarities = []
                    for liked_movie_id in profile["liked_movies"][:5]:
                        if liked_movie_id in self.movies_df["id"].values:
                            liked_idx = self.movies_df[
                                self.movies_df["id"] == liked_movie_id
                            ].index[0]
                            similarity = self.get_content_similarity(movie_idx, liked_idx)
                            if similarity > 0:
                                liked_similarities.append(similarity)

                    if liked_similarities:
                        score += np.mean(liked_similarities) * 0.2

                # Penalizar filmes similares aos não curtidos
                if profile["disliked_movies"] and self.content_matrix is not None:
                    disliked_similarities = []
                    for disliked_movie_id in profile["disliked_movies"][:3]:
                        if disliked_movie_id in self.movies_df["id"].values:
                            disliked_idx = self.movies_df[
                                self.movies_df["id"] == disliked_movie_id
                            ].index[0]
                            similarity = self.get_content_similarity(
                                movie_idx, disliked_idx
                            )
                            if similarity > 0:
                                disliked_similarities.append(similarity)

                    if disliked_similarities:
                        score -= np.mean(disliked_similarities) * 0.1

                # Boost por popularidade e rating
                popularity = movie_data.get("popularity", 0)
                rating = movie_data.get("rating", 0)
                
                if popularity > 0:
                    score += min(popularity / 1000, 0.1)
                if rating > 0:
                    score += min(rating / 10, 0.1)

                scores[movie_id] = max(0.0, min(1.0, score))

            except Exception as e:
                logger.warning(f"⚠️ Erro ao calcular score para filme {movie_id}: {e}")
                continue

        return sorted(scores.items(), key=lambda x: x[1], reverse=True)[:top_n]

    def get_collaborative_recommendations(self, user_id, candidate_movies, top_n=10):
        """Recomendações colaborativas com tratamento robusto - VERSÃO MELHORADA"""
        if (self.collaborative_model is None or 
            not hasattr(self, 'user_ids') or 
            user_id not in self.user_ids):
            logger.debug(f"👤 Usuário {user_id} não encontrado no modelo colaborativo ou modelo não disponível")
            return []

        try:
            user_idx = self.user_ids.index(user_id)
            user_vector = self.user_factors[user_idx]

            scores = {}
            scores_found = 0
            
            for movie_id in candidate_movies:
                if movie_id in self.movie_ids:
                    movie_idx = self.movie_ids.index(movie_id)
                    item_vector = self.item_factors[movie_idx]
                    score = np.dot(user_vector, item_vector)
                    scores[movie_id] = score
                    scores_found += 1

            logger.debug(f"🎯 Collaborative scores calculados para {scores_found}/{len(candidate_movies)} filmes")
            
            if scores:
                return sorted(scores.items(), key=lambda x: x[1], reverse=True)[:top_n]
            else:
                return []
            
        except Exception as e:
            logger.warning(f"⚠️ Erro nas recomendações colaborativas para usuário {user_id}: {e}")
            return []

    
    def get_hybrid_recommendations(self, user_id, candidate_movies, top_n=5):
        """Combina recomendações com cache final e tratamento robusto"""
        
        # Gerar chave de cache baseada nos candidatos
        candidates_hash = hashlib.md5(
            ''.join(map(str, sorted(candidate_movies))).encode()
        ).hexdigest()[:8]
        
        # Verificar cache da recomendação final
        cached_result = cache.get("recommendation", user_id, candidates_hash, top_n)
        if cached_result:
            logger.info(f"⚡ Cache HIT - recomendação para usuário {user_id}")
            return cached_result

        logger.info(f"💭 Cache MISS - calculando recomendação para usuário {user_id}")

        try:
            # Calcular recomendações
            content_recs = dict(
                self.get_content_recommendations(user_id, candidate_movies, top_n * 2)
            )
            collab_recs = dict(
                self.get_collaborative_recommendations(user_id, candidate_movies, top_n * 2)
            )

            # Combinar scores
            final_scores = {}
            profile = self.user_profiles.get(user_id, {})
            user_interactions = len(profile.get("liked_movies", [])) + len(
                profile.get("disliked_movies", [])
            )

            for movie_id in candidate_movies:
                content_score = content_recs.get(movie_id, 0.0)
                collab_score = collab_recs.get(movie_id, 0.0)

                # Pesos adaptativos baseados na quantidade de interações
                if user_interactions < 5:
                    weight_content = 0.8
                    weight_collab = 0.2
                else:
                    weight_content = 0.4
                    weight_collab = 0.6

                final_score = content_score * weight_content + collab_score * weight_collab

                if final_score > 0:
                    final_scores[movie_id] = final_score

            result = sorted(final_scores.items(), key=lambda x: x[1], reverse=True)[:top_n]
            
            # Cachear resultado por 10 minutos
            cache.set("recommendation", user_id, candidates_hash, top_n, 
                     value=result, ttl=600)
            
            return result

        except Exception as e:
            logger.error(f"❌ Erro ao gerar recomendações híbridas: {e}")
            return []

    def train(self, new_ratings=None):
        """Treina modelo e invalida cache com tratamento robusto"""
        logger.info("🚀 Iniciando treinamento...")

        try:
            # Invalidar cache ao treinar
            cache.increment_version()

            # Carregar dados do banco
            self.load_data_from_db()

            # Adicionar novos ratings se fornecidos
            if new_ratings:
                try:
                    new_df = pd.DataFrame(new_ratings)
                    new_df.columns = ["user_id", "movie_id", "rating"]

                    # Garantir tipos corretos
                    new_df["user_id"] = new_df["user_id"].astype(int)
                    new_df["movie_id"] = new_df["movie_id"].astype(int)
                    new_df["rating"] = new_df["rating"].astype(int)

                    logger.info(f"📥 Novos ratings recebidos: {len(new_df)}")

                    # 🔥 Remover ratings existentes do mesmo user_id e movie_id
                    if len(self.ratings_df) > 0:
                        before_count = len(self.ratings_df)

                        self.ratings_df = self.ratings_df[
                            ~self.ratings_df.set_index(['user_id', 'movie_id']).index.isin(
                                new_df.set_index(['user_id', 'movie_id']).index
                            )
                        ]

                        after_count = len(self.ratings_df)
                        removed = before_count - after_count

                        logger.info(f"🗑️ {removed} ratings antigos removidos por sobrescrição")

                        # Adicionar os novos ratings
                        self.ratings_df = pd.concat([self.ratings_df, new_df], ignore_index=True)

                    else:
                        self.ratings_df = new_df

                    logger.info(f"➕ Total de ratings após atualização: {len(self.ratings_df)}")

                except Exception as e:
                    logger.warning(f"⚠️ Erro ao processar novos ratings: {e}")

            # Preparar features e modelos
            self.prepare_content_features()
            self.prepare_collaborative_model()

            # Limpar cache de perfis de usuário
            self.user_profiles.clear()

            self.last_update = datetime.now()
            logger.info("✅ Treinamento concluído - Cache invalidado")

        except Exception as e:
            logger.error(f"❌ Erro durante o treinamento: {e}")
            raise e

    def recommend(self, user_id, candidate_ids, top_n=1):
        """Gera recomendações com cache completo e tratamento robusto"""
        try:
            # Validar e converter IDs dos candidatos
            candidate_movies = []
            for mid in candidate_ids:
                try:
                    candidate_movies.append(int(mid))
                except (ValueError, TypeError):
                    logger.warning(f"⚠️ ID de filme inválido ignorado: {mid}")
                    continue

            if not candidate_movies:
                return {"error": "❌ Nenhum filme candidato válido"}

            # Gerar recomendações híbridas
            recommendations = self.get_hybrid_recommendations(
                user_id, candidate_movies, top_n
            )

            if not recommendations:
                return {"error": "❌ Não foi possível gerar recomendações"}

            return {
                "recommended_movie": recommendations[0][0],
                "score": float(recommendations[0][1]),
                "all_recommendations": [
                    (int(mid), float(score)) for mid, score in recommendations
                ],
                "cache_used": cache.available
            }

        except Exception as e:
            logger.error(f"❌ Erro ao gerar recomendação: {e}")
            return {"error": f"Erro interno: {str(e)}"}

# Instância global
recommender = HybridRecommender()

def load_model(force_model=True):
    """Carrega modelo salvo. Se não encontrar, decide se cria um novo com base no parâmetro force_model."""
    global recommender

    with model_lock:
        try:
            if os.path.exists(MODEL_PATH):
                with open(MODEL_PATH, "rb") as f:
                    loaded_recommender = pickle.load(f)

                if hasattr(loaded_recommender, 'movies_df'):
                    recommender = loaded_recommender
                    logger.info("✅ Modelo carregado com sucesso do arquivo")
                    return recommender
                else:
                    raise Exception("❌ Arquivo de modelo inválido ou corrompido.")

            elif force_model:
                logger.critical("❌ Arquivo de modelo NÃO encontrado e force_model=True. Abortando.")
                raise FileNotFoundError(f"Arquivo de modelo não encontrado em {MODEL_PATH}")

            else:
                logger.warning("⚠️ Arquivo de modelo não encontrado. Criando modelo vazio (modo dev ou retrain).")
                recommender = HybridRecommender()
                return recommender

        except Exception as e:
            logger.critical(f"❌ Erro ao carregar modelo: {e}")
            raise e

def save_model():
    """Salva modelo com tratamento de erro"""
    global recommender

    with model_lock:
        try:
            # Salvar em arquivo temporário primeiro
            tmp_path = f"{MODEL_PATH}.tmp"
            with open(tmp_path, "wb") as f:
                pickle.dump(recommender, f)
            
            # Substituir arquivo original apenas se salvamento foi bem-sucedido
            os.replace(tmp_path, MODEL_PATH)
            logger.info("📦 Modelo salvo com sucesso")
            
        except Exception as e:
            logger.error(f"❌ Erro ao salvar modelo: {e}")
            # Limpar arquivo temporário se existir
            try:
                if os.path.exists(f"{MODEL_PATH}.tmp"):
                    os.remove(f"{MODEL_PATH}.tmp")
            except:
                pass

def train(ratings_data):
    """API de treinamento com cache e tratamento robusto"""
    global recommender

    try:
        # Carregar modelo atual
        model = load_model()

        # Validar e formatar ratings
        formatted_ratings = []
        for r in ratings_data:
            try:
                formatted_rating = {
                    "user_id": int(r["user"]),
                    "movie_id": int(r["movie"]),
                    "rating": int(r["rating"]),
                }
                # Validar valores
                if formatted_rating["rating"] not in [0, 1]:
                    logger.warning(f"⚠️ Rating inválido (deve ser 0 ou 1): {r}")
                    continue
                    
                formatted_ratings.append(formatted_rating)
                
            except (ValueError, KeyError, TypeError) as e:
                logger.warning(f"⚠️ Rating inválido ignorado: {r} - Erro: {e}")
                continue

        if not formatted_ratings:
            return {
                "error": "❌ Nenhum rating válido fornecido",
                "cache_version": cache.model_version
            }

        # Executar treinamento
        model.train(formatted_ratings)
        
        # Salvar modelo treinado
        save_model()

        return {
            "message": f"✅ Treinamento concluído com {len(formatted_ratings)} ratings válidos",
            "cache_version": cache.model_version,
            "ratings_processed": len(formatted_ratings),
            "timestamp": datetime.now().isoformat()
        }

    except Exception as e:
        logger.error(f"❌ Erro na API de treinamento: {e}")
        return {
            "error": f"Erro no treinamento: {str(e)}",
            "cache_version": cache.model_version
        }

def recommend(user, candidate_ids, top_n=1):
    """API de recomendação com cache e tratamento robusto"""
    global recommender

    try:
        # Carregar modelo
        model = load_model()

        # Validar parâmetros
        try:
            user_id = int(user)
        except (ValueError, TypeError):
            return {"error": "❌ ID de usuário deve ser um número válido"}

        if not candidate_ids:
            return {"error": "❌ Lista de filmes candidatos não pode estar vazia"}

        if not isinstance(top_n, int) or top_n < 1:
            top_n = 1

        # Gerar recomendação
        result = model.recommend(user_id, candidate_ids, top_n)
        
        # Adicionar informações de debug se for erro
        if "error" in result:
            result["debug_info"] = {
                "user_id": user_id,
                "candidate_count": len(candidate_ids),
                "model_loaded": model is not None,
                "has_movies": model.movies_df is not None and len(model.movies_df) > 0 if model else False,
                "has_ratings": model.ratings_df is not None and len(model.ratings_df) > 0 if model else False
            }
        
        return result

    except Exception as e:
        logger.error(f"❌ Erro na API de recomendação: {e}")
        return {"error": f"Erro interno na recomendação: {str(e)}"}

def get_cache_stats():
    """Estatísticas do cache Redis com informações adicionais"""
    try:
        base_stats = cache.get_stats()
        
        # Adicionar informações do modelo se disponível
        if recommender:
            base_stats["model_info"] = {
                "last_update": recommender.last_update.isoformat() if recommender.last_update else None,
                "movies_count": len(recommender.movies_df) if recommender.movies_df is not None else 0,
                "ratings_count": len(recommender.ratings_df) if recommender.ratings_df is not None else 0,
                "user_profiles_count": len(recommender.user_profiles),
                "has_collaborative_model": recommender.collaborative_model is not None,
                "has_content_matrix": recommender.content_matrix is not None
            }
        
        return base_stats
        
    except Exception as e:
        logger.error(f"❌ Erro ao obter estatísticas do cache: {e}")
        return {"available": False, "error": str(e)}

def debug_ratings_data(self):
    """Debug dos dados de rating"""
    if self.ratings_df is None or len(self.ratings_df) == 0:
        logger.error("❌ Nenhum dado de rating disponível")
        return
        
    logger.info("🔍 DEBUG - Análise dos dados de rating:")
    logger.info(f"   📊 Total de ratings: {len(self.ratings_df)}")
    logger.info(f"   👥 Usuários únicos: {self.ratings_df['user_id'].nunique()}")
    logger.info(f"   🎬 Filmes únicos: {self.ratings_df['movie_id'].nunique()}")
    logger.info(f"   ⭐ Ratings positivos: {(self.ratings_df['rating'] == 1).sum()}")
    logger.info(f"   👎 Ratings negativos: {(self.ratings_df['rating'] == 0).sum()}")
    
    # Mostrar distribuição por usuário
    user_counts = self.ratings_df['user_id'].value_counts()
    logger.info(f"   📈 Ratings por usuário: min={user_counts.min()}, max={user_counts.max()}, média={user_counts.mean():.1f}")
    
    # Mostrar distribuição por filme
    movie_counts = self.ratings_df['movie_id'].value_counts()
    logger.info(f"   🎭 Ratings por filme: min={movie_counts.min()}, max={movie_counts.max()}, média={movie_counts.mean():.1f}")
    
    # Verificar se há dados duplicados
    duplicates = self.ratings_df.duplicated(['user_id', 'movie_id']).sum()
    if duplicates > 0:
        logger.warning(f"   ⚠️ {duplicates} entradas duplicadas encontradas!")
    
    # Amostra dos dados
    logger.info("   📋 Amostra dos dados:")
    for _, row in self.ratings_df.head(10).iterrows():
        logger.info(f"      Usuário {row['user_id']} -> Filme {row['movie_id']} = {row['rating']}")

def should_retrain():
    """Verifica se precisa retreinar com tratamento robusto"""
    try:
        if recommender is None or recommender.last_update is None:
            return True

        time_diff = datetime.now() - recommender.last_update
        return time_diff > timedelta(hours=24)
        
    except Exception as e:
        logger.warning(f"⚠️ Erro ao verificar necessidade de retreinamento: {e}")
        return False

def auto_retrain_if_needed():
    """Auto retreinamento com tratamento de erro"""
    try:
        if should_retrain():
            logger.info("🔄 Iniciando retreinamento automático...")
            recommender.train()
            save_model()
            logger.info("✅ Retreinamento automático concluído")
    except Exception as e:
        logger.warning(f"⚠️ Erro no retreinamento automático: {e}")
        # Não propagar o erro para não quebrar a aplicação