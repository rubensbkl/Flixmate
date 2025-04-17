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

import dao.FeedbackDAO;
import dao.MovieDAO;
import dao.MovieGenreDAO;
import dao.UserDAO;
import dao.UserGenreDAO;
import model.Feedback;
import model.Genre;
import model.Movie;
import model.User;
import service.AIService;
import service.FeedbackService;
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
        UserDAO userDAO = new UserDAO(dbHost, dbName, dbPort, dbUser, dbPassword);
        UserGenreDAO userGenreDAO = new UserGenreDAO(dbHost, dbName, dbPort, dbUser, dbPassword);

        // Services
        TMDBService tmdb = new TMDBService(tmdbApiKey);
        AIService ai = new AIService(azureOpenAIEndpoint, azureOpenAIKey, azureOpenAIDeploymentName, tmdb);
        RecommendationService recommendationService = new RecommendationService(tmdb);
        MovieGenreService movieGenreService = new MovieGenreService(movieGenreDAO);
        MovieService movieService = new MovieService(movieDAO, movieGenreService, tmdb);
        FeedbackService feedbackService = new FeedbackService(feedbackDAO, movieService);
        UserGenreService userGenreService = new UserGenreService(userGenreDAO);
        UserService userService = new UserService(userDAO);

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
                "https://3bd4-187-86-247-172.ngrok-free.app",
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

                // Obter ID do usuário do atributo da requisição
                int userId = req.attribute("userId");

                // Parse do corpo da requisição
                JsonObject bodyObj = JsonParser.parseString(req.body()).getAsJsonObject();

                // Criar objeto Feedback
                Feedback feedback = new Feedback();
                feedback.setUserId(userId);
                feedback.setMovieId(bodyObj.get("movieId").getAsInt());
                feedback.setFeedback(bodyObj.get("feedback").getAsBoolean());

                boolean sucesso = feedbackService.registrarFeedback(feedback);

                if (sucesso) {
                    res.status(201);
                    return gson.toJson(Map.of("status", "Interação registrada com sucesso"));
                } else {
                    res.status(500);
                    return gson.toJson(Map.of("status", "Erro ao registrar interação"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro no servidor"));
            }
        });

        post("/api/recommendation", (req, res) -> {
            System.out.println("Recebida requisição para /api/recommendation");

            JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
            System.out.println("Corpo da requisição: " + body);

            // Obter ID do usuário do atributo da requisição
            int userId = req.attribute("userId");
            System.out.println("ID do usuário obtido do token: " + userId);

            List<Feedback> interacoes = feedbackDAO.getFeedbacksByUserId(userId);
            // Verifica se o usuário tem interações
            if (interacoes.isEmpty()) {
                res.status(400);
                return gson.toJson(Map.of("error", "Usuário não possui interações"));
            }

            // Enriquecer as interações com detalhes dos filmes
            System.out.println("Enriquecendo interações do usuário com detalhes dos filmes:");
            for (Feedback feedback : interacoes) {
                Movie movie = movieService.buscarFilmePorId(feedback.getMovieId());
                ArrayList<Genre> generos = movieGenreService.buscarGenerosDoFilme(feedback.getMovieId());

                // Formatar os gêneros como string
                StringBuilder genresStr = new StringBuilder();
                for (int i = 0; i < generos.size(); i++) {
                    if (i > 0)
                        genresStr.append(", ");
                    genresStr.append(generos.get(i).getName()).append(" (ID: ").append(generos.get(i).getId())
                            .append(")");
                }

                // Adicionar detalhes do filme ao feedback
                feedback.setMovieTitle(movie.getTitle());
                feedback.setMovieGenres(genresStr.toString());
                feedback.setReleaseDate(movie.getReleaseDate());
                feedback.setPopularity(movie.getPopularity());
                feedback.setAdult(movie.getAdult());
                feedback.setOriginalLanguage(movie.getOriginalLanguage());

                // Adicionar gêneros ao filme original
                ArrayList<Integer> generosIds = new ArrayList<>();
                for (Genre g : generos) {
                    generosIds.add(g.getId());
                }
                movie.setGenreIds(generosIds);
            }

            List<Genre> generosFavoritos = userGenreDAO.getPreferredGenres(userId);
            List<Movie> candidateMovies = recommendationService.getCandidateMovies(interacoes);

            try {
                System.out.println("Chamando IA para gerar recomendação...");
                int recommendedMovieId = ai.gerarRecomendacao(interacoes, generosFavoritos, candidateMovies);
                System.out.println("ID do filme recomendado: " + recommendedMovieId);

                System.out.println("Limpando interações do usuário...");
                boolean clearFeedback = feedbackDAO.clear(userId);
                System.out.println("Resultado da limpeza: " + clearFeedback);

                if (!clearFeedback) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Erro ao limpar interações"));
                }

                return gson.toJson(Map.of("status", "ok", "recomendacao", recommendedMovieId));
            } catch (Exception e) {
                System.out.println("Erro ao gerar recomendação:");
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao gerar recomendação"));
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
    }
}