package app;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

import java.util.Map;

import com.google.gson.Gson;

import dao.UserDAO;
import model.User;
import util.JWTUtil;

    public class Application {

        public static void main(String[] args) {

            port(6789);

            staticFiles.location("/public");
            staticFiles.externalLocation("webjars");


            // Configurações CORS para permitir requisições do frontend React
            options("/*", (request, response) -> {
                String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
                if (accessControlRequestHeaders != null) {
                    response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
                }

                String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
                if (accessControlRequestMethod != null) {
                    response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
                }

                return "OK";
            });

            // Configuração CORS correta
            before((req, res) -> {
                res.header("Access-Control-Allow-Origin", req.headers("Origin"));
                res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                res.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept");
                res.header("Access-Control-Allow-Credentials", "true");
                res.type("application/json");
            });

            UserDAO userDAO = new UserDAO();
            Gson gson = new Gson();

            // Rota de login
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

            // Rota de registro
            post("/api/register", (req, res) -> {
                User user = gson.fromJson(req.body(), User.class);
                if (userDAO.insert(user)) {
                    res.status(201);
                    return gson.toJson(Map.of("status", "success"));
                }

                res.status(400);
                return gson.toJson(Map.of("error", "Erro ao criar usuário"));
            });

            // Middleware para proteger rotas
            before("/api/protected/*", (req, res) -> {
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
    
            // Fallback for React routing (Serve index.html for all unknown routes)
            get("/*", (req, res) -> {
                return gson.toJson(Map.of("status", "success"));
            });

        }
    }