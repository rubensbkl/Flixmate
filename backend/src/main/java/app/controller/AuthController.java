package app.controller;

import model.User;
import service.UserGenreService;
import service.UserService;
import util.JWTUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Simplificação para substituir o CORS antigo
public class AuthController {

    @org.springframework.beans.factory.annotation.Autowired
    private UserService userService;
    @org.springframework.beans.factory.annotation.Autowired
    private UserGenreService userGenreService;
    @org.springframework.beans.factory.annotation.Autowired
    private JWTUtil jwt;
    private final Gson gson = new Gson();

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody String body) {
        User user = gson.fromJson(body, User.class);
        if (userService.authenticateUser(user.getEmail(), user.getPassword())) {
            User fullUser = userService.getUserByEmail(user.getEmail());
            String token = jwt.generateToken(user.getEmail(), fullUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", Map.of(
                    "firstName", fullUser.getFirstName(),
                    "lastName", fullUser.getLastName(),
                    "email", fullUser.getEmail(),
                    "isAdmin", fullUser.isAdmin(),
                    "googleConnected", fullUser.isGoogleConnected(),
                    "githubConnected", fullUser.isGithubConnected(),
                    "hasPassword", fullUser.hasPassword()));

            return ResponseEntity.ok(gson.toJson(response));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(gson.toJson(Map.of("error", "Credenciais inválidas")));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody String body) {
        JsonObject requestBody = JsonParser.parseString(body).getAsJsonObject();

        User user = new User();
        user.setFirstName(requestBody.get("firstName").getAsString());
        user.setLastName(requestBody.get("lastName").getAsString());
        user.setEmail(requestBody.get("email").getAsString());
        user.setPassword(requestBody.get("password").getAsString());
        user.setGender(requestBody.get("gender").getAsString().charAt(0));

        if (user.getEmail() == null || user.getPassword() == null ||
                user.getFirstName() == null || user.getLastName() == null ||
                user.getGender() == '\0') {
            return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "Todos os campos são obrigatórios")));
        }

        if (userService.getUserByEmail(user.getEmail()) != null) {
            return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "Email já cadastrado")));
        }

        JsonArray favoriteGenresArray = requestBody.getAsJsonArray("favoriteGenres");
        List<Integer> favoriteGenres = new ArrayList<>();
        if (favoriteGenresArray != null) {
            for (int i = 0; i < favoriteGenresArray.size(); i++) {
                favoriteGenres.add(favoriteGenresArray.get(i).getAsInt());
            }
        }


        if (userService.insertUser(user)) {
            User fullUser = userService.getUserByEmail(user.getEmail());
            boolean allGenresInserted = true;
            for (Integer genreId : favoriteGenres) {
                if (!userGenreService.addPreferredGenre(fullUser.getId(), genreId)) {
                    allGenresInserted = false;
                }
            }

            String token = jwt.generateToken(fullUser.getEmail(), fullUser.getId());
            Map<String, Object> userData = Map.of(
                    "firstName", fullUser.getFirstName(),
                    "lastName", fullUser.getLastName(),
                    "email", fullUser.getEmail(),
                    "gender", fullUser.getGender(),
                    "isAdmin", fullUser.isAdmin(),
                    "googleConnected", fullUser.isGoogleConnected(),
                    "githubConnected", fullUser.isGithubConnected(),
                    "hasPassword", fullUser.hasPassword());

            if (!allGenresInserted) {
                return ResponseEntity.status(HttpStatus.CREATED).body(gson.toJson(Map.of(
                        "status", "success",
                        "token", token,
                        "user", userData,
                        "warning", "Alguns gêneros favoritos podem não ter sido salvos")));
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body(gson.toJson(Map.of(
                        "status", "success",
                        "token", token,
                        "user", userData)));
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(gson.toJson(Map.of("error", "Erro ao criar conta")));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(gson.toJson(Map.of("valid", false, "error", "Token não fornecido")));
        }

        try {
            String token = authHeader.substring(7);
            var decoded = jwt.verifyToken(token);
            int userId = decoded.getClaim("userId").asInt();

            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(gson.toJson(Map.of("valid", false, "error", "Usuário não encontrado")));
            }

            Map<String, Object> userData = Map.of(
                    "id", user.getId(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "email", user.getEmail(),
                    "isAdmin", user.isAdmin(),
                    "googleConnected", user.isGoogleConnected(),
                    "githubConnected", user.isGithubConnected(),
                    "hasPassword", user.hasPassword());

            return ResponseEntity.ok(gson.toJson(Map.of("valid", true, "user", userData)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(gson.toJson(Map.of("valid", false, "error", "Token inválido")));
        }
    }
}
