import pandas as pd
import psycopg2
from psycopg2.extras import execute_values


def conectar_banco():
    """Conecta ao banco PostgreSQL"""
    # Dados de conexão com o banco
    USUARIO = "cinematch"
    SENHA = "cinematch"
    HOST = "localhost"  # ou "postgres" se estiver em outro container
    PORTA = "5432"
    DB = "cinematch"

    return psycopg2.connect(
        dbname=DB, user=USUARIO, password=SENHA, host=HOST, port=PORTA
    )


def importar_filmes(cursor):
    """Importa filmes do CSV limpo"""
    print("\n📽️ IMPORTANDO FILMES...")

    # Carregar o CSV já tratado
    df = pd.read_csv("movies_limpo.csv")
    print(f"Filmes a importar: {len(df)}")

    # Dados a inserir
    dados = df.values.tolist()

    # Colunas na tabela (mesma ordem do CSV)
    campos = [
        "id",
        "title",
        "overview",
        "rating",
        "release_date",
        "original_language",
        "popularity",
        "adult",
        "poster_path",
    ]

    # Mostrar os primeiros dados a serem inseridos
    print("\n🔍 Primeiros registros a inserir:")
    for i, row in enumerate(dados[:3]):
        print(f"{i+1}: ID {row[0]} - {row[1]}")

    # Comando SQL com OVERRIDING SYSTEM VALUE
    query = f"""
        INSERT INTO movies ({', '.join(campos)})
        OVERRIDING SYSTEM VALUE
        VALUES %s
        ON CONFLICT (id) DO NOTHING;
    """

    # Inserir em lote
    execute_values(cursor, query, dados)
    print("✅ Filmes importados com sucesso!")

    return len(dados)


def executar_sql_generos(cursor, arquivo_sql):
    """Executa o arquivo SQL dos gêneros em lotes menores"""
    print(f"\n🎭 EXECUTANDO SQL DE GÊNEROS ({arquivo_sql})...")

    try:
        # Ler o arquivo SQL
        with open(arquivo_sql, "r", encoding="utf-8") as f:
            sql_content = f.read()

        # Dividir em linhas e filtrar apenas os INSERTs
        linhas = sql_content.split("\n")
        inserts = [
            linha.strip()
            for linha in linhas
            if linha.strip().startswith("INSERT INTO movie_genres")
        ]

        total_inserts = len(inserts)
        print(f"Total de inserções: {total_inserts}")

        if total_inserts == 0:
            print("⚠️ Nenhuma inserção encontrada no arquivo SQL!")
            return 0

        # Executar em lotes para não sobrecarregar
        tamanho_lote = 1000  # Ajuste conforme necessário
        inseridos = 0

        for i in range(0, total_inserts, tamanho_lote):
            lote = inserts[i : i + tamanho_lote]

            # Executar cada INSERT do lote
            for insert_sql in lote:
                cursor.execute(insert_sql)
                inseridos += 1

            # Fazer commit parcial e mostrar progresso
            cursor.connection.commit()
            progresso = (inseridos / total_inserts) * 100
            print(f"📊 Progresso: {inseridos}/{total_inserts} ({progresso:.1f}%)")

        print("✅ Gêneros importados com sucesso!")
        return total_inserts

    except FileNotFoundError:
        print(f"❌ Arquivo {arquivo_sql} não encontrado!")
        return 0
    except Exception as e:
        print(f"❌ Erro ao executar SQL: {e}")
        return 0


def main():
    """Função principal"""
    print("🚀 INICIANDO IMPORTAÇÃO AUTOMÁTICA")
    print("=" * 50)

    conn = None
    cur = None

    try:
        # Conectar ao banco
        conn = conectar_banco()
        cur = conn.cursor()

        # Configurar timeout maior para operações lentas
        cur.execute("SET statement_timeout = '30min';")

        # 1. Importar filmes
        total_filmes = importar_filmes(cur)

        # Commit após filmes para não perder o progresso
        conn.commit()
        print("💾 Filmes salvos no banco!")

        # 2. Executar SQL dos gêneros
        total_generos = executar_sql_generos(cur, "movie_genres_inserts.sql")

        # Commit final (já feito em lotes na função acima)
        print("💾 Transação finalizada!")

        # Relatório final
        print("\n" + "=" * 50)
        print("✅ IMPORTAÇÃO CONCLUÍDA COM SUCESSO!")
        print("=" * 50)
        print(f"📽️ Filmes importados: {total_filmes}")
        print(f"🎭 Inserções de gêneros executadas: {total_generos}")
        print("🎉 Banco de dados atualizado!")

    except FileNotFoundError as e:
        print(f"❌ Erro: Arquivo não encontrado - {e}")
        print("Certifique-se de que os arquivos estão no diretório correto:")
        print("  - movies_limpo.csv")
        print("  - movie_genres_inserts.sql")

    except psycopg2.Error as e:
        print(f"❌ Erro de banco de dados: {e}")
        if conn:
            conn.rollback()

    except Exception as e:
        print(f"❌ Erro durante execução: {e}")
        if conn:
            conn.rollback()

    finally:
        # Fechar conexões
        if cur:
            cur.close()
        if conn:
            conn.close()
        print("🔌 Conexão com banco fechada.")


if __name__ == "__main__":
    main()
