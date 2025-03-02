    package app;

    import static spark.Spark.*;
    import com.google.gson.Gson;
    import dao.UserDAO;
    import model.User;

    import java.nio.file.Files;
    import java.nio.file.Paths;
    import java.util.*;

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

            before((req, res) -> {
                res.header("Set-Cookie", "JSESSIONID=" + req.session().id() + "; Path=/; HttpOnly; Secure; SameSite=Strict");
            });

            before((req, res) -> {
                res.header("Access-Control-Allow-Origin", req.headers("Origin"));
                res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                res.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept");
                res.header("Access-Control-Allow-Credentials", "true");
                res.type("application/json");
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

            Map<String, Integer> loginAttempts = new HashMap<>();

            post("/api/login", (req, res) -> {
                String ip = req.ip();
                loginAttempts.put(ip, loginAttempts.getOrDefault(ip, 0) + 1);

                if (loginAttempts.get(ip) > 5) {
                    res.status(429);
                    return gson.toJson(Map.of("status", "error", "message", "Too many login attempts"));
                }

                User user = gson.fromJson(req.body(), User.class);
                if (userDAO.auth(user.getEmail(), user.getPassword())) {
                    req.session().attribute("user", user.getEmail());

                    // ✅ Generate CSRF token and store it in session
                    String csrfToken = UUID.randomUUID().toString();
                    req.session().attribute("csrf_token", csrfToken);

                    loginAttempts.put(ip, 0); // Reset on success
                    return gson.toJson(Map.of("status", "success", "csrf_token", csrfToken));
                } else {
                    return gson.toJson(Map.of("status", "error", "message", "Invalid credentials"));
                }
            });

            get("/api/session", (req, res) -> {
                String userEmail = req.session().attribute("user");
                if (userEmail != null) {
                    return gson.toJson(Map.of("authenticated", true, "user", userEmail));
                } else {
                    return gson.toJson(Map.of("authenticated", false));
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

            post("/api/logout", (req, res) -> {
                String csrfToken = req.headers("X-CSRF-Token");
                String sessionToken = req.session().attribute("csrf_token");

                if (sessionToken == null || !sessionToken.equals(csrfToken)) {
                    res.status(403);
                    return gson.toJson(Map.of("status", "error", "message", "Invalid CSRF Token"));
                }

                req.session().removeAttribute("user");
                req.session().removeAttribute("csrf_token"); // ✅ Clear CSRF token on logout
                req.session().invalidate();
                return gson.toJson(Map.of("status", "success"));
            });


            // Fallback for React routing (Serve index.html for all unknown routes)
            get("/*", (req, res) -> {
                res.type("text/html");
                return new String(Files.readAllBytes(Paths.get("src/main/frontend/build/index.html")));
            });

        }
    }