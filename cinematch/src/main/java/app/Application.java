package app;

import static spark.Spark.*;
import com.google.gson.Gson;
import dao.UserDAO;
import model.User;
import java.util.HashMap;
import java.util.Map;

public class Application {

//    private static UserService userService = new UserService();

    public static void main(String[] args) {
    	
        port(6789);

        staticFiles.location("/public");
        staticFiles.externalLocation("webjars");

        UserDAO userDAO = new UserDAO();
        Gson gson = new Gson();

        // Página inicial (somente para usuários logados)
        get("/", (req, res) -> {
            res.redirect("/index.html");
            return  null;
        });
      
     // Redirecionar para login se não autenticado
        before((req, res) -> {
            if (!req.pathInfo().equals("/login") && !req.pathInfo().equals("/register")) {
                if (req.session().attribute("user") == null) {
                    res.redirect("/login");
                }
            }
        });


        get("/webjars/*", (req, res) -> {
            // Pega o caminho do WebJar
            String path = "/META-INF/resources/webjars/" + req.splat()[0];
            return Application.class.getResourceAsStream(path);
        });

        // Rota de login
        post("/login", (req, res) -> {
            User user = gson.fromJson(req.body(), User.class);
            if (userDAO.auth(user.getEmail(), user.getPassword())) {
                req.session().attribute("user", user.getEmail());
                System.out.println("User email set in session: " + req.session().attribute("user"));
                return gson.toJson(Map.of("status", "success"));
            } else {
                return gson.toJson(Map.of("status", "error", "message", "Credenciais inválidas"));
            }
        });

        // Route to check session attribute
        get("/check-session", (req, res) -> {
            String userEmail = req.session().attribute("user");
            if (userEmail != null) {
                return "User email in session: " + userEmail;
            } else {
                return "No user email in session";
            }
        });

        get("/login", (req, res) -> {
            res.redirect("/login.html");
            return null;
        });

        // Rota de registro
        post("/register", (req, res) -> {
            System.out.println(req.body());
            User user = gson.fromJson(req.body(), User.class);
            if (userDAO.insert(user)) {
                return gson.toJson(Map.of("status", "success"));
            } else {
                return gson.toJson(Map.of("status", "error", "message", "Falha no registro"));
            }
        });

        get("/register", (req, res) -> {
            res.redirect("/register.html");
            return null;
        });

        // Logout
        get("/logout", (req, res) -> {
            req.session().removeAttribute("user");
            res.redirect("/login");
            return null;
        });

    }
}