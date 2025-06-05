import pandas as pd
import os


### 🔗 Caminhos dos arquivos do MovieLens
DATASET_DIR = "./movielens"  # Caminho onde estão os arquivos
LINKS_PATH = "./movielens/links.csv"  # Vem do MovieLens latest (contém tmdbId)
OUTPUT_DIR = "./output"

os.makedirs(OUTPUT_DIR, exist_ok=True)


### 🚀 1. Carregar arquivos do MovieLens 1M
# ratings.dat -> userId::movieId::rating::timestamp
ratings = pd.read_csv(
    os.path.join(DATASET_DIR, "ratings10m.dat"),
    sep="::",
    engine="python",
    names=["userId", "movieId", "rating", "timestamp"],
)

print(f"✅ Carregado {len(ratings)} ratings")


### 🔗 2. Carregar mapeamento para TMDB (links.csv)
links = pd.read_csv(LINKS_PATH)

# Alguns movieId podem não ter tmdbId
links = links.dropna(subset=["tmdbId"])
links["tmdbId"] = links["tmdbId"].astype(int)

print(f"🔗 Carregado {len(links)} links com tmdbId")


### 🔀 3. Fazer merge ratings -> links
ratings = ratings.merge(links[["movieId", "tmdbId"]], on="movieId")

print(f"⭐ Ratings com tmdbId após merge: {len(ratings)}")

### 🔄 4. Converter rating para binário (opcional)
# rating >= 3 -> 1 (like), rating < 3 -> 0 (dislike)
ratings["rating_binary"] = ratings["rating"].apply(lambda x: 1 if x >= 3 else 0)


### 🆔 5. Remapear userId para IDs fictícios para não colidir com seu banco
# Por exemplo, começar em 1000000
user_map = {uid: idx for idx, uid in enumerate(ratings["userId"].unique(), start=1000000)}
ratings["user_id_mapped"] = ratings["userId"].map(user_map)


### 🗂️ 6. Gerar DataFrames finais no formato do seu recommender

# ratings_df
ratings_df = ratings[["user_id_mapped", "tmdbId", "rating_binary"]]
ratings_df.columns = ["user_id", "movie_id", "rating"]
print(f"✅ ratings_df pronto com {len(ratings_df)} registros")


### 💾 8. Salvar arquivos para usar no pretrain
ratings_df.to_csv(os.path.join(OUTPUT_DIR, "ratings_df.csv"), index=False)

print(f"💾 Arquivos salvos em {OUTPUT_DIR}/")
