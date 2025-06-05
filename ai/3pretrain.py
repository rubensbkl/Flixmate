import pandas as pd
import os
import pickle
from datetime import datetime

from recommender import HybridRecommender, MODEL_PATH


### 📂 Caminhos dos dados
RATINGS_PATH = "./output/ratings_df_filtered.csv"  # ratings_df gerado pelo seu script
MODEL_OUTPUT_PATH = MODEL_PATH             # Salva no mesmo lugar que a API espera

### ✅ Configuração do banco (já puxa do seu .env igual no recommender)
# O HybridRecommender já faz conexão com o PostgreSQL e carrega o movies_df real


### 🚀 1. Criar instância do recomendador
recommender = HybridRecommender()

print("🔗 Conectando ao banco e carregando dados dos filmes...")
recommender.load_data_from_db()

if recommender.movies_df is None or len(recommender.movies_df) == 0:
    raise Exception("❌ Nenhum filme encontrado no banco! Verifique a conexão.")

print(f"🎬 Filmes carregados do banco: {len(recommender.movies_df)}")


### 🔗 2. Carregar ratings offline
print("📥 Carregando ratings offline...")
ratings_df = pd.read_csv(RATINGS_PATH)

# ✅ Garantir que está no formato correto
ratings_df = ratings_df.astype({
    "user_id": int,
    "movie_id": int,
    "rating": int
})

print(f"✅ {len(ratings_df)} ratings carregados")


### 🔗 3. Incorporar ratings offline no modelo
# Se quiser, pode misturar com dados reais (opcional)
if recommender.ratings_df is not None and len(recommender.ratings_df) > 0:
    combined_ratings = pd.concat([recommender.ratings_df, ratings_df], ignore_index=True)
else:
    combined_ratings = ratings_df

recommender.ratings_df = combined_ratings

print(f"🧠 Total de ratings para treinamento: {len(recommender.ratings_df)}")


### 🚀 4. Treinar o modelo
print("🚀 Treinando modelo...")

recommender.prepare_content_features()
recommender.prepare_collaborative_model()

print("✅ Treinamento concluído")


### 💾 5. Salvar modelo
print(f"💾 Salvando modelo em {MODEL_OUTPUT_PATH}")

tmp_path = f"{MODEL_OUTPUT_PATH}.tmp"
with open(tmp_path, "wb") as f:
    pickle.dump(recommender, f)

os.replace(tmp_path, MODEL_OUTPUT_PATH)

print(f"🎉 Modelo salvo com sucesso em {MODEL_OUTPUT_PATH}")
print(f"📅 {datetime.now().isoformat()}")
