# Dados: catálogo e treino inicial

O repositório não carrega o catálogo de filmes nem os ratings: são arquivos grandes e de
terceiros. Este documento descreve como o banco foi populado e como o modelo inicial foi
treinado, para que o caminho seja reproduzível.

## Fontes

| Fonte | O que veio de lá |
| --- | --- |
| [TMDB](https://developer.themoviedb.org/docs) | catálogo: título, sinopse, gêneros, popularidade, pôster |
| [MovieLens](https://grouplens.org/datasets/movielens/) | ratings históricos, para dar base ao modelo colaborativo |

A ligação entre os dois é o `links.csv` do MovieLens, que traz o `tmdbId` de cada filme. Por
isso as chaves primárias de `movies` e `genres` são os ids do TMDB.

## Ordem dos scripts

O feed funciona com o banco vazio de filmes? Não: `POST /api/feed` responde 400 quando não há
filmes. Então o primeiro passo depois de subir o Compose é popular o catálogo.

1. **Filtro de qualidade do catálogo.** `database/scripts/filter_movies.py` corta o CSV do TMDB
   por idioma, ano, popularidade e presença de sinopse e pôster, e escreve um relatório do que
   foi descartado.
2. **Complemento pela API.** `database/scripts/update_popularity.py` e
   `database/scripts/createGenres.py` completam popularidade e gêneros faltantes consultando o
   TMDB.
3. **Importação.** `database/scripts/importDataset.py` carrega o CSV final no Postgres com
   `execute_values`.
4. **Ratings.** `ai/1preprocess.py` lê os arquivos do MovieLens e mapeia `movieId` para
   `tmdbId`; `ai/2preprocess.py` descarta os ratings de filmes que não entraram no catálogo.
5. **Treino inicial.** `ai/3pretrain.py` constrói o `HybridRecommender` e grava
   `ai/model_data/hybrid_model.pkl`, que é o arquivo que a API de inferência carrega ao subir.

Sem o passo 5 o serviço sobe do mesmo jeito: ele detecta a ausência do modelo, treina a partir
do que existir no banco e segue com o lado de conteúdo até haver avaliações suficientes.

## Limitação dos scripts

Os cinco scripts foram escritos durante a carga inicial e ficaram com caminhos e credenciais
fixos no código (`movies_limpo.csv`, `./output/`, usuário e senha `cinematch` em
`importDataset.py`). Eles resolvem o problema uma vez, não são uma pipeline parametrizada. Se
o projeto tivesse continuado, esse seria um dos primeiros pontos a arrumar: um único comando,
lendo caminho e conexão do ambiente.
