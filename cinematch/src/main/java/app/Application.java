package app;

import static spark.Spark.*;
import com.google.gson.Gson;
import dao.UserDAO;
import model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Content-Length, Accept, Origin");
            response.type("application/json");
        });


        UserDAO userDAO = new UserDAO();
        Gson gson = new Gson();

        // Rota API para dados de filmes
        get("/api/movies/trending", (req, res) -> {
            // Aqui você chamaria sua API de filmes (TMDB, IMDB, etc)
            // Isso é apenas um exemplo simulado
            Map<String, Object> movie1 = Map.of(
                    "id", 1,
                    "title", "Inception",
                    "poster", "https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg",
                    "rating", 8.8
            );
            Map<String, Object> movie2 = Map.of(
                    "id", 2,
                    "title", "The Matrix",
                    "poster", "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg",
                    "rating", 8.7
            );
            return gson.toJson(List.of(movie1, movie2));
        });

        // Rota API para likes de filmes
        post("/api/movies/like", (req, res) -> {
            // Exemplo de como processar um like
            Map<String, Object> requestBody = gson.fromJson(req.body(), Map.class);
            int movieId = ((Double) requestBody.get("movieId")).intValue();
            String userEmail = req.session().attribute("user");

            // Aqui você salvaria o like no banco de dados

            return gson.toJson(Map.of("status", "success"));
        });

        // Rota API para recomendações de filmes
        get("/api/movies/recommendations", (req, res) -> {
            String userEmail = req.session().attribute("user");
            // Aqui você chamaria sua API de IA para gerar recomendações
            // Baseadas no perfil do usuário

            return gson.toJson(Map.of("status", "success", "movies", new ArrayList<>()));
        });

        // Rotas de autenticação
        post("/api/login", (req, res) -> {
            User user = gson.fromJson(req.body(), User.class);
            if (userDAO.auth(user.getEmail(), user.getPassword())) {
                req.session().attribute("user", user.getEmail());
                return gson.toJson(Map.of("status", "success"));
            } else {
                return gson.toJson(Map.of("status", "error", "message", "Credenciais inválidas"));
            }
        });

        post("/api/register", (req, res) -> {
            User user = gson.fromJson(req.body(), User.class);
            if (userDAO.insert(user)) {
                return gson.toJson(Map.of("status", "success"));
            } else {
                return gson.toJson(Map.of("status", "error", "message", "Falha no registro"));
            }
        });

        get("/api/logout", (req, res) -> {
            req.session().removeAttribute("user");
            return gson.toJson(Map.of("status", "success"));
        });

        // Rota para servir a aplicação React para qualquer outra rota
        get("/*", (req, res) -> {
            res.redirect("/index.html");
            return null;
        });
    }
}