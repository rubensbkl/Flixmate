# API

Base local: `http://localhost:6789`. Em produção o Nginx serve as mesmas rotas sob
`https://dominio/api/`.

Todas as respostas são JSON. Erros seguem o formato `{"error": "mensagem"}`.

## Autenticação

O login devolve um JWT assinado com HMAC256. Ele vai em toda requisição seguinte:

```http
Authorization: Bearer <token>
```

O middleware `before("/api/*")` valida o token, injeta `userId` e `userEmail` na requisição e
responde 401 quando o header falta ou 403 quando o token não confere. São públicas apenas
`POST /api/login`, `POST /api/register`, `GET /api/verify` e `GET /api/ping`.

## Sessão

### `POST /api/login`

```json
{ "email": "admin@admin.com", "password": "senha" }
```

200:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": { "firstName": "Admin", "lastName": "Admin", "email": "admin@admin.com" }
}
```

401 quando as credenciais não batem.

### `POST /api/register`

```json
{
  "firstName": "Bernardo",
  "lastName": "Rocha",
  "email": "b@exemplo.com",
  "password": "senha",
  "gender": "M",
  "genres": [28, 878, 53]
}
```

201 na criação. 400 quando falta campo, quando o email já existe ou quando nenhum gênero
favorito foi escolhido. A senha é gravada com bcrypt, nunca em texto puro.

### `GET /api/verify`

Confere o token do header e devolve `{"valid": true, "user": {...}}` ou 401 com
`{"valid": false}`.

### `GET /api/ping`

`{"status": "ok", "message": "pong"}`. Sem autenticação, serve para healthcheck.

## Feed e avaliação

### `POST /api/feed`

```json
{ "page": 1 }
```

Sorteia 500 candidatos entre os filmes do banco, pede ao serviço de IA os 20 melhores, busca
os detalhes no TMDB, mistura com os populares da página pedida e devolve
`{"status": "ok", "movies": [...]}` embaralhado. O sorteio não guarda histórico, então um
filme pode reaparecer entre páginas.

### `POST /api/rate`

```json
{ "movieId": 27205, "rating": true }
```

`rating` é booleano: `true` para like, `false` para dislike. Se o filme ainda não existe no
banco, ele é buscado no TMDB e inserido junto com os gêneros antes de a avaliação ser gravada.

200:

```json
{ "success": true, "operation": "CREATE", "currentRating": true, "message": "Rating criado" }
```

`operation` é `CREATE`, `UPDATE` ou `IGNORED` (mesma avaliação enviada de novo). O treino do
modelo só dispara em `CREATE` e `UPDATE`.

### `GET /api/rate/:movieId`

Devolve a avaliação do usuário logado para o filme, ou 404 se não existir.

### `DELETE /api/rate/:movieId`

Apaga a avaliação. 200 na remoção, 404 se não havia avaliação.

## Recomendações

### `GET /api/recommendation`

Gera uma recomendação nova. Diferente do feed, exclui dos candidatos tudo que já foi
recomendado para o usuário. 404 quando não sobra filme elegível.

### `GET /api/recommendations/:userId`

Lista as recomendações já gravadas, cada uma com `id`, `title`, `poster_path`,
`release_date`, `genres` e `score`.

### `POST /api/recommendation/delete`

`{"movieId": 27205}`. Remove a recomendação da lista do usuário.

### `POST /api/recommendation/watched`

`{"movieId": 27205, "watched": true}`. Liga ou desliga o filme na watchlist.

### `POST /api/recommendation/favorite`

`{"movieId": 27205, "favorite": true}`. Liga ou desliga o filme nos favoritos.

### `GET /api/movie/:movieId/watchlist`, `GET /api/movie/:movieId/favorite`, `GET /api/movie/:movieId/recommended`

Consultas booleanas de estado do filme para o usuário logado.

### `GET /api/movie/:movieId/details`

Dados do filme mais o estado dele para o usuário: avaliação, favorito e watchlist.

## Perfil

### `GET /api/private`

Dados completos do usuário logado, incluindo os gêneros preferidos.

### `GET /api/profile/:userId`

Dados públicos de um perfil.

### `GET /api/profile/:userId/watchlist`, `/favorites`, `/recommended`

As três listas de filmes de um perfil, no mesmo formato do feed.

### `POST /api/profile/update`

```json
{ "firstName": "Bernardo", "email": "novo@exemplo.com", "genres": [28, 878] }
```

Todos os campos são opcionais menos a regra dos gêneros: quando `genres` vem, precisa ter de 1
a 5 itens. A atualização de dados e a de gêneros são validadas juntas antes de qualquer
escrita.

### `GET /api/users`

Lista de usuários, usada pela busca de perfis.

## Busca

### `GET /api/movies/search`

| Parâmetro | Tipo | Descrição |
| --- | --- | --- |
| `query` | string | texto no título |
| `genres` | ids separados por vírgula | filtro por gênero |
| `yearFrom`, `yearTo` | ano | intervalo de lançamento |
| `sortBy` | string | ordenação |
| `page`, `limit` | inteiro | paginação |

### `GET /api/profiles/search`

`query`, `page` e `limit`. Busca por nome ou email.

## Serviço de IA

Não é exposto pelo Nginx: só o backend fala com ele, pela rede interna do Compose. Em
desenvolvimento fica em `http://localhost:5005` e publica o Swagger em `/docs`.

| Rota | Corpo | Resposta |
| --- | --- | --- |
| `POST /train` | `{"ratings": [{"user", "movie", "rating"}]}` | mensagem, versão do cache, avaliações processadas |
| `POST /recommend` | `{"user", "candidate_ids", "top_n"}` | `recommended_movie`, `score`, `cache_used` |
| `POST /feed` | `{"user", "top_n", "candidate_ids"}` | `all_recommendations`: pares (filme, score) |
| `GET /health` | | estado do modelo, do Redis e do último treino |
| `POST /admin/reload_model` | | recarrega o modelo do disco |
