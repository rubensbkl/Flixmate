package app;

import static spark.Spark.*;
import com.google.gson.Gson;
import dao.UserDAO;
import model.User;
import java.util.HashMap;
import java.util.Map;

public class Main {

//    private static UserService userService = new UserService();

    public static void main(String[] args) {
    	
        port(6789);

        staticFiles.location("/public");
        
        UserDAO userDAO = new UserDAO();
        Gson gson = new Gson();
      
     // Redirecionar para login se não autenticado
        before((req, res) -> {
            if (!req.pathInfo().equals("/login") && !req.pathInfo().equals("/register")) {
                if (req.session().attribute("user") == null) {
                    res.redirect("/login");
                }
            }
        });

        // Rota de login
        post("/login", (req, res) -> {
            User user = gson.fromJson(req.body(), User.class);
            if (userDAO.auth(user.getEmail(), user.getPassword())) {
                req.session().attribute("user", user.getEmail());
                return gson.toJson(Map.of("status", "success"));
            } else {
                return gson.toJson(Map.of("status", "error", "message", "Credenciais inválidas"));
            }
        });

        // Rota de registro
        post("/register", (req, res) -> {
            User user = gson.fromJson(req.body(), User.class);
            if (userDAO.insert(user)) {
                return gson.toJson(Map.of("status", "success"));
            } else {
                return gson.toJson(Map.of("status", "error", "message", "Falha no registro"));
            }
        });

        // Logout
        get("/logout", (req, res) -> {
            req.session().removeAttribute("user");
            res.redirect("/login");
            return null;
        });

        // Página inicial (somente para usuários logados)
        get("/", (req, res) -> "Bem-vindo ao CineMatch!");
    }
}