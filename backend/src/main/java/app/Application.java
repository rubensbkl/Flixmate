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
import java.util.stream.Stream;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dao.InteractionDAO;
import dao.UserDAO;
import dao.UserPreferredGenreDAO;
import model.Genre;
import model.Interaction;
import model.User;
import service.AIService;
import service.TMDBService;
import service.UserPreferredGenreService;
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
        UserDAO userDAO = new UserDAO(dbHost, dbName, dbPort, dbUser, dbPassword);
        InteractionDAO interactionDAO = new InteractionDAO(dbHost, dbName, dbPort, dbUser, dbPassword);
        UserPreferredGenreDAO upgDAO = new UserPreferredGenreDAO(dbHost, dbName, dbPort, dbUser, dbPassword);
        // RecommendationDAO recommendationDAO = new RecommendationDAO(dbHost, dbName,
        // dbPort, dbUser, dbPassword);

        // Services
        UserPreferredGenreService userPreferredGenreService = new UserPreferredGenreService();
        TMDBService tmdb = new TMDBService(tmdbApiKey);
        AIService ai = new AIService(azureOpenAIEndpoint, azureOpenAIKey, azureOpenAIDeploymentName, tmdb);

        // JWT Util
        JWTUtil jwt = new JWTUtil(jwtSecret);

        // Configurar a porta do servidor
        port(porta);

        // Static files e webjars
        staticFiles.location("/public");
        staticFiles.externalLocation("webjars");

        // CORS - Cross-Origin Resource Sharing
        if (env.equals("dev")) {
            Set<String> allowedOrigins = new HashSet<>(List.of(
                    "http://localhost:3000",
                    "https://3bd4-187-86-247-172.ngrok-free.app",
                    "https://flixmate.com.br"));

            options("/*", (req, res) -> {
                String acrh = req.headers("Access-Control-Request-Headers");
                if (acrh != null)
                    res.header("Access-Control-Allow-Headers", acrh);

                String acrm = req.headers("Access-Control-Request-Method");
                if (acrm != null)
                    res.header("Access-Control-Allow-Methods", acrm);

                res.header("Access-Control-Allow-Credentials", "true");

                return "OK";
            });

            before((req, res) -> {
                String origin = req.headers("Origin");
                if (origin != null && allowedOrigins.contains(origin)) {
                    res.header("Access-Control-Allow-Origin", origin);
                }

                res.header("Access-Control-Allow-Credentials", "true");
                res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                res.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept");
                res.type("application/json");
            });

        } else if (env.equals("production")) {
            Set<String> allowedOrigins = new HashSet<>(List.of(
                    "http://localhost:3000",
                    "https://flixmate.com.br"));

            options("/*", (req, res) -> {
                String acrh = req.headers("Access-Control-Request-Headers");
                if (acrh != null)
                    res.header("Access-Control-Allow-Headers", acrh);

                String acrm = req.headers("Access-Control-Request-Method");
                if (acrm != null)
                    res.header("Access-Control-Allow-Methods", acrm);

                res.header("Access-Control-Allow-Credentials", "true");

                return "OK";
            });

            before((req, res) -> {
                String origin = req.headers("Origin");
                if (origin != null && allowedOrigins.contains(origin)) {
                    res.header("Access-Control-Allow-Origin", origin);
                }

                res.header("Access-Control-Allow-Credentials", "true");
                res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                res.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept");
                res.type("application/json");
            });
        }

        // Endpoints
        post("/api/login", (req, res) -> {
            User user = gson.fromJson(req.body(), User.class);
            if (userDAO.auth(user.getEmail(), user.getPassword())) {
                // Buscar os dados completos do usuário
                User fullUser = userDAO.getByEmail(user.getEmail());

                // Gerar token JWT
                String token = jwt.generateToken(user.getEmail());

                // Criar resposta com token e dados básicos do usuário (sem senha)
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("user", Map.of(
                        "id", fullUser.getId(),
                        "firstName", fullUser.getFirstName(),
                        "lastName", fullUser.getLastName(),
                        "email", fullUser.getEmail()));

                return gson.toJson(response);
            }
            res.status(401);
            return gson.toJson(Map.of("error", "Credenciais inválidas"));
        });

        post("/api/register", (req, res) -> {
            User user = gson.fromJson(req.body(), User.class);

            // Verificações básicas
            if (user.getEmail() == null || user.getPassword() == null ||
                    user.getFirstName() == null || user.getLastName() == null ||
                    user.getGender() == '\0') {
                res.status(400);
                return gson.toJson(Map.of("error", "Todos os campos são obrigatórios"));
            }

            // Verificar se o email já existe
            if (userDAO.getByEmail(user.getEmail()) != null) {
                res.status(400);
                return gson.toJson(Map.of("error", "Email já cadastrado"));
            }

            // Inserir no banco
            if (userDAO.insert(user)) {
                // Gerar token
                User fullUser = userDAO.getByEmail(user.getEmail());
                String token = jwt.generateToken(fullUser.getEmail());

                Map<String, Object> userData = Map.of(
                        "id", fullUser.getId(),
                        "firstName", fullUser.getFirstName(),
                        "lastName", fullUser.getLastName(),
                        "email", fullUser.getEmail(),
                        "gender", fullUser.getGender());

                res.status(201);
                return gson.toJson(Map.of(
                        "status", "success",
                        "token", token,
                        "user", userData));
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
                String userEmail = decoded.getSubject();

                // Buscar informações do usuário
                User user = userDAO.getByEmail(userEmail);
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
        before("/api/protected/*", (req, res) -> {
            String authHeader = req.headers("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                halt(401, "{\"error\":\"Token não enviado\"}");
            }

            try {
                String token = authHeader.substring(7);
                var decoded = jwt.verifyToken(token);
                req.attribute("userEmail", decoded.getSubject());
            } catch (Exception e) {
                halt(403, "{\"error\":\"Token inválido\"}");
            }
        });

        get("/api/ping", (req, res) -> gson.toJson(Map.of("status", "ok", "message", "pong")));

        post("/api/feedback", (req, res) -> {
            Interaction interaction = gson.fromJson(req.body(), Interaction.class);
            if (interaction.getUserId() <= 0 || interaction.getMovieId() <= 0 || interaction.getInteraction() == null) {
                res.status(400);
                return gson.toJson(Map.of("error", "Dados inválidos"));
            }

            boolean sucesso = interactionDAO.insert(interaction);

            if (sucesso) {
                res.status(201);
                return gson.toJson(Map.of("status", "Interação registrada com sucesso"));
            } else {
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao registrar interação"));
            }
        });

        post("/api/recommendation", (req, res) -> {
            JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
            int userId = body.get("userId").getAsInt();

            List<Interaction> interacoes = interactionDAO.getInteractionsByUserId(userId);

            if (interacoes.size() < 5) {
                res.status(400);
                return gson.toJson(Map.of("error", "Usuário precisa ter ao menos 5 interações"));
            }

            try {
                String recomendacao = ai.gerarRecomendacao(interacoes);
                boolean sucesso = interactionDAO.clear(userId);

                if (!sucesso) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Erro ao limpar interações"));
                }

                return gson.toJson(Map.of("status", "ok", "recomendacao", recomendacao));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao gerar recomendação"));
            }
        });

        post("/api/movies", (req, res) -> {
            JsonObject requestBody = gson.fromJson(req.body(), JsonObject.class);
            int userId = requestBody.get("userId").getAsInt();
            int page = requestBody.has("page") ? requestBody.get("page").getAsInt() : 1;
            System.out.println("Page: " + page);
            try {
                // Pega a flag do filtro adulto do usuário
                boolean contentFilter = userDAO.getContentFilter(userId);
                System.out.println("Content Filter: " + contentFilter);
                // DAO de preferências
                List<Genre> preferredGenresList = upgDAO.getPreferredGenres(userId);
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
                                        System.out.println("Filme " + movieId + " é adulto e o filtro está ativado. Pulando...");
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
                    int score1 = userPreferredGenreService.calculateGenreScore(m1, preferredGenres);
                    int score2 = userPreferredGenreService.calculateGenreScore(m2, preferredGenres);
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