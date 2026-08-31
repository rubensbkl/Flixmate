#!/usr/bin/env python3
"""
Importação filtrada do TMDB v11 para o Postgres do Flixmate.
Filtra ~60K filmes de qualidade e importa com gêneros.
"""

import csv
import json
import sys
import time
import psycopg2
from psycopg2.extras import execute_values

# ─── Configuração ─────────────────────────────────────────────
CSV_PATH = "/home/bkl/git/Flixmate/database/scripts/TMDB_movie_dataset_v11.csv"

DB_CONFIG = {
    "dbname": "cinematch",
    "user": "cinematch",
    "password": "cinematch",
    "host": "localhost",
    "port": "5432",
}

ALLOWED_LANGUAGES = {"en", "es", "fr", "de", "it", "pt"}
MIN_VOTE_COUNT = 10

# Mapeamento do TMDB (Inglês) para os IDs no nosso banco
GENRE_MAP = {
    "Action": 28,
    "Adventure": 12,
    "Animation": 16,
    "Comedy": 35,
    "Crime": 80,
    "Documentary": 99,
    "Drama": 18,
    "Family": 10751,
    "Fantasy": 14,
    "History": 36,
    "Horror": 27,
    "Music": 10402,
    "Mystery": 9648,
    "Romance": 10749,
    "Science Fiction": 878,
    "TV Movie": 10770,
    "Thriller": 53,
    "War": 10752,
    "Western": 37
}

# ─── Funções ──────────────────────────────────────────────────
def parse_genres(genres_str):
    """Extrai lista de genre_ids do campo genres do CSV."""
    if not genres_str or genres_str.strip() == "":
        return []
    
    # No CSV do TMDB v11, eles vêm como "Action, Science Fiction, Adventure"
    names = [n.strip() for n in genres_str.split(",")]
    ids = [GENRE_MAP[n] for n in names if n in GENRE_MAP]
    return ids


def filter_and_load(csv_path):
    """Lê o CSV e retorna filmes filtrados + mapeamento de gêneros."""
    movies = []
    movie_genres = []
    total = 0
    skipped = {"no_poster": 0, "no_overview": 0, "not_released": 0, "bad_lang": 0, "low_votes": 0, "no_title": 0, "no_date": 0}

    print(f"📂 Lendo {csv_path}...")
    t0 = time.time()

    with open(csv_path, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            total += 1

            title = row.get("title", "").strip()
            if not title:
                skipped["no_title"] += 1
                continue

            poster = row.get("poster_path", "").strip()
            if not poster:
                skipped["no_poster"] += 1
                continue

            overview = row.get("overview", "").strip()
            if not overview:
                skipped["no_overview"] += 1
                continue

            status = row.get("status", "").strip()
            if status != "Released":
                skipped["not_released"] += 1
                continue

            lang = row.get("original_language", "").strip()
            if lang not in ALLOWED_LANGUAGES:
                skipped["bad_lang"] += 1
                continue

            try:
                vote_count = int(float(row.get("vote_count", "0").strip() or "0"))
            except (ValueError, TypeError):
                vote_count = 0
            if vote_count < MIN_VOTE_COUNT:
                skipped["low_votes"] += 1
                continue

            # Campos do filme
            try:
                movie_id = int(row.get("id", "0").strip())
            except (ValueError, TypeError):
                continue

            try:
                rating = float(row.get("vote_average", "0").strip() or "0")
            except (ValueError, TypeError):
                rating = 0.0

            release_date = row.get("release_date", "").strip() or "1900-01-01"

            try:
                popularity = float(row.get("popularity", "0").strip() or "0")
            except (ValueError, TypeError):
                popularity = 0.0

            backdrop = row.get("backdrop_path", "").strip() or None

            movies.append((
                movie_id,
                title,
                overview,
                rating,
                release_date,
                lang,
                popularity,
                poster,
                backdrop,
            ))

            # Gêneros
            genre_ids = parse_genres(row.get("genres", ""))
            for gid in genre_ids:
                movie_genres.append((movie_id, gid))

            if total % 200000 == 0:
                print(f"   ... {total:,} linhas processadas, {len(movies):,} aceitos")

    elapsed = time.time() - t0
    print(f"\n📊 Resultado do filtro ({elapsed:.1f}s):")
    print(f"   Total no CSV:    {total:,}")
    print(f"   Aceitos:         {len(movies):,}")
    print(f"   Rejeitados:      {total - len(movies):,}")
    for reason, count in sorted(skipped.items(), key=lambda x: -x[1]):
        if count > 0:
            print(f"     - {reason}: {count:,}")
    print(f"   Relações gênero: {len(movie_genres):,}")

    return movies, movie_genres


def import_to_db(movies, movie_genres):
    """Importa filmes e gêneros no Postgres."""
    print(f"\n🗄️  Conectando ao banco...")
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()
    cur.execute("SET statement_timeout = '30min';")

    # Limpar dados antigos
    print("🧹 Limpando tabelas dependentes...")
    cur.execute("DELETE FROM recommendations;")
    cur.execute("DELETE FROM watchlater;")
    cur.execute("DELETE FROM favorite;")
    cur.execute("DELETE FROM feedbacks;")
    cur.execute("DELETE FROM movie_genres;")
    cur.execute("DELETE FROM movies;")
    conn.commit()
    print("   ✅ Tabelas limpas")

    # Inserir filmes em lotes
    BATCH = 5000
    print(f"\n📽️  Importando {len(movies):,} filmes em lotes de {BATCH}...")
    t0 = time.time()

    for i in range(0, len(movies), BATCH):
        batch = movies[i:i + BATCH]
        execute_values(
            cur,
            """INSERT INTO movies (id, title, overview, rating, release_date,
                                   original_language, popularity, poster_path, backdrop_path)
               OVERRIDING SYSTEM VALUE
               VALUES %s
               ON CONFLICT (id) DO NOTHING""",
            batch,
        )
        conn.commit()
        done = min(i + BATCH, len(movies))
        pct = done * 100 / len(movies)
        print(f"   📊 {done:,}/{len(movies):,} filmes ({pct:.0f}%)")

    elapsed = time.time() - t0
    print(f"   ✅ Filmes importados em {elapsed:.1f}s")

    # Inserir gêneros em lotes
    print(f"\n🎭 Importando {len(movie_genres):,} relações filme-gênero...")
    t0 = time.time()

    for i in range(0, len(movie_genres), BATCH):
        batch = movie_genres[i:i + BATCH]
        execute_values(
            cur,
            """INSERT INTO movie_genres (movie_id, genre_id)
               VALUES %s
               ON CONFLICT (movie_id, genre_id) DO NOTHING""",
            batch,
        )
        conn.commit()
        done = min(i + BATCH, len(movie_genres))
        if done % 20000 < BATCH:
            pct = done * 100 / len(movie_genres)
            print(f"   📊 {done:,}/{len(movie_genres):,} relações ({pct:.0f}%)")

    elapsed = time.time() - t0
    print(f"   ✅ Gêneros importados em {elapsed:.1f}s")

    # Verificação final
    cur.execute("SELECT COUNT(*) FROM movies;")
    count_movies = cur.fetchone()[0]
    cur.execute("SELECT COUNT(*) FROM movie_genres;")
    count_genres = cur.fetchone()[0]
    cur.execute("SELECT COUNT(DISTINCT original_language) FROM movies;")
    count_langs = cur.fetchone()[0]

    print(f"\n🎉 Importação concluída!")
    print(f"   📽️  Filmes no banco:     {count_movies:,}")
    print(f"   🎭 Relações de gênero:  {count_genres:,}")
    print(f"   🌍 Idiomas:             {count_langs}")

    cur.close()
    conn.close()


# ─── Main ─────────────────────────────────────────────────────
if __name__ == "__main__":
    print("🚀 Flixmate — Importação filtrada do TMDB v11")
    print("=" * 55)

    movies, movie_genres = filter_and_load(CSV_PATH)

    if not movies:
        print("❌ Nenhum filme passou no filtro!")
        sys.exit(1)

    import_to_db(movies, movie_genres)
