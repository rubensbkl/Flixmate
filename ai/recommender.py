import os
import pickle
import pandas as pd
import logging
import threading
from functools import lru_cache
from river import compose, linear_model, preprocessing
from sqlalchemy import create_engine

DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = os.getenv("DB_PORT", "5432")
DB_NAME = os.getenv("DB_NAME", "postgres")
DB_USER = os.getenv("DB_USER", "postgres")
DB_PASSWORD = os.getenv("DB_PASSWORD", "postgres")

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

MODEL_DIR = "/app/model_data"
os.makedirs(MODEL_DIR, exist_ok=True)
MODEL_PATH = f"{MODEL_DIR}/model.pkl"

model_lock = threading.Lock()

def load_model():
    with model_lock:
        try:
            with open(MODEL_PATH, 'rb') as f:
                logger.info("✅ Modelo carregado.")
                return pickle.load(f)
        except Exception as e:
            logger.warning(f"⚠️ Criando novo modelo. Erro: {e}")
            return compose.Pipeline(
                preprocessing.OneHotEncoder(),
                preprocessing.StandardScaler(),
                linear_model.LogisticRegression()
            )

def save_model(model, updated):
    if updated:
        with model_lock:
            tmp_path = f"{MODEL_PATH}.tmp"
            with open(tmp_path, 'wb') as f:
                pickle.dump(model, f)
            os.replace(tmp_path, MODEL_PATH)
            logger.info("📦 Modelo salvo com segurança.")
    else:
        logger.info("📭 Sem atualizações no modelo.")

@lru_cache(maxsize=1)
def load_movies():
    logger.info("🎬 Carregando filmes do banco de dados...")

    engine = create_engine(f"postgresql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}")
    query = """
        SELECT
            id,
            rating,
            popularity,
            release_date,
            original_language,
            adult
        FROM movies
    """
    
    df = pd.read_sql(query, engine)

    df['release_year'] = pd.to_datetime(df['release_date']).dt.year
    df['adult'] = df['adult'].map({'f': 0, 't': 1, False: 0, True: 1}).fillna(0).astype(int)
    
    return df.set_index('id').to_dict(orient='index')

def clear_movies_cache():
    load_movies.cache_clear()
    logger.info("🧹 Cache de filmes limpo.")

def movie_to_features(movie_id, user, movie):
    return {
        'user': user,
        'movie': str(movie_id),
        'rating': float(movie.get('rating', 0)),
        'popularity': float(movie.get('popularity', 0)),
        'release_year': int(movie.get('release_year', 2000)),
        'original_language': movie.get('original_language', 'unknown'),
        'adult': int(movie.get('adult', 0)),
    }

def train_model(ratings, model, movie_dict):
    updated = False
    for r in ratings:
        user = r['user']
        try:
            movie_id = int(r['movie'])
        except (ValueError, KeyError):
            logger.warning(f"ID de filme inválido ou faltando: {r.get('movie', None)}")
            continue

        if movie_id not in movie_dict:
            logger.warning(f"🎞️ Filme {movie_id} não encontrado.")
            continue

        x = movie_to_features(movie_id, user, movie_dict[movie_id])
        logger.info(f"Treinando usuário {user} com filme {movie_id} e features {x} => rating {r['rating']}")
        model.learn_one(x, r['rating'])
        updated = True

    return model, updated

def model_recommend(user, model, movie_dict, top_n=5):
    scores = []
    for movie_id, movie in movie_dict.items():
        x = movie_to_features(movie_id, user, movie)
        proba = model.predict_proba_one(x)
        score = max([proba.get(k, 0) for k in [True, 1, '1']])
        logger.info(f"User {user} - Movie {movie_id} - Score {score:.4f} - Features: {x}")
        scores.append((movie_id, score))

    top = sorted(scores, key=lambda x: x[1], reverse=True)[:top_n]

    candidate_ids = movie_dict.keys()
    missing = [mid for mid in candidate_ids if mid not in movie_dict]
    if missing:
        logger.warning(f"🎯 Filmes candidatos ausentes: {missing}")

    logger.info(f"Top {top_n} recommendations for user {user}: {top}")
    return top

# ENDPOINT 1 - Treina o modelo
def train(ratings):
    model = load_model()
    movie_dict = load_movies()

    model, updated = train_model(ratings, model, movie_dict)
    save_model(model, updated)
    return {"message": "✅ Treinamento concluído com sucesso."}

# ENDPOINT 2 - Retorna recomendação
def recommend(user, candidate_ids, top_n=1):
    model = load_model()
    movie_dict = load_movies()
    candidate_movies = {
        int(mid): movie_dict[int(mid)]
        for mid in candidate_ids if int(mid) in movie_dict
    }

    top = model_recommend(user, model, candidate_movies, top_n=top_n)
    if not top:
        return {"error": "❌ Nenhum filme candidato disponível para recomendação."}

    return {"recommended_movie": top[0][0], "score": top[0][1]}