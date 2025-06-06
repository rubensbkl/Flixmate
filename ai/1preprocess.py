import os

import pandas as pd

### 🔗 Caminhos dos arquivos do MovieLens
DATASET_DIR = "./movielens"  # Caminho onde estão os arquivos
LINKS_PATH = "./movielens/links.csv"  # Vem do MovieLens latest (contém tmdbId)
OUTPUT_DIR = "./output"

os.makedirs(OUTPUT_DIR, exist_ok=True)

### 🚀 1. Carregar arquivos do MovieLens 1M
# ratings.dat -> userId::movieId::rating::timestamp
ratings = pd.read_csv(
    os.path.join(DATASET_DIR, "ratings.csv"),
    sep=",",
    engine="python",
)

# Check if file has headers, if not add them
if ratings.columns[0] == '0' or 'userId' not in ratings.columns:
    ratings.columns = ["userId", "movieId", "rating", "timestamp"]

print(f"✅ Carregado {len(ratings)} ratings")

# 🔧 Converter movieId para int (se não estiver)
ratings["movieId"] = pd.to_numeric(ratings["movieId"], errors="coerce")
ratings = ratings.dropna(subset=["movieId"])  # Remove linhas com movieId inválido
ratings["movieId"] = ratings["movieId"].astype(int)

# 🔧 Converter rating para numeric
ratings["rating"] = pd.to_numeric(ratings["rating"], errors="coerce")
ratings = ratings.dropna(subset=["rating"])  # Remove linhas com rating inválido

print(f"🔧 Ratings após limpeza de movieId: {len(ratings)}")

### 🔗 2. Carregar mapeamento para TMDB (links.csv)
links = pd.read_csv(LINKS_PATH)

print(f"📊 Tipos de dados em links:")
print(links.dtypes)

# 🔧 Garantir que movieId é int em ambos os DataFrames
links["movieId"] = pd.to_numeric(links["movieId"], errors="coerce")
links = links.dropna(subset=["movieId"])
links["movieId"] = links["movieId"].astype(int)

# Alguns movieId podem não ter tmdbId
links = links.dropna(subset=["tmdbId"])
links["tmdbId"] = pd.to_numeric(links["tmdbId"], errors="coerce")
links = links.dropna(subset=["tmdbId"])
links["tmdbId"] = links["tmdbId"].astype(int)

print(f"🔗 Carregado {len(links)} links com tmdbId válidos")

# 🔍 Debug: Verificar tipos antes do merge
print(f"📊 Tipo movieId em ratings: {ratings['movieId'].dtype}")
print(f"📊 Tipo movieId em links: {links['movieId'].dtype}")

### 🔀 3. Fazer merge ratings -> links
ratings = ratings.merge(links[["movieId", "tmdbId"]], on="movieId", how="inner")

print(f"⭐ Ratings com tmdbId após merge: {len(ratings)}")

### 🔄 4. Converter rating para binário (opcional)
# rating >= 3 -> 1 (like), rating < 3 -> 0 (dislike)
ratings["rating_binary"] = (ratings["rating"] >= 3).astype(int)

### 🆔 5. Remapear userId para IDs fictícios para não colidir com seu banco
# Por exemplo, começar em 1000000
user_map = {uid: idx for idx, uid in enumerate(ratings["userId"].unique(), start=1000000)}
ratings["user_id_mapped"] = ratings["userId"].map(user_map)

### 🗂️ 6. Gerar DataFrames finais no formato do seu recommender

# ratings_df
ratings_df = ratings[["user_id_mapped", "tmdbId", "rating_binary"]]
ratings_df.columns = ["user_id", "movie_id", "rating"]
print(f"✅ ratings_df pronto com {len(ratings_df)} registros")

# 🔍 Mostrar estatísticas finais
print(f"\n📊 Estatísticas finais:")
print(f"   Usuários únicos: {ratings_df['user_id'].nunique()}")
print(f"   Filmes únicos: {ratings_df['movie_id'].nunique()}")
print(f"   Ratings positivos: {(ratings_df['rating'] == 1).sum()}")
print(f"   Ratings negativos: {(ratings_df['rating'] == 0).sum()}")

### 💾 8. Salvar arquivos para usar no pretrain
ratings_df.to_csv(os.path.join(OUTPUT_DIR, "ratings_df.csv"), index=False)

print(f"💾 Arquivos salvos em {OUTPUT_DIR}/")