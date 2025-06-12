package app;

import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dao.FavoriteDAO;
import dao.FeedbackDAO;
import dao.GenreDAO;
import dao.MovieDAO;
import dao.MovieGenreDAO;
import dao.RecommendationDAO;
import dao.UserDAO;
import dao.UserGenreDAO;
import dao.WatchLaterDAO;
import model.Favorite;
import model.Feedback;
import model.Genre;
import model.Movie;
import model.Recommendation;
import model.User;
import model.WatchLater;
import service.FavoriteService;
import service.FeedbackService;
import service.GenreService;
import service.MovieGenreService;
import service.MovieService;
import service.RecommendationService;
import service.UserGenreService;
import service.UserService;
import service.WatchLaterService;
import util.FlixAi;
import util.JWTUtil;
import util.TMDBUtil;

public class Application {

    /**
     * Método para validar as variáveis de ambiente necessárias.
     * 
     * @throws IllegalStateException se alguma variável de ambiente obrigatória não
     *                               estiver definida
     */
    public static void validateRequiredEnvs() {
        String[] requiredEnvs = {
                "ENV",
                "PORT",
                "JWT_SECRET",
                "TMDB_API_KEY",
                "DB_HOST",
                "DB_PORT",
                "DB_NAME",
                "DB_USER",
                "DB_PASSWORD"
        };

        for (String env : requiredEnvs) {
            String value = System.getenv(env);
            if (value == null || value.isEmpty()) {
                throw new IllegalStateException("Missing required environment variable: " + env);
            }
        }

        System.out.println("All required environment variables are set.");
    }

    /**
     * Método principal que inicia o servidor e configura os endpoints.
     * 
     * @param args argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {

        // ===========================
        // ======= INICIALIZAÇÃO ======
        // ===========================

        // Valida as variáveis de ambiente necessárias
        validateRequiredEnvs();

        // Pegar todas as variáveis de ambiente respectivamente
        String env = System.getenv("ENV");
        // Pegar a porta do servidor
        int porta = Integer.parseInt(System.getenv("PORT"));
        // Envs de autenticação
        String jwtSecret = System.getenv("JWT_SECRET");
        String tmdbApiKey = System.getenv("TMDB_API_KEY");
        // Envs de Banco
        String dbHost = System.getenv("DB_HOST");
        String dbName = System.getenv("DB_NAME");
        int dbPort = Integer.parseInt(System.getenv("DB_PORT"));
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        // Libs
        Gson gson = new Gson();

        // DAOs
        FeedbackDAO feedbackDAO = new FeedbackDAO(dbHost, dbName, dbPort, dbUser, dbPassword);
        MovieDAO movieDAO = new MovieDAO(dbHost, dbName, dbPort, dbUser, dbPassword);
        MovieGenreDAO movieGenreDAO = new MovieGenreDAO(dbHost, dbName, dbPort, dbUser, dbPassword);
        RecommendationDAO recommendationDAO = new RecommendationDAO(dbHost, dbName, dbPort, dbUser, dbPassword);
        UserDAO userDAO = new UserDAO(dbHost, dbName, dbPort, dbUser, dbPassword);
        UserGenreDAO userGenreDAO = new UserGenreDAO(dbHost, dbName, dbPort, dbUser, dbPassword);
        GenreDAO genreDAO = new GenreDAO(dbHost, dbName, dbPort, dbUser, dbPassword);
        WatchLaterDAO watchLaterDAO = new WatchLaterDAO(dbHost, dbName, dbPort, dbUser, dbPassword);
        FavoriteDAO favoriteDAO = new FavoriteDAO(dbHost, dbName, dbPort, dbUser, dbPassword);

        TMDBUtil tmdb = new TMDBUtil(tmdbApiKey);
        JWTUtil jwt = new JWTUtil(jwtSecret);
        FlixAi flixAi = new FlixAi();

        // Services
        MovieGenreService movieGenreService = new MovieGenreService(movieGenreDAO);
        RecommendationService recommendationService = new RecommendationService(recommendationDAO, tmdb);
        MovieService movieService = new MovieService(movieDAO, movieGenreService, tmdb);
        FeedbackService feedbackService = new FeedbackService(feedbackDAO, movieService);
        UserGenreService userGenreService = new UserGenreService(userGenreDAO);
        UserService userService = new UserService(userDAO);
        GenreService genreService = new GenreService(genreDAO);
        WatchLaterService watchLaterService = new WatchLaterService(watchLaterDAO);
        FavoriteService favoriteService = new FavoriteService(favoriteDAO);

        // Configurar a porta do servidor
        port(porta);

        // Static files e webjars
        staticFiles.location("/public");
        staticFiles.externalLocation("webjars");

        // ==========================
        // ======= CORS =============
        // ==========================

        // CORS - Cross-Origin Resource Sharing
        Set<String> allowedOrigins = new HashSet<>(List.of(
                "http://localhost:3000",
                "127.0.0.1:3000",
                "https://flixmate.com.br"));

        // Habilitar CORS para requisições preflight OPTIONS
        options("/*", (req, res) -> {
            String origin = req.headers("Origin");
            if (origin != null && allowedOrigins.contains(origin)) {
                res.header("Access-Control-Allow-Origin", origin);
            }

            String acrh = req.headers("Access-Control-Request-Headers");
            if (acrh != null) {
                res.header("Access-Control-Allow-Headers", acrh);
            }

            String acrm = req.headers("Access-Control-Request-Method");
            if (acrm != null) {
                res.header("Access-Control-Allow-Methods", acrm);
            }

            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Max-Age", "86400"); // 24 horas - ajuda a reduzir preflight requests

            res.status(200);
            return "OK";
        });

        // CORS para todas as outras requisições
        before((req, res) -> {
            // Ignorar esta parte para requisições OPTIONS, já tratadas acima
            if (req.requestMethod().equals("OPTIONS")) {
                return;
            }

            String origin = req.headers("Origin");
            if (origin != null && allowedOrigins.contains(origin)) {
                res.header("Access-Control-Allow-Origin", origin);
            }

            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept");
            res.type("application/json");
        });

        // Middleware de autenticação para rotas protegidas
        before("/api/*", (req, res) -> {
            // Ignorar verificação para requisições OPTIONS
            if (req.requestMethod().equals("OPTIONS")) {
                return;
            }

            // Excluir rotas públicas
            String path = req.pathInfo();
            if (path.equals("/api/login") || path.equals("/api/register") || path.equals("/api/verify")
                    || path.equals("/api/ping")) {
                return; // Ignorar verificação para rotas públicas
            }

            String authHeader = req.headers("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                halt(401, gson.toJson(Map.of("error", "Token não fornecido")));
            }

            try {
                String token = authHeader.substring(7);
                var decoded = jwt.verifyToken(token);

                if (decoded == null) {
                    halt(401, gson.toJson(Map.of("error", "Token inválido")));
                }

                // Adicionar atributos úteis à requisição
                req.attribute("userEmail", decoded.getSubject());
                req.attribute("userId", decoded.getClaim("userId").asInt());
            } catch (Exception e) {
                e.printStackTrace();
                halt(403, gson.toJson(Map.of("error", "Token inválido")));
            }
        });

        // ==========================
        // ======= LOGIN ============
        // ==========================

        post("/api/login", (req, res) -> {
            User user = gson.fromJson(req.body(), User.class);
            if (userService.authenticateUser(user.getEmail(), user.getPassword())) {
                // Buscar os dados completos do usuário
                User fullUser = userService.getUserByEmail(user.getEmail());

                // Gerar token JWT
                String token = jwt.generateToken(user.getEmail(), fullUser.getId());

                // Criar resposta com token e dados básicos do usuário (sem senha)
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("user", Map.of(
                        "firstName", fullUser.getFirstName(),
                        "lastName", fullUser.getLastName(),
                        "email", fullUser.getEmail()));

                return gson.toJson(response);
            }
            res.status(401);
            return gson.toJson(Map.of("error", "Credenciais inválidas"));
        });

        post("/api/register", (req, res) -> {
            JsonObject requestBody = JsonParser.parseString(req.body()).getAsJsonObject();

            // Extrai dados do usuário
            User user = new User();
            user.setFirstName(requestBody.get("firstName").getAsString());
            user.setLastName(requestBody.get("lastName").getAsString());
            user.setEmail(requestBody.get("email").getAsString());
            user.setPassword(requestBody.get("password").getAsString());
            user.setGender(requestBody.get("gender").getAsString().charAt(0));

            // Verificações do usuário
            if (user.getEmail() == null || user.getPassword() == null ||
                    user.getFirstName() == null || user.getLastName() == null ||
                    user.getGender() == '\0') {
                res.status(400);
                return gson.toJson(Map.of("error", "Todos os campos são obrigatórios"));
            }

            if (userService.getUserByEmail(user.getEmail()) != null) {
                res.status(400);
                return gson.toJson(Map.of("error", "Email já cadastrado"));
            }

            // Extrair dados dos gêneros favoritos
            JsonArray favoriteGenresArray = requestBody.getAsJsonArray("favoriteGenres");
            List<Integer> favoriteGenres = new ArrayList<>();
            for (int i = 0; i < favoriteGenresArray.size(); i++) {
                favoriteGenres.add(favoriteGenresArray.get(i).getAsInt());
            }

            // Verificações dos gêneros favoritos
            if (favoriteGenres.isEmpty()) {
                res.status(400);
                return gson.toJson(Map.of("error", "Selecione pelo menos um gênero favorito"));
            }

            // Inserir usuário no banco
            if (userService.insertUser(user)) {
                // Buscar o usuário recém-inserido para obter o ID
                User fullUser = userService.getUserByEmail(user.getEmail());

                // Inserir os gêneros favoritos
                boolean allGenresInserted = true;
                for (Integer genreId : favoriteGenres) {
                    if (!userGenreService.addPreferredGenre(fullUser.getId(), genreId)) {
                        allGenresInserted = false;
                        System.err.println(
                                "Erro ao inserir gênero favorito: " + genreId + " para o usuário: " + fullUser.getId());
                    }
                }

                // Gerar token JWT
                String token = jwt.generateToken(fullUser.getEmail(), fullUser.getId());

                // Criar resposta
                Map<String, Object> userData = Map.of(
                        "firstName", fullUser.getFirstName(),
                        "lastName", fullUser.getLastName(),
                        "email", fullUser.getEmail(),
                        "gender", fullUser.getGender());

                res.status(201);

                if (!allGenresInserted) {
                    // Se algum gênero não foi inserido, ainda retornamos sucesso mas com aviso
                    return gson.toJson(Map.of(
                            "status", "success",
                            "token", token,
                            "user", userData,
                            "warning", "Alguns gêneros favoritos podem não ter sido salvos"));
                } else {
                    return gson.toJson(Map.of(
                            "status", "success",
                            "token", token,
                            "user", userData));
                }
            }

            res.status(500);
            return gson.toJson(Map.of("error", "Erro ao criar conta"));
        });

        get("/api/verify", (req, res) -> {
            String authHeader = req.headers("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                res.status(401);
                return gson.toJson(Map.of("valid", false, "error", "Token não fornecido"));
            }

            try {
                String token = authHeader.substring(7);
                var decoded = jwt.verifyToken(token);
                int userId = decoded.getClaim("userId").asInt();

                // Buscar informações do usuário
                User user = userService.getUserById(userId);
                if (user == null) {
                    res.status(401);
                    return gson.toJson(Map.of("valid", false, "error", "Usuário não encontrado"));
                }

                Map<String, Object> userData = Map.of(
                        "id", user.getId(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "email", user.getEmail());

                return gson.toJson(Map.of("valid", true, "user", userData));
            } catch (Exception e) {
                res.status(401);
                return gson.toJson(Map.of("valid", false, "error", "Token inválido"));
            }
        });

        get("/api/ping", (req, res) -> gson.toJson(Map.of("status", "ok", "message", "pong")));

        // ==========================
        // ======= RECOMENDAÇÕES ====
        // ==========================

        // Endpoint para deleter filmes recomendados
        post("/api/recommendation/delete", (req, res) -> {
            try {
                int userId = req.attribute("userId");
                JsonObject bodyObj = JsonParser.parseString(req.body()).getAsJsonObject();
                int movieId = bodyObj.get("movieId").getAsInt();

                // Deletar a recomendação do banco de dados
                boolean deleted = recommendationService.deleteRecommendation(userId, movieId);
                System.out.println("Recomendação deletada: " + deleted);
                flixAi.train(userId, movieId, false);
                if (deleted) {
                    return gson.toJson(Map.of("status", "ok", "message", "Recomendação deletada com sucesso"));
                } else {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Erro ao deletar recomendação"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao deletar recomendação"));
            }
        });

        // Endpoint para listar filmes recomendados
        get("/api/recommendations/:userId", (req, res) -> {
            try {
                // Pegar userId dos parâmetros da URL
                int targetUserId = Integer.parseInt(req.params("userId"));

                ArrayList<Recommendation> recommendations = recommendationService
                        .getRecommendationsByUserId(targetUserId);

                if (recommendations.isEmpty()) {
                    return gson.toJson(Map.of("error", "Nenhuma recomendação encontrada"));
                }

                List<Map<String, Object>> moviesData = new ArrayList<>();

                // Buscar cada filme no banco de dados e converter para mapa
                for (Recommendation recommendation : recommendations) {
                    // Pega o ID do filme da recomendação
                    int movieId = recommendation.getMovieId();
                    // Buscar o filme no banco de dados
                    Movie movie = movieService.getMovieById(movieId);

                    // Verifica se o filme existe
                    if (movie == null) {
                        return gson.toJson(Map.of("error", "Filme não encontrado para ID: " + movieId));
                    }

                    List<Integer> movieGenresIds = movieGenreService.getGenreIdsForMovie(movieId);
                    System.out.println("Gêneros do filme: " + movieGenresIds);
                    ArrayList<Genre> movieGenres = new ArrayList<>();
                    for (Integer genreId : movieGenresIds) {
                        Genre genre = genreService.getGenreById(genreId);
                        if (genre != null) {
                            movieGenres.add(genre);
                        }
                    }

                    // Criar mapa com dados do filme incluindo gêneros
                    Map<String, Object> movieData = new HashMap<>();
                    movieData.put("id", movie.getId());
                    movieData.put("title", movie.getTitle());
                    movieData.put("poster_path", movie.getPosterPath());
                    movieData.put("release_date", movie.getReleaseDate());
                    movieData.put("genres", movieGenres.stream()
                            .map(Genre::getName)
                            .toList()); // Adiciona os nomes dos gêneros

                    // Passar se o filme recomendado foi assistido ou favoritado
                    movieData.put("score", recommendation.getScore());

                    System.out.println("Dados do filme: " + movieData);
                    moviesData.add(movieData);
                }

                return gson.toJson(Map.of("status", "ok", "movies", moviesData));
            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(Map.of("error", "ID de usuário inválido"));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao receber recomendações"));
            }
        });

        // Endpoint watchlist
        get("/api/profile/:userId/watchlist", (req, res) -> {
            try {
                int targetUserId = Integer.parseInt(req.params("userId"));

                ArrayList<Integer> movieIds = watchLaterService.getWatchLaterMovies(targetUserId);
                List<Map<String, Object>> moviesData = new ArrayList<>();

                for (Integer movieId : movieIds) {
                    Movie movie = movieService.getMovieById(movieId);
                    if (movie != null) {
                        List<Genre> genres = movieGenreService.buscarGenerosDoFilme(movieId);

                        Map<String, Object> movieData = Map.of(
                                "id", movie.getId(),
                                "title", movie.getTitle(),
                                "poster_path", movie.getPosterPath(),
                                "release_date", movie.getReleaseDate(),
                                "genres", genres.stream().map(Genre::getName).collect(Collectors.toList()));
                        moviesData.add(movieData);
                    }
                }

                return gson.toJson(Map.of("status", "ok", "movies", moviesData));

            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(Map.of("error", "ID de usuário inválido"));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao buscar watchlist: " + e.getMessage()));
            }
        });

        // Endpoint favoritos
        get("/api/profile/:userId/favorites", (req, res) -> {
            try {
                int targetUserId = Integer.parseInt(req.params("userId"));

                ArrayList<Integer> movieIds = favoriteService.getFavoriteMovies(targetUserId);
                List<Map<String, Object>> moviesData = new ArrayList<>();

                for (Integer movieId : movieIds) {
                    Movie movie = movieService.getMovieById(movieId);
                    if (movie != null) {
                        List<Genre> genres = movieGenreService.buscarGenerosDoFilme(movieId);

                        Map<String, Object> movieData = Map.of(
                                "id", movie.getId(),
                                "title", movie.getTitle(),
                                "poster_path", movie.getPosterPath(),
                                "release_date", movie.getReleaseDate(),
                                "genres", genres.stream().map(Genre::getName).collect(Collectors.toList()));
                        moviesData.add(movieData);
                    }
                }

                return gson.toJson(Map.of("status", "ok", "movies", moviesData));

            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(Map.of("error", "ID de usuário inválido"));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao buscar favoritos: " + e.getMessage()));
            }
        });

        // Pega lista de filmes para o usuario avaliar
        post("/api/feed", (req, res) -> {
            int userId = req.attribute("userId");
            JsonObject requestBody = gson.fromJson(req.body(), JsonObject.class);
            int page = requestBody.has("page") ? requestBody.get("page").getAsInt() : 1;

            try {
                // 🔍 Gerar lista de candidatos
                List<Integer> candidatos = movieService.getAllMoviesIds();

                if (candidatos.isEmpty()) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Não há filmes disponíveis para gerar o feed."));
                }

                final int NUM_CANDIDATOS = 500;

                Collections.shuffle(candidatos, new Random());
                candidatos = candidatos.subList(0, Math.min(NUM_CANDIDATOS, candidatos.size()));

                // 🔥 Chamar IA para gerar o feed
                JsonObject aiResponse = flixAi.getFeed(userId, 50, candidatos);
                JsonArray aiMoviesArray = aiResponse.getAsJsonArray("all_recommendations");

                if (aiMoviesArray == null) {
                    throw new RuntimeException("❌ Feed da IA retornou vazio ou inválido.");
                }

                List<Integer> aiMovieIds = new ArrayList<>();
                aiMoviesArray.forEach(item -> {
                    JsonArray pair = item.getAsJsonArray();
                    int movieId = pair.get(0).getAsInt();
                    aiMovieIds.add(movieId);
                });

                // 🔥 Buscar detalhes dos filmes da IA na TMDB
                List<JsonObject> aiMoviesDetails = tmdb.getMoviesDetails(aiMovieIds);

                System.out.println("[🎥] AI movies fetched: total = " + aiMoviesDetails.size());

                // 🔍 Buscar mais filmes do discover
                JsonArray discoverMovies = tmdb.getTopRatedMovies(page);

                System.out.println("[🎥] Discover movies fetched: total = " + discoverMovies.size());

                // 🔗 Combinar IA + discover
                Map<Integer, JsonObject> uniqueMoviesMap = new HashMap<>();

                aiMoviesDetails.forEach(movie -> {
                    int id = movie.get("id").getAsInt();
                    uniqueMoviesMap.put(id, movie);
                });

                discoverMovies.forEach(item -> {
                    JsonObject movie = item.getAsJsonObject();
                    int id = movie.get("id").getAsInt();
                    uniqueMoviesMap.putIfAbsent(id, movie);
                });

                List<JsonObject> finalMovies = new ArrayList<>(uniqueMoviesMap.values());

                System.out.println("[🎥] Movies fetched: total = " + finalMovies.size());

                return gson.toJson(Map.of("status", "ok", "movies", finalMovies));

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao buscar filmes: " + e.getMessage()));
            }
        });

        // Endpoint watchlist toggle
        post("/api/recommendation/watched", (req, res) -> {
            try {
                int userId = req.attribute("userId");
                JsonObject bodyObj = JsonParser.parseString(req.body()).getAsJsonObject();
                int movieId = bodyObj.get("movieId").getAsInt();

                boolean watched = bodyObj.get("watched").getAsBoolean();
                WatchLater watchLater = new WatchLater(userId, movieId);

                boolean success = watchLaterService.toggleWatchLater(watchLater, watched);

                if (success) {
                    boolean currentStatus = watchLaterService.isInWatchLater(watchLater);
                    return gson.toJson(Map.of(
                            "status", "ok",
                            "message", watched ? "Filme adicionado à watchlist" : "Filme removido da watchlist",
                            "currentStatus", currentStatus));
                } else {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Erro ao atualizar watchlist"));
                }

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao processar watchlist: " + e.getMessage()));
            }
        });

        // Endpoint favorite toggle
        post("/api/recommendation/favorite", (req, res) -> {
            try {
                int userId = req.attribute("userId");

                JsonObject bodyObj = JsonParser.parseString(req.body()).getAsJsonObject();
                int movieId = bodyObj.get("movieId").getAsInt();

                boolean status = bodyObj.get("favorite").getAsBoolean();
                Favorite favoriteObj = new Favorite(userId, movieId);

                boolean success = favoriteService.toggleFavorite(favoriteObj, status);

                if (success) {
                    boolean currentStatus = favoriteService.isInFavorites(favoriteObj);
                    flixAi.train(userId, movieId, currentStatus);
                    return gson.toJson(Map.of(
                            "status", "ok",
                            "message", status ? "Filme adicionado aos favoritos" : "Filme removido dos favoritos",
                            "currentStatus", currentStatus));
                } else {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Erro ao atualizar favoritos"));
                }

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao processar favoritos: " + e.getMessage()));
            }
        });

        // Endpoint para verificar status de watchlist
        get("/api/movie/:movieId/watchlist", (req, res) -> {
            try {
                int userId = req.attribute("userId");
                int movieId = Integer.parseInt(req.params("movieId"));

                WatchLater watchLater = new WatchLater(userId, movieId);

                boolean isInWatchlist = watchLaterService.isInWatchLater(watchLater);

                return gson.toJson(Map.of(
                        "status", "ok",
                        "movieId", movieId,
                        "isInWatchlist", isInWatchlist));

            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(Map.of("error", "ID de filme inválido"));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao verificar watchlist: " + e.getMessage()));
            }
        });

        // Endpoint para verificar status de favorito
        get("/api/movie/:movieId/favorite", (req, res) -> {
            try {
                int userId = req.attribute("userId");
                int movieId = Integer.parseInt(req.params("movieId"));

                Favorite favoriteObj = new Favorite(userId, movieId);

                boolean isFavorite = favoriteService.isInFavorites(favoriteObj);

                return gson.toJson(Map.of(
                        "status", "ok",
                        "movieId", movieId,
                        "isFavorite", isFavorite));

            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(Map.of("error", "ID de filme inválido"));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao verificar favorito: " + e.getMessage()));
            }
        });

        // ==========================
        // ======= USER =============
        // ==========================

        // Endpoint para buscar informações do usuário
        get("/api/users", (req, res) -> {
            try {
                int currentUserId = req.attribute("userId");

                // Prevenir SQL injection
                if (currentUserId <= 0) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "ID de usuário inválido"));
                }

                List<User> allUsers = userDAO.getAll();

                List<Map<String, Object>> usersData = new ArrayList<>();
                for (User user : allUsers) {

                    Map<String, Object> userData = Map.of(
                            "id", user.getId(),
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName(),
                            "email", user.getEmail());
                    usersData.add(userData);
                }

                return gson.toJson(Map.of(
                        "status", "ok",
                        "users", usersData));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao buscar usuários: " + e.getMessage()));
            }
        });

        // Endpoint para buscar informações privadas do usuário
        get("/api/private", (req, res) -> {
            try {
                int targetUserId = req.attribute("userId");

                // Buscar o usuário pelo ID
                User user = userService.getUserById(targetUserId);

                if (user == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Usuário não encontrado"));
                }

                // Precisamos enviar os generos favoritos do usuário
                List<Genre> preferredGenres = userGenreService.getPreferredGenres(targetUserId);
                List<Map<String, Object>> genresData = new ArrayList<>();
                for (Genre genre : preferredGenres) {
                    Map<String, Object> genreData = Map.of(
                            "id", genre.getId(),
                            "name", genre.getName());
                    genresData.add(genreData);
                }

                // Apenas informações básicas do usuário
                //
                Map<String, Object> userData = Map.of(
                        "id", user.getId(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "email", user.getEmail(),
                        "gender", String.valueOf(user.getGender())

                );
                // Adiciona os gêneros favoritos do usuário

                return gson.toJson(Map.of("status", "ok", "user", userData, "preferredGenres", genresData));
            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(Map.of("error", "ID de usuário inválido"));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao buscar informações do usuário: " + e.getMessage()));
            }
        });

        // Endpoint para buscar informações do perfil de um usuário específico
        get("/api/profile/:userId", (req, res) -> {
            try {
                int targetUserId = Integer.parseInt(req.params("userId"));

                // Buscar o usuário pelo ID
                User user = userService.getUserById(targetUserId);

                if (user == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Usuário não encontrado"));
                }

                // Apenas informações básicas do usuário
                Map<String, Object> userData = Map.of(
                        "id", user.getId(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "email", user.getEmail(),
                        "gender", String.valueOf(user.getGender()));

                return gson.toJson(Map.of("status", "ok", "user", userData));
            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(Map.of("error", "ID de usuário inválido"));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao buscar informações do usuário: " + e.getMessage()));
            }
        });

        // Endpoint para buscar filmes recomendados para um usuário específico
        get("/api/profile/:userId/recommended", (req, res) -> {
            try {
                int targetUserId = Integer.parseInt(req.params("userId"));

                // Prevent SQL injection
                if (targetUserId <= 0) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "ID de usuário inválido"));
                }

                // Buscar IDs de filmes recomendados
                ArrayList<Recommendation> recommendations = recommendationService
                        .getRecommendationsByUserId(targetUserId);
                List<Map<String, Object>> moviesData = new ArrayList<>();

                // Buscar cada filme no banco de dados e converter para mapa
                for (Recommendation recommendation : recommendations) {
                    int movieId = recommendation.getMovieId();
                    Movie movie = movieService.getMovieById(movieId);
                    if (movie != null) {
                        Map<String, Object> movieData = Map.of(
                                "id", movie.getId(),
                                "title", movie.getTitle(),
                                "poster_path", movie.getPosterPath());
                        moviesData.add(movieData);
                    }
                }

                return gson.toJson(Map.of("status", "ok", "movies", moviesData));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao buscar filmes recomendados: " + e.getMessage()));
            }
        });

        // Endpoint para atualizar informações do perfil do usuário
        post("/api/profile/update", (req, res) -> {
            try {
                int userId = req.attribute("userId");
                JsonObject bodyObj = JsonParser.parseString(req.body()).getAsJsonObject();

                // Extract basic user information
                String firstName = bodyObj.has("firstName") ? bodyObj.get("firstName").getAsString() : null;
                String lastName = bodyObj.has("lastName") ? bodyObj.get("lastName").getAsString() : null;
                String email = bodyObj.has("email") ? bodyObj.get("email").getAsString() : null;
                String gender = bodyObj.has("gender") ? bodyObj.get("gender").getAsString() : null;

                // Extract genres if they exist in the request
                List<Integer> genres = null;
                if (bodyObj.has("genres") && bodyObj.get("genres").isJsonArray()) {
                    JsonArray genresArray = bodyObj.get("genres").getAsJsonArray();
                    genres = new ArrayList<>();
                    for (int i = 0; i < genresArray.size(); i++) {
                        genres.add(genresArray.get(i).getAsInt());
                    }

                    // VALIDAÇÃO: Verificar se pelo menos um gênero foi selecionado
                    if (genres.isEmpty()) {
                        res.status(400);
                        return gson.toJson(Map.of("error", "É necessário selecionar pelo menos um gênero preferido"));
                    }

                    // VALIDAÇÃO: Limitar o número máximo de gêneros
                    if (genres.size() > 5) {
                        res.status(400);
                        return gson.toJson(Map.of("error", "Você pode selecionar no máximo 5 gêneros preferidos"));
                    }
                } else if (bodyObj.has("genres")) {
                    // VALIDAÇÃO: Se o campo genres foi fornecido mas não é um array válido
                    res.status(400);
                    return gson.toJson(Map.of("error", "Formato inválido para gêneros preferidos"));
                }

                // Fetch current user data
                User currentUser = userService.getUserById(userId);
                if (currentUser == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Usuário não encontrado"));
                }

                // Update user object with new values or keep existing ones
                if (firstName != null)
                    currentUser.setFirstName(firstName);
                if (lastName != null)
                    currentUser.setLastName(lastName);
                if (email != null)
                    currentUser.setEmail(email);
                if (gender != null && gender.length() > 0)
                    currentUser.setGender(gender.charAt(0));

                // Update user in database
                boolean userUpdated = userDAO.update(currentUser);
                if (!userUpdated) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Erro ao atualizar informações do usuário"));
                }

                // Se os gêneros não foram fornecidos na requisição, não mexer nos gêneros
                // existentes
                boolean genresUpdated = true;
                if (genres != null) {
                    // Obter gêneros atuais antes de remover (backup)
                    List<Genre> currentGenres = userGenreService.getPreferredGenres(userId);

                    // Remover gêneros existentes
                    boolean genresRemoved = userGenreService.removeAllPreferredGenres(userId);

                    if (!genresRemoved) {
                        System.err.println("Erro ao remover gêneros existentes para o usuário " + userId);
                        // Em caso de falha, não continuamos com a atualização dos gêneros
                        res.status(500);
                        return gson.toJson(Map.of("error", "Erro ao atualizar preferências de gêneros"));
                    }

                    // Adicionar novos gêneros
                    boolean allGenresAdded = true;
                    for (Integer genreId : genres) {
                        if (!userGenreService.addPreferredGenre(userId, genreId)) {
                            allGenresAdded = false;
                            System.err.println("Erro ao adicionar gênero " + genreId + " para o usuário " + userId);
                        }
                    }

                    // Se houve erro ao adicionar novos gêneros, restaurar os gêneros antigos
                    if (!allGenresAdded) {
                        System.err.println("Restaurando gêneros anteriores para o usuário " + userId);
                        // Primeiro limpar novamente para evitar duplicatas
                        userGenreService.removeAllPreferredGenres(userId);

                        // Restaurar gêneros anteriores
                        for (Genre genre : currentGenres) {
                            userGenreService.addPreferredGenre(userId, genre.getId());
                        }

                        res.status(500);
                        return gson.toJson(Map.of("error",
                                "Erro ao atualizar alguns gêneros preferidos. As alterações foram revertidas."));
                    }

                    genresUpdated = allGenresAdded;
                }

                // Fetch updated user information including genres
                User updatedUser = userService.getUserById(userId);
                List<Genre> preferredGenres = userGenreService.getPreferredGenres(userId);

                // Verificar se o usuário realmente tem gêneros após a atualização
                if (preferredGenres == null || preferredGenres.isEmpty()) {
                    System.err.println("AVISO: Usuário " + userId
                            + " ficou sem gêneros após atualização. Isso não deveria acontecer.");
                    // Aqui poderíamos adicionar um gênero padrão, mas para ser consistente com a
                    // validação anterior,
                    // vamos apenas retornar um erro
                    res.status(500);
                    return gson.toJson(Map.of("error", "Erro no servidor: usuário ficou sem gêneros preferidos"));
                }

                // Create response with user data and preferred genres
                Map<String, Object> userData = Map.of(
                        "id", updatedUser.getId(),
                        "firstName", updatedUser.getFirstName(),
                        "lastName", updatedUser.getLastName(),
                        "email", updatedUser.getEmail(),
                        "gender", String.valueOf(updatedUser.getGender()));

                List<Map<String, Object>> genresData = new ArrayList<>();
                for (Genre genre : preferredGenres) {
                    Map<String, Object> genreData = Map.of(
                            "id", genre.getId(),
                            "name", genre.getName());
                    genresData.add(genreData);
                }

                // Final response with success message and updated data
                return gson.toJson(Map.of(
                        "status", "ok",
                        "message", "Perfil atualizado com sucesso",
                        "user", userData,
                        "preferredGenres", genresData));

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao atualizar perfil: " + e.getMessage()));
            }
        });

        // =====================================//
        // Endpoints de Avaliação de Filmes //
        // =====================================//

        get("/api/rate/:movieId", (req, res) -> {
            try {
                int userId = req.attribute("userId");
                int movieId = Integer.parseInt(req.params("movieId"));

                Feedback feedback = feedbackService.getFeedback(userId, movieId);

                if (feedback != null) {
                    res.status(200);
                    return gson.toJson(Map.of(
                            "movieId", movieId,
                            "currentRating", feedback.getFeedback() // true ou false
                    ));
                } else {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Avaliação não encontrada"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro no servidor: " + e.getMessage()));
            }
        });

        delete("/api/rate/:movieId", (req, res) -> {
            try {
                int userId = req.attribute("userId");
                int movieId = Integer.parseInt(req.params("movieId"));
                boolean removed = feedbackService.removeRating(userId, movieId);

                if (removed) {
                    System.out.println("[🏅:🗑️] RATING DELETE SUCCESS: [userId: " + userId + ", movieId: " + movieId + "]");
                    res.status(200);
                    return gson.toJson(Map.of("status", "Avaliação removida com sucesso"));
                } else {
                    System.out.println("[🏅:🔴] RATING DELETE ERROR: [userId: " + userId + ", movieId: " + movieId + "]");
                    res.status(404);
                    return gson.toJson(Map.of("error", "Avaliação não encontrada"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro no servidor: " + e.getMessage()));
            }
        });

        // ========================================//
        // AI - Artificial Intelligence Endpoints //
        // ========================================//

        // Endpoint para enviar feedback de rating
        post("/api/rate", (req, res) -> {
            try {
                int userId = req.attribute("userId");
                JsonObject bodyObj = JsonParser.parseString(req.body()).getAsJsonObject();
                int movieId = bodyObj.get("movieId").getAsInt();
                boolean ratingValue = bodyObj.get("rating").getAsBoolean();

                // Verifica se o filme existe
                boolean movieExists = movieService.movieExists(movieId);
                if (!movieExists) {
                    System.out.println("[🎬:⁉️] MOVIE NOT FOUND: [movieId: " + movieId + "]");
                    JsonObject movieObj = tmdb.getMovieDetails(movieId);
                    if (movieObj == null) {
                        res.status(404);
                        return gson.toJson(Map.of("error", "Filme não encontrado no TMDB"));
                    } else {
                        movieService.storeMovie(movieObj);
                        movieGenreService.storeMovieGenres(movieObj);
                    }
                }

                // Executa a operação
                int result = feedbackService.storeOrUpdateRating(userId, movieId, ratingValue);

                // Treina a IA se rating for criado ou atualizado
                if (result == 1 || result == 2) {
                    flixAi.train(userId, movieId, ratingValue);
                }

                String operation;
                String message;
                Boolean currentRating = null;

                switch (result) {
                    case 1 -> { // CREATE
                        operation = "CREATE";
                        message = "Rating criado";
                        currentRating = ratingValue;
                    }
                    case 2 -> { // UPDATE
                        operation = "UPDATE";
                        message = "Rating atualizado";
                        currentRating = ratingValue;
                    }
                    case 3 -> { // IGNORE
                        operation = "IGNORED";
                        message = "Rating ignorado";
                    }
                    case 0 -> {
                        res.status(500);
                        return gson.toJson(Map.of("error", "Erro ao processar rating"));
                    }
                    default -> {
                        res.status(500);
                        return gson.toJson(Map.of("error", "Erro desconhecido"));
                    }
                }

                res.status(200);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("operation", operation);
                response.put("currentRating", currentRating);
                response.put("message", message);

                return gson.toJson(response);

            } catch (Exception e) {
                System.err.println("[🏅:🔴] RATING ERROR: Endpoint - " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro no servidor: " + e.getMessage()));
            }
        });

        // Endpoint para receber recomendações de filmes
        get("/api/recommendation", (req, res) -> {
            int userId = req.attribute("userId");

            ArrayList<Integer> recommendedMovies = recommendationService.getRecommendedMoviesIds(userId);
            System.out.println("Filmes recomendados: " + recommendedMovies.size());
            ArrayList<Integer> allMovies = movieService.getAllMoviesIds();
            System.out.println("Filmes disponíveis: " + allMovies.size());

            if (allMovies.isEmpty()) {
                res.status(400);
                return "{\"erro\": \"Não há filmes disponíveis para recomendar.\"}";
            }

            final int NUM_CANDIDATOS = 500; // Pode ser configurável futuramente

            List<Integer> candidatos = allMovies.stream()
                    .filter(id -> !recommendedMovies.contains(id))
                    .collect(Collectors.toCollection(ArrayList::new));

            System.out.println("Filmes candidatos: " + candidatos.size());
            if (candidatos.isEmpty()) {
                res.status(404);
                return "{\"erro\": \"Não há filmes não avaliados para recomendar.\"}";
            }

            Collections.shuffle(candidatos, new SecureRandom());

            candidatos = candidatos.subList(0, Math.min(NUM_CANDIDATOS, candidatos.size()));

            // Imprimir Candidatos
            System.out.println("Candidatos: " + candidatos);

            JsonObject recomendacao = flixAi.recommend(userId, candidatos);

            System.out.println("Recomendação recebida: " + recomendacao);

            JsonArray recommendedMoviesJSON = recomendacao.getAsJsonArray("recommended_movies");

            if (recommendedMoviesJSON.size() == 0) {
                res.status(404);
                return "{\"erro\": \"Não há filmes não avaliados para recomendar.\"}";
            } else {
                System.out.println("Filmes recomendados: " + recommendedMoviesJSON.size());
            }

            JsonObject firstMovie = recommendedMoviesJSON.get(0).getAsJsonObject();

            int melhorFilmeId = firstMovie.get("id").getAsInt();
            double score = firstMovie.get("score").getAsDouble();

            System.out.println("Melhor filme recomendado: " + melhorFilmeId + " com score " + score);

            boolean stored = recommendationService.storeRecommendation(userId, melhorFilmeId, score);
            if (!stored) {
                System.err.println(
                        "Erro ao salvar recomendação no banco para userId=" + userId + ", movieId=" + melhorFilmeId);
            }

            JsonObject movie = tmdb.getMovieDetails(melhorFilmeId);
            if (movie == null) {
                res.status(404);
                return "{\"erro\": \"Filme não encontrado.\"}";
            }

            res.type("application/json");
            return movie.toString();

        });

        // =====================//
        // Endpoints de Filmes //
        // =====================//

        // Endpoint de busca de filmes com filtros avançados
        get("/api/movies/search", (req, res) -> {
            try {
                int userId = req.attribute("userId");

                String query = req.queryParams("query");
                if (query == null)
                    query = "";
                query = query.trim();

                int page = 1;
                int limit = 25;

                // Novos parâmetros de filtro
                String sortBy = req.queryParams("sortBy");
                if (sortBy == null)
                    sortBy = "popularity";

                String genresParam = req.queryParams("genres");
                String yearFrom = req.queryParams("yearFrom");
                String yearTo = req.queryParams("yearTo");

                try {
                    page = Integer.parseInt(req.queryParams("page"));
                    limit = Integer.parseInt(req.queryParams("limit"));
                } catch (Exception e) {
                    // valores default se parsing falhar
                }

                if (page < 1)
                    page = 1;
                if (limit < 1 || limit > 100)
                    limit = 25;

                // Buscar filmes com filtros avançados
                ArrayList<Movie> movies = movieService.searchWithFilters(query, page, limit, sortBy, genresParam,
                        yearFrom, yearTo);

                // Contar total de resultados com os mesmos filtros
                int totalResults = movieService.countSearchResultsWithFilters(query, sortBy, genresParam, yearFrom,
                        yearTo);
                int totalPages = (int) Math.ceil((double) totalResults / limit);

                // Montar resposta
                List<Map<String, Object>> results = new ArrayList<>();
                for (Movie movie : movies) {
                    List<Genre> genres = movieGenreService.buscarGenerosDoFilme(movie.getId());
                    List<String> genreNames = genres.stream().map(Genre::getName).collect(Collectors.toList());

                    Map<String, Object> movieData = Map.of(
                            "id", movie.getId(),
                            "title", movie.getTitle(),
                            "poster_path", movie.getPosterPath(),
                            "release_date", movie.getReleaseDate(),
                            "genres", genreNames);
                    results.add(movieData);
                }

                Map<String, Object> response = Map.of(
                        "status", "ok",
                        "page", page,
                        "total_pages", totalPages,
                        "total_results", totalResults,
                        "results", results);

                System.out.println("[🎬:🟢] MOVIE SEARCH SUCCESS: [total: " + totalResults + ", query: " + query
                        + ", filters applied]");

                res.type("application/json");
                res.status(200);
                return gson.toJson(response);

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao buscar filmes: " + e.getMessage()));
            }
        });

        // Endpoint de busca de perfis
        get("/api/profiles/search", (req, res) -> {
            try {
                int userId = req.attribute("userId");

                String query = req.queryParams("query");
                if (query == null)
                    query = "";
                query = query.trim();

                int page = 1;
                int limit = 25;

                try {
                    page = Integer.parseInt(req.queryParams("page"));
                    limit = Integer.parseInt(req.queryParams("limit"));
                } catch (Exception e) {
                    // valores padrão se parsing falhar
                }

                if (page < 1)
                    page = 1;
                if (limit < 1 || limit > 100)
                    limit = 25;

                // Se não há query, retornar usuários mais recentes ou populares (ajuste
                // conforme sua regra)
                if (query.isEmpty()) {
                    ArrayList<User> users = userService.getAllUsers(page, limit);

                    if (users.isEmpty()) {
                        Map<String, Object> response = Map.of(
                                "status", "ok",
                                "page", page,
                                "total_pages", 0,
                                "total_results", 0,
                                "results", new ArrayList<>());
                        res.type("application/json");
                        res.status(200);
                        return gson.toJson(response);
                    }

                    int totalUsers = userService.getTotalUsersCount();
                    int totalPages = (int) Math.ceil((double) totalUsers / limit);

                    List<Map<String, Object>> results = new ArrayList<>();
                    for (User user : users) {
                        Map<String, Object> userData = Map.of(
                                "id", user.getId(),
                                "first_name", user.getFirstName(),
                                "last_name", user.getLastName(),
                                "email", user.getEmail());
                        results.add(userData);
                    }

                    Map<String, Object> response = Map.of(
                            "status", "ok",
                            "page", page,
                            "total_pages", totalPages,
                            "total_results", totalUsers,
                            "results", results);

                    System.out.println("[👤:🟢] PROFILE GET SUCCESS: [total: " + totalUsers + "]");

                    res.type("application/json");
                    res.status(200);
                    return gson.toJson(response);
                }

                // Se há query, realizar busca pelos usuários
                ArrayList<User> users = userService.search(query, page, limit);

                int totalResults = userService.countSearchResults(query);
                int totalPages = (int) Math.ceil((double) totalResults / limit);

                List<Map<String, Object>> results = new ArrayList<>();
                for (User user : users) {
                    Map<String, Object> userData = Map.of(
                            "id", user.getId(),
                            "first_name", user.getFirstName(),
                            "last_name", user.getLastName(),
                            "email", user.getEmail());
                    results.add(userData);
                }

                Map<String, Object> response = Map.of(
                        "status", "ok",
                        "page", page,
                        "total_pages", totalPages,
                        "total_results", totalResults,
                        "results", results);

                System.out.println("[👤:🟢] PROFILE GET SUCCESS: [total: " + totalResults + "query: " + query + "]");

                res.type("application/json");
                res.status(200);
                return gson.toJson(response);

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao buscar perfis: " + e.getMessage()));
            }
        });

        // Endpoint para verificar se um filme está nos favoritos do usuário
        get("/api/movie/:movieId/recommended", (req, res) -> {
            try {
                int userId = req.attribute("userId"); // Usuário logado
                int movieId = Integer.parseInt(req.params("movieId"));

                // Verificar se o filme está nas recomendações do usuário
                boolean isRecommended = recommendationService.isMovieRecommended(userId, movieId);

                return gson.toJson(Map.of(
                        "status", "ok",
                        "movieId", movieId,
                        "isRecommended", isRecommended));

            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(Map.of("error", "ID de filme inválido"));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao verificar recomendação: " + e.getMessage()));
            }
        });

        // Endpoint para buscar detalhes de um filme
        get("/api/movie/:movieId/details", (req, res) -> {
            try {
                int userId = req.attribute("userId");
                int movieId = Integer.parseInt(req.params("movieId"));

                Map<String, Object> response = new HashMap<>();

                Movie movie = movieService.getMovieById(movieId);
                if (movie == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Filme não encontrado"));
                }

                Feedback fback = feedbackService.getFeedback(userId, movieId);
                Boolean rating = fback != null ? fback.getFeedback() : null;

                if (rating != null)
                    response.put("rating", rating == true ? 1 : 0);
                else
                    response.put("rating", null);

                List<Genre> genres = movieGenreService.buscarGenerosDoFilme(movieId);

                List<String> genreNames = genres.stream()
                        .map(Genre::getName)
                        .collect(Collectors.toList());

                Map<String, Object> movieData = Map.of(
                        "id", movie.getId(),
                        "title", movie.getTitle(),
                        "overview", movie.getOverview(),
                        "rating", movie.getRating(),
                        "releaseDate", movie.getReleaseDate(),
                        "originalLanguage", movie.getOriginalLanguage(),
                        "popularity", movie.getPopularity(),
                        "posterPath", movie.getPosterPath(),
                        "backdropPath", movie.getBackdropPath(),
                        "genres", genreNames);

                response.put("movieData", movieData);

                res.type("application/json");
                res.status(200);
                return gson.toJson(response);

            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(Map.of("error", "ID de filme inválido"));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao buscar dados do filme: " + e.getMessage()));
            }
        });

    }

}