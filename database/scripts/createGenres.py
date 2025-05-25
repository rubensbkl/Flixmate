import ast
import json

import pandas as pd


def extrair_generos_filmes(dataset_original, filmes_limpos, arquivo_sql_saida):
    """
    Extrai gêneros dos filmes limpos e gera SQL para popular movie_genres
    """
    print("Carregando datasets...")

    # Carregar dataset original (com gêneros)
    df_original = pd.read_csv(dataset_original)
    print(f"Dataset original carregado: {len(df_original)} filmes")

    # Carregar filmes limpos
    df_limpos = pd.read_csv(filmes_limpos)
    print(f"Filmes limpos carregados: {len(df_limpos)} filmes")

    # Verificar se a coluna de gêneros existe
    if "genres" not in df_original.columns:
        print("ERRO: Coluna 'genres' não encontrada no dataset original!")
        print(f"Colunas disponíveis: {list(df_original.columns)}")
        return

    coluna_generos = "genres"
    print(f"Usando coluna de gêneros: {coluna_generos}")

    # Fazer merge dos datasets pelos IDs
    df_merged = df_limpos[["id"]].merge(
        df_original[["id", coluna_generos]], on="id", how="left"
    )

    print(f"Filmes com match: {len(df_merged.dropna(subset=[coluna_generos]))}")

    # Mapeamento dos gêneros: Inglês -> Português (conforme seu banco)
    mapeamento_generos = {
        "Action": 28,  # Ação
        "Adventure": 12,  # Aventura
        "Animation": 16,  # Animação
        "Comedy": 35,  # Comédia
        "Crime": 80,  # Crime
        "Documentary": 99,  # Documentário
        "Drama": 18,  # Drama
        "Family": 10751,  # Família
        "Fantasy": 14,  # Fantasia
        "History": 36,  # História
        "Horror": 27,  # Terror
        "Music": 10402,  # Música
        "Mystery": 9648,  # Mistério
        "Romance": 10749,  # Romance
        "Science Fiction": 878,  # Ficção científica
        "TV Movie": 10770,  # Cinema TV
        "Thriller": 53,  # Thriller
        "War": 10752,  # Guerra
        "Western": 37,  # Faroeste
    }

    # Nomes dos gêneros em português para referência
    generos_pt = {
        28: "Ação",
        12: "Aventura",
        16: "Animação",
        35: "Comédia",
        80: "Crime",
        99: "Documentário",
        18: "Drama",
        10751: "Família",
        14: "Fantasia",
        36: "História",
        27: "Terror",
        10402: "Música",
        9648: "Mistério",
        10749: "Romance",
        878: "Ficção científica",
        10770: "Cinema TV",
        53: "Thriller",
        10752: "Guerra",
        37: "Faroeste",
    }

    def processar_generos(generos_str):
        """
        Processa string de gêneros em inglês e retorna lista de IDs para o banco português
        """
        if pd.isna(generos_str) or generos_str == "":
            return []

        try:
            # Gêneros vêm como string separada por vírgula: "Adventure, Fantasy, Action"
            generos_lista = [genero.strip() for genero in str(generos_str).split(",")]

            # Converter gêneros inglês -> IDs portugueses
            ids_generos = []
            for genero_en in generos_lista:
                if genero_en in mapeamento_generos:
                    ids_generos.append(mapeamento_generos[genero_en])
                else:
                    print(f"Gênero não mapeado: '{genero_en}'")

            return ids_generos

        except Exception as e:
            print(f"Erro ao processar gêneros '{generos_str}': {e}")
            return []

    # Processar gêneros
    print("Processando gêneros...")
    df_merged["genre_ids"] = df_merged[coluna_generos].apply(processar_generos)

    # Identificar filmes sem gêneros
    filmes_sem_generos = df_merged[df_merged["genre_ids"].apply(len) == 0]
    if len(filmes_sem_generos) > 0:
        print(f"\n⚠️  FILMES SEM GÊNEROS ENCONTRADOS ({len(filmes_sem_generos)}):")

        # Buscar títulos dos filmes sem gêneros
        filmes_sem_generos_com_titulo = filmes_sem_generos.merge(
            df_limpos[["id", "title"]], on="id", how="left"
        )

        for idx, row in filmes_sem_generos_com_titulo.head(
            20
        ).iterrows():  # Mostrar até 20
            titulo = row.get("title", "Título não encontrado")
            genero_original = (
                row[coluna_generos] if pd.notna(row[coluna_generos]) else "N/A"
            )
            print(f"  - ID {row['id']}: {titulo}")
            print(f"    Gênero original: '{genero_original}'")

        if len(filmes_sem_generos) > 20:
            print(f"  ... e mais {len(filmes_sem_generos) - 20} filmes")
    else:
        print("✅ Todos os filmes têm gêneros!")

    # Gerar SQL
    sql_inserts = []
    sql_inserts.append("-- Inserir relações movie_genres")
    sql_inserts.append("-- Gerado automaticamente")
    sql_inserts.append("")

    total_relacoes = 0
    filmes_com_generos = 0

    for _, row in df_merged.iterrows():
        movie_id = row["id"]
        genre_ids = row["genre_ids"]

        if genre_ids:  # Se tem gêneros
            filmes_com_generos += 1
            for genre_id in genre_ids:
                sql_inserts.append(
                    f"INSERT INTO movie_genres (movie_id, genre_id) VALUES ({movie_id}, {genre_id}) ON CONFLICT DO NOTHING;"
                )
                total_relacoes += 1

    # Adicionar estatísticas detalhadas no final
    sql_inserts.append("")
    sql_inserts.append(f"-- Estatísticas:")
    sql_inserts.append(f"-- Total de filmes processados: {len(df_limpos)}")
    sql_inserts.append(f"-- Filmes com gêneros encontrados: {filmes_com_generos}")
    sql_inserts.append(f"-- Total de relações movie_genres: {total_relacoes}")

    # Estatísticas por gênero
    sql_inserts.append(f"-- Distribuição por gênero:")
    contagem_generos = {}
    for _, row in df_merged.iterrows():
        for genre_id in row["genre_ids"]:
            nome_genero = generos_pt.get(genre_id, f"ID_{genre_id}")
            contagem_generos[nome_genero] = contagem_generos.get(nome_genero, 0) + 1

    for genero, count in sorted(
        contagem_generos.items(), key=lambda x: x[1], reverse=True
    ):
        sql_inserts.append(f"--   {genero}: {count} filmes")

    # Salvar arquivo SQL
    with open(arquivo_sql_saida, "w", encoding="utf-8") as f:
        f.write("\n".join(sql_inserts))

    print(f"\n=== RESULTADO ===")
    print(f"Total de filmes processados: {len(df_limpos)}")
    print(f"Filmes com gêneros encontrados: {filmes_com_generos}")
    print(f"Filmes SEM gêneros: {len(df_limpos) - filmes_com_generos}")
    print(f"Total de relações movie_genres: {total_relacoes}")
    print(f"Arquivo SQL gerado: {arquivo_sql_saida}")

    # Salvar também lista de filmes sem gêneros em arquivo separado
    if len(df_limpos) - filmes_com_generos > 0:
        filmes_sem_generos_final = df_merged[df_merged["genre_ids"].apply(len) == 0]
        filmes_sem_generos_com_titulo = filmes_sem_generos_final.merge(
            df_limpos[["id", "title"]], on="id", how="left"
        )

        arquivo_sem_generos = "filmes_sem_generos.csv"
        filmes_sem_generos_com_titulo[["id", "title", coluna_generos]].to_csv(
            arquivo_sem_generos, index=False, encoding="utf-8"
        )
        print(f"Lista de filmes sem gêneros salva em: {arquivo_sem_generos}")

    return df_merged


def analisar_generos_originais(dataset_original):
    """
    Função auxiliar para analisar como os gêneros estão estruturados no dataset original
    """
    print("=== ANÁLISE DOS GÊNEROS NO DATASET ORIGINAL ===")

    df = pd.read_csv(dataset_original)

    if "genres" in df.columns:
        print(f"Coluna 'genres' encontrada!")
        print(f"Tipo: {df['genres'].dtype}")
        print(f"Valores não nulos: {df['genres'].notna().sum()}")
        print(f"Exemplos de gêneros:")

        # Mostrar alguns exemplos não nulos
        exemplos = df["genres"].dropna().head(5)
        for i, exemplo in enumerate(exemplos, 1):
            print(f"  {i}: {exemplo}")

        # Analisar todos os gêneros únicos
        todos_generos = set()
        for generos_str in df["genres"].dropna():
            if isinstance(generos_str, str):
                generos_lista = [g.strip() for g in generos_str.split(",")]
                todos_generos.update(generos_lista)

        print(f"\nTodos os gêneros únicos encontrados ({len(todos_generos)}):")
        for genero in sorted(todos_generos):
            print(f"  - {genero}")
    else:
        print("ERRO: Coluna 'genres' não encontrada!")
        print(f"Colunas disponíveis: {list(df.columns)}")

    return ["genres"] if "genres" in df.columns else []


# ===== EXECUÇÃO PRINCIPAL =====
if __name__ == "__main__":
    # Configurações dos arquivos
    dataset_original = "TMDB_movie_dataset_v11.csv"  # Dataset original com gêneros
    filmes_limpos = "movies_limpo.csv"  # Seus filmes limpos
    arquivo_sql_saida = "movie_genres_inserts.sql"  # SQL gerado

    try:
        # Primeiro, analisar estrutura dos gêneros (opcional)
        print("Analisando estrutura dos gêneros no dataset original...")
        analisar_generos_originais(dataset_original)

        print("\n" + "=" * 60)

        # Executar extração principal
        df_resultado = extrair_generos_filmes(
            dataset_original, filmes_limpos, arquivo_sql_saida
        )

        print("\n" + "=" * 60)
        print("PROCESSO CONCLUÍDO!")
        print(f"Execute o arquivo '{arquivo_sql_saida}' no seu banco PostgreSQL")
        print("=" * 60)

    except FileNotFoundError as e:
        print(f"Erro: Arquivo não encontrado - {e}")
        print("Certifique-se de que os arquivos estão no diretório correto")
    except Exception as e:
        print(f"Erro durante execução: {e}")
        import traceback

        traceback.print_exc()
