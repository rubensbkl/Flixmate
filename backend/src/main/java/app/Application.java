package app;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import controller.Controller;
import dao.FeedbackDAO;
import dao.GenreDAO;
import dao.MovieDAO;
import dao.MovieGenreDAO;
import dao.RecommendationDAO;
import dao.UserDAO;
import dao.UserGenreDAO;
import model.Feedback;
import model.Genre;
import model.Movie;
import model.Recommendation;
import model.User;
import service.AIService;
import service.FeedbackService;
import service.GenreService;
import service.MovieGenreService;
import service.MovieService;
import service.RecommendationService;
import service.TMDBService;
import service.UserGenreService;
import service.UserService;
import util.JWTUtil;

public class Application {

    /**
     * Validates the required environment variables for the application.
     * Throws an exception if any required variable is missing or empty.
     */
    public static void validateRequiredEnvs() {
        // List of required environment variables
        String[] requiredEnvs = {
                "ENV",
                "PORT",
                "JWT_SECRET",
                "TMDB_API_KEY",
                "AZURE_OPENAI_ENDPOINT",
                "AZURE_OPENAI_API_KEY",
                "AZURE_OPENAI_DEPLOYMENT_NAME",
                "DB_HOST",
                "DB_PORT",
                "DB_NAME",
                "DB_USER",
                "DB_PASSWORD"
        };

        // Check each required environment variable
        for (String env : requiredEnvs) {
            String value = System.getenv(env);
            if (value == null || value.isEmpty()) {
                throw new IllegalStateException("Missing required environment variable: " + env);
            }
        }

        System.out.println("All required environment variables are set.");
    }

    /**
     * Main method to start the application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {

        // Valida as variáveis de ambiente necessárias
        validateRequiredEnvs();

        // Pegar todas as variáveis de ambiente respectivamente
        String env = System.getenv("ENV");
        // Pegar a porta do servidor
        int porta = Integer.parseInt(System.getenv("PORT"));
        // Envs de autenticação
        String jwtSecret = System.getenv("JWT_SECRET");
        String tmdbApiKey = System.getenv("TMDB_API_KEY");
        // Envs de IA
        String azureOpenAIEndpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
        String azureOpenAIKey = System.getenv("AZURE_OPENAI_API_KEY");
        String azureOpenAIDeploymentName = System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME");
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

        // Services
        TMDBService tmdb = new TMDBService(tmdbApiKey);
        AIService ai = new AIService(azureOpenAIEndpoint, azureOpenAIKey, azureOpenAIDeploymentName);
        MovieGenreService movieGenreService = new MovieGenreService(movieGenreDAO);
        RecommendationService recommendationService = new RecommendationService(recommendationDAO, tmdb);
        MovieService movieService = new MovieService(movieDAO, movieGenreService, tmdb);
        FeedbackService feedbackService = new FeedbackService(feedbackDAO, movieService);
        UserGenreService userGenreService = new UserGenreService(userGenreDAO);
        UserService userService = new UserService(userDAO);
        GenreService genreService = new GenreService(genreDAO);

        Controller controller = new Controller(tmdb, movieService, movieGenreService, recommendationService, ai,
                genreService, feedbackService);

        // JWT Util
        JWTUtil jwt = new JWTUtil(jwtSecret);

        // Configurar a porta do servidor
        port(porta);

        // Static files e webjars
        staticFiles.location("/public");
        staticFiles.externalLocation("webjars");

        // CORS - Cross-Origin Resource Sharing
        Set<String> allowedOrigins = new HashSet<>(List.of(
                "http://localhost:3000",
                "127.0.0.1:3000",
                "https://442d-2804-6e7c-116-6100-e44f-6d36-62b3-c8ff.ngrok-free.app/",
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

        // Endpoints
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

            // Extrair os dados do usuário
            User user = new User();
            user.setFirstName(requestBody.get("firstName").getAsString());
            user.setLastName(requestBody.get("lastName").getAsString());
            user.setEmail(requestBody.get("email").getAsString());
            user.setPassword(requestBody.get("password").getAsString());
            user.setGender(requestBody.get("gender").getAsString().charAt(0));
            user.setAdult(requestBody.get("isUserAdult").getAsBoolean());

            // Extrair o array de gêneros favoritos
            JsonArray favoriteGenresArray = requestBody.getAsJsonArray("favoriteGenres");
            List<Integer> favoriteGenres = new ArrayList<>();
            for (int i = 0; i < favoriteGenresArray.size(); i++) {
                favoriteGenres.add(favoriteGenresArray.get(i).getAsInt());
            }

            System.out.println("Usuário recebido: " + user);
            System.out.println("Gêneros favoritos: " + favoriteGenres);

            // Verificações básicas
            if (user.getEmail() == null || user.getPassword() == null ||
                    user.getFirstName() == null || user.getLastName() == null ||
                    user.getGender() == '\0') {
                res.status(400);
                return gson.toJson(Map.of("error", "Todos os campos são obrigatórios"));
            }

            // Verificar se o email já existe
            if (userService.getUserByEmail(user.getEmail()) != null) {
                res.status(400);
                return gson.toJson(Map.of("error", "Email já cadastrado"));
            }

            // Verificar se foram selecionados gêneros favoritos
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

                // Adicionar atributos úteis à requisição
                req.attribute("userEmail", decoded.getSubject());
                req.attribute("userId", decoded.getClaim("userId").asInt());
            } catch (Exception e) {
                e.printStackTrace();
                halt(403, gson.toJson(Map.of("error", "Token inválido")));
            }
        });

        get("/api/ping", (req, res) -> gson.toJson(Map.of("status", "ok", "message", "pong")));

        post("/api/feedback", (req, res) -> {
            try {
                int userId = req.attribute("userId");
                JsonObject bodyObj = JsonParser.parseString(req.body()).getAsJsonObject();
                int movieId = bodyObj.get("movieId").getAsInt();
                boolean feedbackValue = bodyObj.get("feedback").getAsBoolean();

                JsonObject movieObj = tmdb.getMovieDetails(movieId);
                if (movieObj == null) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Filme não encontrado na API TMDB"));
                }

                boolean storedMovie = movieService.storeMovie(movieObj);
                boolean storedGenres = movieGenreService.storeMovieGenres(movieObj);
                boolean storedFeedback = feedbackService.storeFeedback(userId, movieId, feedbackValue);

                boolean success = storedMovie && storedGenres && storedFeedback;

                if (success) {
                    res.status(201);
                    return gson.toJson(Map.of("status", "Interação registrada com sucesso"));
                } else {
                    res.status(500);
                    return gson.toJson(Map.of("status", "Erro ao registrar interação"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro no servidor: " + e.getMessage()));
            }
        });

        post("/api/recommendation", (req, res) -> {
            int userId = req.attribute("userId");

            // Buscar interações do usuário
            List<Feedback> interacoes = feedbackService.getFeedbacksByUserId(userId);
            System.out.println("Interações do usuário: " + interacoes);
            // Buscar gêneros favoritos do usuário
            List<Genre> generosFavoritos = userGenreService.getPreferredGenres(userId);

            System.out.println("Interações do usuário: " + interacoes);
            System.out.println("Gêneros favoritos do usuário: " + generosFavoritos);

            // Verificar se o usuário tem interações
            if (interacoes == null || interacoes.isEmpty()) {
                res.status(400);
                return gson.toJson(Map.of("error", "Usuário sem interações"));
            }

            // Verificar se o usuário tem gêneros favoritos
            if (generosFavoritos == null || generosFavoritos.isEmpty()) {
                res.status(400);
                return gson.toJson(Map.of("error", "Usuário sem gêneros favoritos"));
            }

            // chamar controller para gerar recomendação
            System.out.println("Gerando recomendação...");
            JsonObject recomendacao = controller.gerarRecomendacaoPersonalizada(userId, interacoes, generosFavoritos, res);

            if (recomendacao == null) {
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao gerar recomendação"));
            }

            if (recomendacao.has("error")) {
                res.status(400);
                return gson.toJson(Map.of("error", recomendacao.get("error").getAsString()));
            }

            // ENviar a recomendação
            res.status(200);
            return gson.toJson(Map.of("status", "ok", "recomendacao", recomendacao));

        });

        // Endpoint para suprise
        post("/api/recommendation/surprise", (req, res) -> {
            int userId = req.attribute("userId");

            System.out.println("Gerando recomendação surpresa...");
            // Buscar gêneros favoritos do usuário
            List<Genre> generosFavoritos = userGenreService.getPreferredGenres(userId);

            System.out.println("Gêneros favoritos do usuário: " + generosFavoritos);

            // Gerar recomendação aleatória
            return controller.gerarRecomendacaoAleatoria(userId, generosFavoritos, res);
        });

        // post("/api/recommendation", (req, res) -> {
        // int userId = req.attribute("userId");

        // List<Feedback> interacoes = feedbackService.getFeedbacksByUserId(userId);
        // // Verifica se o usuário tem interações
        // if (interacoes != null) {
        // RecommendationHelper helper = new RecommendationHelper();
        // for (Feedback feedback : interacoes) {
        // int movieId = feedback.getMovieId();
        // Movie movie = movieService.buscarFilmePorId(movieId);
        // List<Genre> generos = movieGenreService.buscarGenerosDoFilme(movieId);

        // helper.addMovieWithGenres(movie, generos);
        // }

        // }

        // // Buscar gêneros favoritos do usuário
        // List<Genre> generosFavoritos = userGenreService.getPreferredGenres(userId);

        // // Obter filmes candidatos para recomendação
        // JsonArray candidateMoviesJson =
        // recommendationService.getCandidateMoviesJSON(interacoes);

        // if (candidateMoviesJson == null || candidateMoviesJson.size() == 0) {
        // // Se nao houver filmes candidatos, buscar 10 aleatórios

        // }

        // // Adicionar filmes candidatos ao helper
        // for (int i = 0; i < candidateMoviesJson.size(); i++) {
        // JsonObject movieJson = candidateMoviesJson.get(i).getAsJsonObject();

        // // Extrair os dados básicos do filme a partir do JSON
        // int movieId = movieJson.get("id").getAsInt();
        // String title = movieJson.get("title").getAsString();
        // String releaseDate = movieJson.has("release_date") ?
        // movieJson.get("release_date").getAsString() : "";
        // String originalLanguage = movieJson.get("original_language").getAsString();
        // double popularity = movieJson.get("popularity").getAsDouble();
        // boolean adult = movieJson.get("adult").getAsBoolean();
        // // Adicionar à lista de candidatos

        // Movie movie = new Movie(movieId, title, releaseDate, originalLanguage,
        // popularity, adult);

        // helper.addCandidateMovieId(movieId);

        // // Processar gêneros do JSON
        // List<Genre> genres = new ArrayList<>();
        // if (movieJson.has("genre_ids") && movieJson.get("genre_ids").isJsonArray()) {
        // JsonArray genreIdsJson = movieJson.getAsJsonArray("genre_ids");
        // for (int j = 0; j < genreIdsJson.size(); j++) {
        // int genreId = genreIdsJson.get(j).getAsInt();
        // // Buscar detalhes do gênero pelo ID
        // Genre genre = genreService.getGenreById(genreId);
        // if (genre != null) {
        // genres.add(genre);
        // }
        // }
        // }

        // // Adicionar o filme com seus gêneros ao helper
        // helper.addMovieWithGenres(movie, genres);
        // }

        // // Verificar se temos dados suficientes para gerar recomendação
        // if (!helper.hasEnoughDataForRecommendation()) {
        // res.status(400);
        // return gson.toJson(Map.of("error", "Dados insuficientes para gerar
        // recomendação"));
        // }

        // try {
        // System.out.println("Chamando IA para gerar recomendação...");
        // int recommendedMovieId = ai.gerarRecomendacao(interacoes, generosFavoritos,
        // helper);
        // System.out.println("ID do filme recomendado: " + recommendedMovieId);

        // System.out.println("Limpando interações do usuário...");
        // boolean clearFeedback = feedbackService.clearAllById(userId);
        // System.out.println("Resultado da limpeza: " + clearFeedback);

        // if (!clearFeedback) {
        // res.status(500);
        // return gson.toJson(Map.of("error", "Erro ao limpar interações"));
        // }

        // // Limpar o helper depois de usar
        // helper.clear();
        // // Checar se o filme ja existe no banco de dados
        // Movie existingMovie = movieService.buscarFilmePorId(recommendedMovieId);
        // JsonObject movieObj;
        // if (existingMovie != null) {
        // System.out.println("Filme já existe no banco de dados: " + existingMovie);
        // return gson.toJson(Map.of("status", "ok", "recomendacao", existingMovie));
        // } else {
        // System.out.println("Filme não encontrado no banco de dados, buscando na
        // API...");
        // movieObj = tmdb.getMovieDetails(recommendedMovieId);
        // // Armazenar o filme no banco de dados
        // boolean storedRecomendation = movieService.storeMovie(movieObj);
        // System.out.println("Filme armazenado: " + storedRecomendation);
        // }
        // boolean storedGenres = movieGenreService.storeMovieGenres(movieObj);

        // // Armazenar a recomendação no banco de dados
        // boolean storedRecommendation =
        // recommendationService.storeRecommendation(userId, recommendedMovieId);

        // System.out.println("Recomendação armazenada: " + storedRecommendation);

        // return gson.toJson(Map.of("status", "ok", "recomendacao", movieObj));
        // } catch (Exception e) {
        // System.out.println("Erro ao gerar recomendação:");
        // e.printStackTrace();
        // res.status(500);
        // return gson.toJson(Map.of("error", "Erro ao gerar recomendação"));
        // }

        // });

        // Endpoint para classifcar filmes favoritos de recomendações
        post("/api/recommendation/favorite", (req, res) -> {
            try {
                int userId = req.attribute("userId");
                JsonObject bodyObj = JsonParser.parseString(req.body()).getAsJsonObject();
                int movieId = bodyObj.get("movieId").getAsInt();

                // Pegar a recomendação do banco de dados
                Recommendation recommendation = recommendationService.getRecommendationByUserIdAndMovieId(userId,
                        movieId);

                // Calcular o status de favorito
                boolean favoriteValue = !recommendation.isFavorite();

                // Atualizar o status de favorito no banco de dados
                boolean updated = recommendationService.updateRecommendation(userId, movieId,
                        recommendation.isWatched(), favoriteValue);
                if (updated) {
                    return gson.toJson(Map.of("status", "ok", "message", "Recomendação atualizada com sucesso"));
                } else {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Erro ao atualizar recomendação"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao atualizar recomendação"));
            }
        });

        // Endpoint para classifcar filmes assistidos de recomendações
        post("/api/recommendation/watched", (req, res) -> {
            try {
                int userId = req.attribute("userId");
                JsonObject bodyObj = JsonParser.parseString(req.body()).getAsJsonObject();
                int movieId = bodyObj.get("movieId").getAsInt();

                // Pegar a recomendação do banco de dados
                Recommendation recommendation = recommendationService.getRecommendationByUserIdAndMovieId(userId,
                        movieId);

                // Calcular o status de assistido
                boolean watchedValue = !recommendation.isWatched();

                // Atualizar o status de assistido no banco de dados
                boolean updated = recommendationService.updateRecommendation(userId, movieId, watchedValue,
                        recommendation.isFavorite());
                if (updated) {
                    return gson.toJson(Map.of("status", "ok", "message", "Recomendação atualizada com sucesso"));
                } else {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Erro ao atualizar recomendação"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao atualizar recomendação"));
            }
        });

        // Endpoint para deleter filmes recomendados
        post("/api/recommendation/delete", (req, res) -> {
            try {
                int userId = req.attribute("userId");
                JsonObject bodyObj = JsonParser.parseString(req.body()).getAsJsonObject();
                int movieId = bodyObj.get("movieId").getAsInt();

                // Deletar a recomendação do banco de dados
                boolean deleted = recommendationService.deleteRecommendation(userId, movieId);
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

        post("/api/recommendations", (req, res) -> {
            try {
                int currentUserId = req.attribute("userId");
                ArrayList<Recommendation> recommendations = recommendationService
                        .getRecommendationsByUserId(currentUserId);
                if (recommendations.isEmpty()) {
                    return gson.toJson(Map.of("error", "Nenhuma recomendação encontrada"));
                }
                List<Map<String, Object>> moviesData = new ArrayList<>();

                // Buscar cada filme no banco de dados e converter para mapa
                for (Recommendation recommendation : recommendations) {
                    int movieId = recommendation.getMovieId();
                    Movie movie = movieService.buscarFilmePorId(movieId);

                    if (movie != null) {
                        List<Integer> movieGenresIds = movieGenreService.getGenreIdsForMovie(movieId);
                        System.out.println("Gêneros do filme: " + movieGenresIds);
                        ArrayList<Genre> movieGenres = new ArrayList<>();
                        for (Integer genreId : movieGenresIds) {
                            Genre genre = genreService.getGenreById(genreId);
                            System.out.println("Gênero: " + genre);
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
                        movieData.put("watched", recommendation.isWatched());
                        movieData.put("favorite", recommendation.isFavorite());

                        System.out.println("Dados do filme: " + movieData);
                        moviesData.add(movieData);
                    }
                }

                return gson.toJson(Map.of("status", "ok", "movies", moviesData));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao receber recomendações"));
            }
        });

        post("/api/movies", (req, res) -> {

            // Extrair ID do usuário do token
            int userId = req.attribute("userId");

            JsonObject requestBody = gson.fromJson(req.body(), JsonObject.class);
            int page = requestBody.has("page") ? requestBody.get("page").getAsInt() : 1;
            System.out.println("Page: " + page);
            try {
                // Pega a flag do filtro adulto do usuário
                boolean contentFilter = userService.getContentFilter(userId);
                System.out.println("Content Filter: " + contentFilter);
                // DAO de preferências
                List<Genre> preferredGenresList = userGenreDAO.getPreferredGenres(userId);
                List<Integer> preferredGenres = preferredGenresList.stream()
                        .map(Genre::getId)
                        .toList();

                // Pega filmes de várias listas
                JsonArray trendingMovies = tmdb.getTrendingMovies(page);
                JsonArray popularMovies = tmdb.getPopularMovies(page);
                JsonArray topRatedMovies = tmdb.getTopRatedMovies(page);
                JsonArray upcomingMovies = tmdb.getUpcomingMovies(page);

                // Usamos um Set para evitar duplicatas por ID
                Map<Integer, JsonObject> uniqueMoviesMap = new HashMap<>();

                // Função para processar cada array e adicionar ao mapa de filmes únicos
                Consumer<JsonArray> processMovies = (moviesArray) -> {
                    if (moviesArray != null) {
                        moviesArray.forEach(element -> {
                            if (element.isJsonObject()) {
                                JsonObject movie = element.getAsJsonObject();
                                if (movie.has("id")) {
                                    int movieId = movie.get("id").getAsInt();

                                    // Verifica se é conteúdo adulto e se o filtro está ativado
                                    boolean isAdult = movie.has("adult") && movie.get("adult").getAsBoolean();
                                    if (contentFilter && isAdult) {
                                        // Pula o filme se for adulto e o usuário não quiser ver
                                        System.out.println(
                                                "Filme " + movieId + " é adulto e o filtro está ativado. Pulando...");
                                        return;
                                    }
                                    // Só adiciona se ainda não existe no mapa
                                    if (!uniqueMoviesMap.containsKey(movieId)) {
                                        uniqueMoviesMap.put(movieId, movie);
                                    }
                                }
                            }
                        });
                    }
                };

                // Processa todas as listas
                processMovies.accept(trendingMovies);
                processMovies.accept(popularMovies);
                processMovies.accept(topRatedMovies);
                processMovies.accept(upcomingMovies);

                // Converte o mapa para uma lista
                List<JsonObject> allMovies = new ArrayList<>(uniqueMoviesMap.values());

                // Ordena pela compatibilidade com os gêneros preferidos
                allMovies.sort((m1, m2) -> {
                    int score1 = userGenreService.calculateGenreScore(m1, preferredGenres);
                    int score2 = userGenreService.calculateGenreScore(m2, preferredGenres);
                    return Integer.compare(score2, score1); // Ordem decrescente
                });

                int totalMovies = allMovies.size();
                int moviesToReturn = (totalMovies / 10) * 10; // maior múltiplo de 10 menor ou igual
                if (moviesToReturn == 0 && totalMovies > 0) {
                    moviesToReturn = Math.min(10, totalMovies);
                }
                List<JsonObject> topMovies = allMovies.stream().limit(moviesToReturn).toList();

                // Log para debug
                System.out.println("Total de filmes após remover duplicatas e aplicar filtros: " + totalMovies);
                System.out.println("Enviando " + moviesToReturn + " filmes para o cliente");

                return gson.toJson(Map.of("status", "ok", "movies", topMovies));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao buscar recomendações de filmes: " + e.getMessage()));
            }
        });

        // Endpoint para listar todos os usuários
        get("/api/users", (req, res) -> {
            try {
                int currentUserId = req.attribute("userId");

                // Prevent SQL injection
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

        // Endpoint para pegar filmes recomendados marcados como assistidos por
        // determinado usuário
        get("/api/users/:userId/watched", (req, res) -> {
            try {
                int targetUserId = req.attribute("userId");

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
                    Movie movie = movieService.buscarFilmePorId(movieId);

                    // Verifica se o filme foi assistido
                    if (movie != null && recommendation.isWatched()) {
                        // Adiciona o filme à lista de assistidos
                        Map<String, Object> movieData = Map.of(
                                "id", movie.getId(),
                                "title", movie.getTitle(),
                                "poster_path", movie.getPosterPath());
                        moviesData.add(movieData);
                    } else {
                        // Se o filme não foi assistido, não adiciona à lista
                        System.out.println("Filme " + movieId + " não foi assistido. Pulando...");
                    }
                }

                return gson.toJson(Map.of("status", "ok", "movies", moviesData));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao buscar filmes assistidos: " + e.getMessage()));
            }
        });

        // Endpoint para pegar filmes recomendados marcados como favoritos por
        // determinado usuário
        get("/api/users/:userId/favorites", (req, res) -> {
            try {
                int targetUserId = req.attribute("userId");

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
                    Movie movie = movieService.buscarFilmePorId(movieId);

                    // Verifica se o filme é favorito
                    if (movie != null && recommendation.isFavorite()) {
                        // Adiciona o filme à lista de favoritos
                        Map<String, Object> movieData = Map.of(
                                "id", movie.getId(),
                                "title", movie.getTitle(),
                                "poster_path", movie.getPosterPath());
                        moviesData.add(movieData);
                    } else {
                        // Se o filme não é favorito, não adiciona à lista
                        System.out.println("Filme " + movieId + " não é favorito. Pulando...");
                    }

                }

                return gson.toJson(Map.of("status", "ok", "movies", moviesData));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao buscar filmes favoritos: " + e.getMessage()));
            }
        });

        get("/api/myprofile", (req, res) -> {
            try {
                int targetUserId = req.attribute("userId");

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

        get("/api/profile/:userId", (req, res) -> {
            try {
                int targetUserId = req.attribute("userId");

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

        get("/api/users/:userId/recommended", (req, res) -> {
            try {
                int targetUserId = req.attribute("userId");

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
                    Movie movie = movieService.buscarFilmePorId(movieId);
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

        get("/api/feedbacks/reset", (req, res) -> {
            try {
                int userId = req.attribute("userId");

                System.out.println("🗑️ Resetando todos os feedbacks do usuário: " + userId);

                if (userId <= 0) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "ID de usuário inválido"));
                }

                boolean cleared = feedbackService.clearAllById(userId);


                
                if (cleared) {
                    System.out.println("✅ Feedbacks do usuário " + userId + " resetados com sucesso");
                    return gson.toJson(Map.of("status", "ok", "message", "Feedbacks resetados com sucesso"));
                } else {
                    System.out.println("❌ Erro ao resetar feedbacks do usuário " + userId);
                    res.status(500);
                    return gson.toJson(Map.of("error", "Erro ao resetar feedbacks"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao processar solicitação: " + e.getMessage()));
            }
        });

    }

}