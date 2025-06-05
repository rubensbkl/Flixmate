import re
import pandas as pd
from typing import Optional


# ====================
# Constantes
# ====================

GENEROS_VALIDOS = {
    'Action', 'Adventure', 'Animation', 'Comedy', 'Crime',
    'Drama', 'Family', 'Fantasy', 'History',
    'Horror', 'Music', 'Mystery', 'Romance', 'Science Fiction',
    'Thriller', 'War', 'Western'
}

IDIOMAS_VALIDOS = {"en", "pt", "es", "fr", "it", "de"}

PALAVRAS_SUSPEITAS_TITULO = {
    "xxx", "porn", "sex tape", "hentai", "amateur", "webcam", "cam girl",
    "gangbang", "bukkake", "creampie", "big tits", "big ass", "milf porn",
    "teen porn", "barely legal", "hardcore", "softcore"
}

VOTE_AVERAGE_MIN = 4.0
POPULARITY_MIN = 15.0


# ====================
# Funções auxiliares
# ====================

def titulo_valido(titulo: Optional[str]) -> bool:
    if pd.isna(titulo):
        return False

    titulo = str(titulo).strip().lower()

    if not (2 <= len(titulo) <= 250):
        return False

    if re.fullmatch(r"[\d\W]+", titulo):
        return False

    if re.search(r"[^\w\s]{3,}", titulo):
        return False

    if any(p in titulo for p in PALAVRAS_SUSPEITAS_TITULO):
        return False

    return True


def tem_genero_valido(generos_str: Optional[str]) -> bool:
    if pd.isna(generos_str) or not generos_str.strip():
        return False

    generos = {g.strip() for g in generos_str.split(",")}
    return bool(generos & GENEROS_VALIDOS)


def data_valida(data: Optional[str]) -> bool:
    if pd.isna(data):
        return False

    try:
        ano = int(str(data)[:4])
        return 1950 <= ano <= 2025
    except (ValueError, TypeError):
        return False


# ====================
# Função principal
# ====================

def limpar_base_tmdb(entrada: str, saida: str) -> pd.DataFrame:
    print(f"\n📥 Carregando dados de '{entrada}'...")
    df = pd.read_csv(entrada)
    print(f"📊 Filmes na base original: {len(df)}")

    print("\n🔍 Iniciando limpeza...")

    # Ordenar por popularidade para priorizar filmes mais relevantes
    df = df.sort_values(by="popularity", ascending=False).drop_duplicates(subset="id")
    print(f"✅ Remoção de duplicatas por ID: {len(df)}")

    # Aplicação dos filtros
    filtros = (
        df["title"].apply(titulo_valido) &
        df["vote_average"].ge(VOTE_AVERAGE_MIN) &
        df["popularity"].ge(POPULARITY_MIN) &
        df["release_date"].apply(data_valida) &
        df["original_language"].isin(IDIOMAS_VALIDOS) &
        (df["adult"] == False) &
        df["genres"].apply(tem_genero_valido)
    )
    df = df[filtros]
    print(f"✅ Dados após aplicar todos os filtros: {len(df)}")

    # Remoção de dados críticos faltantes
    colunas_criticas = [
        "id", "title", "overview", "vote_average",
        "release_date", "original_language",
        "popularity", "poster_path", "backdrop_path"
    ]
    df = df.dropna(subset=colunas_criticas)
    print(f"✅ Dados após remoção de campos críticos faltantes: {len(df)}")

    # Seleção e ordenação final
    df_final = (
        df[colunas_criticas]
        .rename(columns={"vote_average": "rating"})
        .sort_values(by=["popularity", "rating"], ascending=[False, False])
        .reset_index(drop=True)
    )

    # Salvamento
    df_final.to_csv(saida, index=False)
    print(f"\n💾 Dados limpos salvos em '{saida}'")
    print(f"🎬 Total final de filmes: {len(df_final)}")

    # Relatório final
    print("\n📑 RELATÓRIO FINAL")
    print(f"⭐ Rating médio: {df_final['rating'].mean():.2f}")
    print(f"🔥 Popularidade média: {df_final['popularity'].mean():.2f}")
    print("\n🗣️ Idiomas mais comuns:")
    print(df_final['original_language'].value_counts().head(10))

    return df_final


# ====================
# Análise rápida
# ====================

def analise_rapida(arquivo: str) -> None:
    df = pd.read_csv(arquivo)

    print(f"\n📊 Analisando '{arquivo}'...")
    print(f"Total de filmes: {len(df)}")
    print(f"Colunas disponíveis: {list(df.columns)}")

    print("\n🚩 Dados faltantes por coluna:")
    faltantes = df.isnull().sum()
    for col, val in faltantes.items():
        if val > 0:
            print(f"  • {col}: {val} ({val/len(df)*100:.1f}%)")

    rating_col = "vote_average" if "vote_average" in df.columns else "rating"
    print(f"\n⭐ Faixa de rating: {df[rating_col].min():.1f} - {df[rating_col].max():.1f}")
    print(f"🔥 Faixa de popularidade: {df['popularity'].min():.1f} - {df['popularity'].max():.1f}")

    duplicatas = df.duplicated(subset=["title"]).sum()
    print(f"📄 Títulos duplicados: {duplicatas}")

    if "adult" in df.columns:
        adultos = df["adult"].sum()
        print(f"🔞 Filmes adultos: {adultos}")

    if "genres" in df.columns:
        sem_genero = df["genres"].isnull().sum() + (df["genres"] == "").sum()
        print(f"🎭 Filmes sem gêneros: {sem_genero}")


# ====================
# Execução principal
# ====================

if __name__ == "__main__":
    entrada = "TMDB_movie_dataset_v11.csv"
    saida = "movies_limpo.csv"

    try:
        print("\n🚀 ANÁLISE INICIAL")
        analise_rapida(entrada)

        print("\n" + "=" * 60)
        print("🧹 INICIANDO LIMPEZA DOS DADOS")
        print("=" * 60)

        limpar_base_tmdb(entrada, saida)

        print("\n" + "=" * 60)
        print("🏁 LIMPEZA CONCLUÍDA COM SUCESSO!")
        print("=" * 60)

    except FileNotFoundError:
        print(f"❌ Erro: Arquivo '{entrada}' não encontrado!")
    except Exception as e:
        print(f"❌ Erro durante a execução: {type(e).__name__}: {e}")
