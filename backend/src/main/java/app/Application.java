package app;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dao.InteractionDAO;
import dao.UserDAO;
import model.Interaction;
import model.User;
import service.AIService;
import service.TMDBService;
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
        // RecommendationDAO recommendationDAO = new RecommendationDAO(dbHost, dbName, dbPort, dbUser, dbPassword);

        // Services
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
                "https://flixmate.com.br"
            ));

            options("/*", (req, res) -> {
                String acrh = req.headers("Access-Control-Request-Headers");
                if (acrh != null) res.header("Access-Control-Allow-Headers", acrh);

                String acrm = req.headers("Access-Control-Request-Method");
                if (acrm != null) res.header("Access-Control-Allow-Methods", acrm);

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
                    "https://flixmate.com.br"
            ));

            options("/*", (req, res) -> {
                String acrh = req.headers("Access-Control-Request-Headers");
                if (acrh != null) res.header("Access-Control-Allow-Headers", acrh);

                String acrm = req.headers("Access-Control-Request-Method");
                if (acrm != null) res.header("Access-Control-Allow-Methods", acrm);

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
                String token = jwt.generateToken(user.getEmail());
                return gson.toJson(Map.of("token", token));
            }
            res.status(401);
            return gson.toJson(Map.of("error", "Credenciais inválidas"));
        });

        post("/api/register", (req, res) -> {
            User user = gson.fromJson(req.body(), User.class);
            if (userDAO.insert(user)) {
                res.status(201);
                return gson.toJson(Map.of("status", "success"));
            }
            res.status(400);
            return gson.toJson(Map.of("error", "Erro ao criar usuário"));
        });

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

        get("/api/movies", (req, res) -> {
            String pageParam = req.queryParams("page");
            int page = pageParam != null ? Integer.parseInt(pageParam) : 1;

            try {
                JsonArray popularMovies = tmdb.getPopularMovies(page);
                return gson.toJson(Map.of("status", "ok", "movies", popularMovies));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao buscar filmes populares"));
            }
        });
    }
}