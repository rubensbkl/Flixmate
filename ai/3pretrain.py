import os
import pickle
from datetime import datetime

import pandas as pd
from recommender import MODEL_PATH, HybridRecommender

### 📂 Caminhos dos dados
RATINGS_PATH = "./output/ratings_df_filtered.csv"  # ratings_df gerado pelo seu script
MODEL_OUTPUT_PATH = MODEL_PATH  # Salva no mesmo lugar que a API espera

### 🚀 1. Criar instância do recomendador
print("🚀 Iniciando processo de pré-treinamento...")
recommender = HybridRecommender()

print("🔗 Conectando ao banco e carregando dados dos filmes...")
try:
    recommender.load_data_from_db()

    if recommender.movies_df is None or len(recommender.movies_df) == 0:
        raise Exception("❌ Nenhum filme encontrado no banco! Verifique a conexão.")

    print(f"🎬 Filmes carregados do banco: {len(recommender.movies_df)}")

except Exception as e:
    print(f"❌ Erro ao conectar com o banco: {e}")
    raise

### 🔗 2. Carregar ratings offline
print("📥 Carregando ratings offline...")

# Verificar se o arquivo existe
if not os.path.exists(RATINGS_PATH):
    raise FileNotFoundError(f"❌ Arquivo não encontrado: {RATINGS_PATH}")

try:
    ratings_df = pd.read_csv(RATINGS_PATH)

    # Verificar estrutura do DataFrame
    required_columns = ["user_id", "movie_id", "rating"]
    if not all(col in ratings_df.columns for col in required_columns):
        raise ValueError(
            f"❌ Colunas esperadas: {required_columns}, encontradas: {list(ratings_df.columns)}"
        )

    # ✅ Garantir que está no formato correto
    ratings_df = ratings_df.astype({"user_id": int, "movie_id": int, "rating": int})

    print(f"✅ {len(ratings_df)} ratings carregados")
    print(
        f"📊 Range de ratings: {ratings_df['rating'].min()} a {ratings_df['rating'].max()}"
    )
    print(f"📊 Usuários únicos: {ratings_df['user_id'].nunique()}")
    print(f"📊 Filmes únicos: {ratings_df['movie_id'].nunique()}")

except Exception as e:
    print(f"❌ Erro ao carregar ratings: {e}")
    if "ratings_df" in locals():
        print(f"📊 Primeiras linhas do DataFrame:")
        print(ratings_df.head())
    raise

### 🔗 3. Incorporar ratings offline no modelo
print("🔄 Incorporando ratings ao modelo...")

# Se quiser, pode misturar com dados reais (opcional)
if recommender.ratings_df is not None and len(recommender.ratings_df) > 0:
    print(f"📊 Ratings existentes no banco: {len(recommender.ratings_df)}")
    combined_ratings = pd.concat(
        [recommender.ratings_df, ratings_df], ignore_index=True
    )
    print(f"📊 Combinando com ratings offline...")
else:
    combined_ratings = ratings_df

# 🔧 Remover duplicatas (manter o último rating em caso de conflito)
print("🔧 Verificando e removendo duplicatas...")
initial_count = len(combined_ratings)

# Verificar se há duplicatas
duplicates = combined_ratings.duplicated(subset=["user_id", "movie_id"], keep=False)
if duplicates.any():
    print(
        f"⚠️  Encontradas {duplicates.sum()} linhas com duplicatas (user_id, movie_id)"
    )

    # Manter apenas o último rating para cada par (user_id, movie_id)
    combined_ratings = combined_ratings.drop_duplicates(
        subset=["user_id", "movie_id"], keep="last"
    ).reset_index(drop=True)

    print(
        f"🔧 Duplicatas removidas: {initial_count} -> {len(combined_ratings)} ratings"
    )
else:
    print("✅ Nenhuma duplicata encontrada")

recommender.ratings_df = combined_ratings
print(f"🧠 Total de ratings para treinamento: {len(recommender.ratings_df)}")

# 🔍 Validações adicionais
print("🔍 Validando dados para treinamento...")
print(f"📊 Usuários únicos: {recommender.ratings_df['user_id'].nunique()}")
print(f"📊 Filmes únicos: {recommender.ratings_df['movie_id'].nunique()}")
print(f"📊 Ratings únicos por par (user_id, movie_id): {len(recommender.ratings_df)}")

# Verificar se há valores nulos
null_check = recommender.ratings_df.isnull().sum()
if null_check.any():
    print(f"⚠️  Valores nulos encontrados:")
    print(null_check[null_check > 0])

    # Remover linhas com valores nulos
    recommender.ratings_df = recommender.ratings_df.dropna()
    print(
        f"🔧 Linhas com valores nulos removidas. Total final: {len(recommender.ratings_df)}"
    )

### 🚀 4. Treinar o modelo
print("🚀 Iniciando treinamento do modelo...")

try:
    print("🔧 Preparando features de conteúdo...")
    recommender.prepare_content_features()
    print("✅ Features de conteúdo preparadas")

    print("🔧 Preparando modelo colaborativo...")
    recommender.prepare_collaborative_model()
    print("✅ Modelo colaborativo preparado")

except Exception as e:
    print(f"❌ Erro durante o treinamento: {e}")
    raise

print("✅ Treinamento concluído com sucesso!")

### 💾 5. Salvar modelo
print(f"💾 Salvando modelo em {MODEL_OUTPUT_PATH}")

try:
    # Criar diretório se não existir
    model_dir = os.path.dirname(MODEL_OUTPUT_PATH)
    if model_dir:
        os.makedirs(model_dir, exist_ok=True)

    # Salvar com arquivo temporário para evitar corrupção
    tmp_path = f"{MODEL_OUTPUT_PATH}.tmp"

    with open(tmp_path, "wb") as f:
        pickle.dump(recommender, f)

    # Mover arquivo temporário para o final
    os.replace(tmp_path, MODEL_OUTPUT_PATH)

    # Verificar se o arquivo foi salvo corretamente
    if os.path.exists(MODEL_OUTPUT_PATH):
        file_size = os.path.getsize(MODEL_OUTPUT_PATH)
        print(f"🎉 Modelo salvo com sucesso!")
        print(f"📁 Caminho: {MODEL_OUTPUT_PATH}")
        print(f"📦 Tamanho: {file_size / (1024*1024):.2f} MB")
    else:
        raise Exception("❌ Arquivo não foi criado corretamente")

except Exception as e:
    print(f"❌ Erro ao salvar modelo: {e}")
    # Limpar arquivo temporário se existir
    tmp_path = f"{MODEL_OUTPUT_PATH}.tmp"
    if os.path.exists(tmp_path):
        os.remove(tmp_path)
    raise

print(f"🎯 Processo concluído com sucesso!")
print(f"📅 {datetime.now().isoformat()}")
