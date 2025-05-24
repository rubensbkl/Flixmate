import pandas as pd

# Caminho do CSV original
csv_entrada = 'TMDB_movie_dataset_v11.csv'

# Caminho do CSV tratado
csv_saida = 'movies_tratado.csv'

# Lê o arquivo CSV original
df = pd.read_csv(csv_entrada)

# Seleciona e renomeia as colunas desejadas
df_tratado = df[[
    'id', 
    'title', 
    'overview', 
    'vote_average', 
    'release_date', 
    'original_language', 
    'popularity', 
    'adult', 
    'poster_path'
]].rename(columns={
    'vote_average': 'rating'
})

# Salva o novo CSV
df_tratado.to_csv(csv_saida, index=False)

print(f'Dados tratados salvos em {csv_saida}')