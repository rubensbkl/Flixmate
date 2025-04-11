package app;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dao.UserDAO;
import dao.InteractionDAO;
import dao.RecommendationDAO;
import model.User;
import model.Interaction;
import model.Recommendation;
import service.AIService;

import io.github.cdimascio.dotenv.Dotenv;
import util.JWTUtil;

public class Application {

    public static void main(String[] args) {

        // Detecta o ambiente (padrão: dev)
        String env = System.getenv().getOrDefault("ENV", "dev");

        // Carrega o arquivo .env correspondente
        Dotenv dotenv = Dotenv.configure().filename(".env." + env).load();

        // Define a porta
        port(Integer.parseInt(dotenv.get("PORT", "6789")));

        // Static files e webjars
        staticFiles.location("/public");
        staticFiles.externalLocation("webjars");


        System.out.println("Iniciando aplicação no ambiente: " + env);
        System.out.println("Porta: " + dotenv.get("PORT", "6789"));

        // CORS - Cross-Origin Resource Sharing 
        if (env.equals("dev")) {
        Set<String> allowedOrigins =
            new HashSet<>(Arrays.asList("http://localhost:3000"));

        options("/*", (request, response) -> {
            String acrh = request.headers("Access-Control-Request-Headers");
            if (acrh != null)
            response.header("Access-Control-Allow-Headers", acrh);

            String acrm = request.headers("Access-Control-Request-Method");
            if (acrm != null)
            response.header("Access-Control-Allow-Methods", acrm);

            return "OK";
        });

        before((req, res) -> {
            String origin = req.headers("Origin");
            if (origin != null && allowedOrigins.contains(origin)) {
            res.header("Access-Control-Allow-Origin", origin);
            }

            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers",
                    "Content-Type, Authorization, X-Requested-With, Accept");
            res.type("application/json");
        });

        } else if (env.equals("production")) {
        Set<String> allowedOrigins =
            new HashSet<>(Arrays.asList("https://flixmate.com.br"));

        before((req, res) -> {
            String origin = req.headers("Origin");
            if (origin != null && allowedOrigins.contains(origin)) {
            res.header("Access-Control-Allow-Origin", origin);
            }

            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers",
                    "Content-Type, Authorization, X-Requested-With, Accept");
            res.type("application/json");
        });
        }
        
        
        Gson gson = new Gson();
        UserDAO userDAO = new UserDAO();
        
        // Rotas públicas
        post("/api/login", (req, res) -> {
            User user = gson.fromJson(req.body(), User.class);
            if (userDAO.auth(user.getEmail(), user.getPassword())) {
                String token = JWTUtil.generateToken(user.getEmail());
                res.type("application/json");
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
        
        // Middleware para autenticação em todas as rotas protegidas
        before("/api/protected/*", (req, res) -> {
            // Exceções: login e register
            String path = req.pathInfo();
            if (path.equals("/api/login") || path.equals("/api/register"))
            return;
            
            String authHeader = req.headers("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                halt(401, "{\"error\":\"Token não enviado\"}");
            }
            
            try {
                String token = authHeader.substring(7);
                var decoded = JWTUtil.verifyToken(token);
                req.attribute("userEmail", decoded.getSubject());
            } catch (Exception e) {
                halt(403, "{\"error\":\"Token inválido\"}");
            }
        });
        
        // Rota de ping para verificação de status
        get("/api/ping", (req, res) -> {
            return gson.toJson(Map.of("status", "ok", "message", "pong"));
        });
        
        InteractionDAO interactionDAO = new InteractionDAO();
        
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

        RecommendationDAO recommendationDAO = new RecommendationDAO();

        post("/api/recommendation", (req, res) -> {
            res.type("application/json");

            JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
            int userId = body.get("userId").getAsInt();

            List<Interaction> interacoes = interactionDAO.getInteractionsByUserId(userId);

            if (interacoes.size() < 5) {
                res.status(400);
                return gson.toJson(Map.of("error", "Usuário precisa ter ao menos 5 interações"));
            }

            try {
                String recomendacao = AIService.gerarRecomendacao(interacoes);
                boolean sucesso = interactionDAO.clear(userId);
        
                if (!sucesso) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Erro ao limpar interações"));
                }
        
                res.status(200);
                return gson.toJson(Map.of("status", "ok", "recomendacao", recomendacao));
        
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Erro ao gerar recomendação"));
            }
        });

    }
}