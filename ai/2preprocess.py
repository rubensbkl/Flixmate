import pandas as pd

# 📥 Carregar ratings
ratings = pd.read_csv('./output/ratings_df.csv')

# 📥 Carregar movies do seu banco
movies = pd.read_csv('./output/movies.csv')  # Ou onde estiver seu arquivo

# 🎯 Obter lista de movie_ids válidos
valid_movie_ids = set(movies['id'].astype(int))

print(f"🎬 Filmes no banco atual: {len(valid_movie_ids)}")
print(f"⭐ Ratings antes do filtro: {len(ratings)}")

# 🔍 Filtrar ratings
filtered_ratings = ratings[ratings['movie_id'].isin(valid_movie_ids)]

print(f"✅ Ratings após filtro: {len(filtered_ratings)}")

# 💾 Salvar ratings filtrados
filtered_ratings.to_csv('./output/ratings_df_filtered.csv', index=False)

print("💾 Ratings filtrados salvos em ./output/ratings_df_filtered.csv")
