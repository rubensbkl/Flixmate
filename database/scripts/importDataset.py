import pandas as pd
import psycopg2
from psycopg2.extras import execute_values

# Carregar o CSV já tratado
df = pd.read_csv("newDataset.csv")

# Dados de conexão com o banco
USUARIO = "cinematch"
SENHA = "cinematch"
HOST = "localhost"  # ou "postgres" se estiver em outro container
PORTA = "5432"
DB = "cinematch"

# Conectar ao banco
conn = psycopg2.connect(
    dbname=DB,
    user=USUARIO,
    password=SENHA,
    host=HOST,
    port=PORTA
)
cur = conn.cursor()

# Dados a inserir
dados = df.values.tolist()

# Colunas na tabela (mesma ordem do CSV)
campos = ['id', 'title', 'overview', 'rating', 'release_date', 'original_language', 'popularity', 'adult', 'poster_path']

# Mostrar os primeiros dados a serem inseridos
print("\n🔍 Primeiros registros a inserir:")
for i, row in enumerate(dados[:5]):
    print(f"{i+1}: {dict(zip(campos, row))}")

# Comando SQL com OVERRIDING SYSTEM VALUE
query = f"""
    INSERT INTO movies ({', '.join(campos)})
    OVERRIDING SYSTEM VALUE
    VALUES %s
    ON CONFLICT (id) DO NOTHING;
"""

# Inserir em lote
execute_values(cur, query, dados)

# Finalizar
conn.commit()
cur.close()
conn.close()

print("\n✅ Importação concluída com sucesso (sem duplicatas).")