import re

import numpy as np
import pandas as pd


def limpar_base_tmdb(arquivo_entrada, arquivo_saida):
    """
    Função para filtrar e limpar base de dados do TMDB
    """
    print("Carregando dados...")
    df = pd.read_csv(arquivo_entrada)
    print(f"Dados originais: {len(df)} filmes")

    # 1. Remover filmes pornográficos
    df = df[df["adult"] == False]
    print(f"Após remover filmes pornográficos: {len(df)} filmes")

    # 2. Remover filmes sem título ou com título vazio/nulo
    df = df.dropna(subset=["title"])
    df = df[df["title"].str.strip() != ""]
    print(f"Após remover filmes sem título: {len(df)} filmes")

    # 3. Remover filmes sem poster
    df = df.dropna(subset=["poster_path"])
    df = df[df["poster_path"].str.strip() != ""]
    print(f"Após remover filmes sem poster: {len(df)} filmes")

    # 4. Remover filmes sem overview (descrição)
    df = df.dropna(subset=["overview"])
    df = df[df["overview"].str.strip() != ""]
    print(f"Após remover filmes sem descrição: {len(df)} filmes")

    # 5. Remover filmes sem gêneros
    def tem_generos_validos(generos_str):
        if pd.isna(generos_str) or str(generos_str).strip() == "":
            return False
        
        # Lista de gêneros válidos em inglês (do TMDB)
        generos_validos = {
            'Action', 'Adventure', 'Animation', 'Comedy', 'Crime', 
            'Documentary', 'Drama', 'Family', 'Fantasy', 'History', 
            'Horror', 'Music', 'Mystery', 'Romance', 'Science Fiction', 
            'TV Movie', 'Thriller', 'War', 'Western'
        }
        
        try:
            # Gêneros vêm como string separada por vírgula
            generos_lista = [g.strip() for g in str(generos_str).split(',')]
            
            # Verificar se pelo menos um gênero é válido
            for genero in generos_lista:
                if genero in generos_validos:
                    return True
            
            return False
        except:
            return False

    df_antes_generos = len(df)
    df = df[df["genres"].apply(tem_generos_validos)]
    print(f"Após remover filmes sem gêneros válidos: {len(df)} filmes")
    print(f"  -> Removidos: {df_antes_generos - len(df)} filmes sem gêneros")

    # 6. Manter todos os idiomas (comentado para permitir diversidade)
    # idiomas_permitidos = [...]
    # df = df[df['original_language'].isin(idiomas_permitidos)]
    print(f"Mantendo todos os idiomas: {len(df)} filmes")

    # 7. Filtrar por rating mínimo (mais flexível)
    df = df[df["vote_average"] >= 2.0]  # Corrigido: vote_average em vez de rating
    print(f"Após filtrar rating mínimo (2.0): {len(df)} filmes")

    # 8. Filtrar por popularidade mínima (mais flexível)
    df = df[df["popularity"] >= 3.0]
    print(f"Após filtrar popularidade mínima (3.0): {len(df)} filmes")

    # 9. Remover títulos suspeitos/bizarros
    def titulo_valido(titulo):
        if pd.isna(titulo):
            return False

        titulo = str(titulo).strip()

        # Muito curto ou muito longo
        if len(titulo) < 2 or len(titulo) > 250:
            return False

        # Só números ou caracteres especiais
        if re.match(r"^[\d\W]+$", titulo):
            return False

        # Muitos caracteres especiais consecutivos
        if re.search(r"[^\w\s]{3,}", titulo):
            return False

        # Palavras claramente pornográficas
        palavras_suspeitas = [
            "xxx",
            "porn",
            "sex tape",
            "hentai",
            "amateur",
            "webcam",
            "cam girl",
            "gangbang",
            "bukkake",
            "creampie",
            "big tits",
            "big ass",
            "milf porn",
            "teen porn",
            "barely legal",
            "hardcore",
            "softcore",
        ]

        titulo_lower = titulo.lower()
        for palavra in palavras_suspeitas:
            if palavra in titulo_lower:
                return False

        return True

    df = df[df["title"].apply(titulo_valido)]
    print(f"Após filtrar títulos suspeitos: {len(df)} filmes")

    # 10. Remover duplicatas por título (manter o mais popular)
    print("Removendo duplicatas...")
    df_duplicatas_antes = len(df)
    df = df.sort_values("popularity", ascending=False)
    df = df.drop_duplicates(subset=["title"], keep="first")
    print(f"Duplicatas removidas: {df_duplicatas_antes - len(df)}")
    print(f"Após remover duplicatas: {len(df)} filmes")

    # 11. Validar e limpar datas
    def validar_data(data):
        if pd.isna(data):
            return False
        try:
            ano = int(str(data)[:4])
            return 1900 <= ano <= 2025
        except:
            return False

    df = df[df["release_date"].apply(validar_data)]
    print(f"Após validar datas: {len(df)} filmes")

    # 12. Remover linhas com dados faltantes críticos
    df = df.dropna(subset=["id", "title", "vote_average", "release_date"])
    print(f"Após remover dados faltantes críticos: {len(df)} filmes")

    # 13. Selecionar e renomear as colunas para o formato final
    df_final = df[[
        'id',
        'title',
        'overview',
        'vote_average',
        'release_date',
        'original_language',
        'popularity',
        'poster_path',
        'backdrop_path'
    ]].rename(columns={
        'vote_average': 'rating'
    })

    # 14. Ordenar por popularidade (sem resetar índice para manter IDs originais)
    df_final = df_final.sort_values(["popularity", "rating"], ascending=[False, False])

    # 15. Salvar resultado
    df_final.to_csv(arquivo_saida, index=False)
    print(f"\nDados limpos salvos em '{arquivo_saida}'")
    print(f"Total final: {len(df_final)} filmes")

    # 16. Relatório estatístico
    print("\n=== RELATÓRIO FINAL ===")
    print(f"Rating médio: {df_final['rating'].mean():.2f}")
    print(f"Popularidade média: {df_final['popularity'].mean():.2f}")
    print(f"Idiomas mais comuns:")
    print(df_final["original_language"].value_counts().head(10))

    return df_final

def analise_rapida(arquivo):
    """
    Função para fazer uma análise rápida dos dados
    """
    df = pd.read_csv(arquivo)

    print("\n=== ANÁLISE DOS DADOS ===")
    print(f"Total de filmes: {len(df)}")
    print(f"Colunas: {list(df.columns)}")
    print(f"\nDados faltantes por coluna:")
    for col in df.columns:
        faltantes = df[col].isnull().sum()
        if faltantes > 0:
            print(f"  {col}: {faltantes} ({faltantes/len(df)*100:.1f}%)")

    print(f"\nFaixa de rating: {df['vote_average'].min():.1f} - {df['vote_average'].max():.1f}")
    print(f"Faixa de popularidade: {df['popularity'].min():.1f} - {df['popularity'].max():.1f}")

    # Verificar duplicatas
    duplicatas = df.duplicated(subset=["title"]).sum()
    print(f"Títulos duplicados: {duplicatas}")

    # Verificar filmes adultos
    adultos = df["adult"].sum() if "adult" in df.columns else 0
    print(f"Filmes adultos: {adultos}")

    # Verificar filmes sem gêneros
    if "genres" in df.columns:
        sem_generos = df["genres"].isnull().sum()
        generos_vazios = (df["genres"] == "").sum()
        print(f"Filmes sem gêneros: {sem_generos + generos_vazios}")

# ===== EXECUÇÃO PRINCIPAL =====
if __name__ == "__main__":
    # Configurações
    arquivo_entrada = "TMDB_movie_dataset_v11.csv"  # Dataset original completo
    arquivo_saida = "movies_limpo.csv"  # Arquivo final limpo

    try:
        # Análise inicial (opcional)
        print("=== ANÁLISE INICIAL ===")
        analise_rapida(arquivo_entrada)

        print("\n" + "=" * 50)
        print("INICIANDO LIMPEZA DOS DADOS")
        print("=" * 50)

        # Executar limpeza
        df_limpo = limpar_base_tmdb(arquivo_entrada, arquivo_saida)

        print("\n" + "=" * 50)
        print("LIMPEZA CONCLUÍDA COM SUCESSO!")
        print("=" * 50)

    except FileNotFoundError:
        print(f"Erro: Arquivo '{arquivo_entrada}' não encontrado!")
        print("Certifique-se de que o arquivo está no mesmo diretório do script.")
    except Exception as e:
        print(f"Erro durante a execução: {str(e)}")