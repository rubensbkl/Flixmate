package app.controller;

import model.User;
import service.UserService;
import util.JWTUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @org.springframework.beans.factory.annotation.Autowired
    private UserService userService;
    @org.springframework.beans.factory.annotation.Autowired
    private JWTUtil jwt;
    private final Gson gson = new Gson();

    private boolean verifyAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token não fornecido");
        }
        String token = authHeader.substring(7);
        String email = jwt.getEmail(token);
        User user = userService.getUserByEmail(email);
        if (user == null || !user.isAdmin()) {
            throw new RuntimeException("Acesso negado: Usuário não é administrador");
        }
        return true;
    }

    @GetMapping("/users")
    public ResponseEntity<String> getAllUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            verifyAdmin(authHeader);
            List<User> daoUsers = userService.getAll();
            List<Map<String, Object>> responseUsers = new ArrayList<>();
            for (model.User u : daoUsers) {
                responseUsers.add(Map.of(
                        "id", u.getId(),
                        "firstName", u.getFirstName() == null ? "" : u.getFirstName(),
                        "lastName", u.getLastName() == null ? "" : u.getLastName(),
                        "email", u.getEmail(),
                        "isAdmin", u.isAdmin()
                ));
            }
            return ResponseEntity.ok(gson.toJson(Map.of("status", "ok", "users", responseUsers)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @PathVariable int id) {
        try {
            verifyAdmin(authHeader);
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                return ResponseEntity.ok(gson.toJson(Map.of("status", "ok")));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Map.of("error", "Usuário não encontrado ou não pôde ser deletado")));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @PutMapping("/users/{id}/password")
    public ResponseEntity<String> updatePassword(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                 @PathVariable int id,
                                                 @RequestBody String body) {
        try {
            verifyAdmin(authHeader);
            JsonObject requestBody = JsonParser.parseString(body).getAsJsonObject();
            String newPassword = requestBody.get("password").getAsString();
            
            User user = userService.getUserById(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Map.of("error", "Usuário não encontrado")));
            }
            user.setPassword(newPassword);
            userService.updateUser(user);
            return ResponseEntity.ok(gson.toJson(Map.of("status", "ok")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(gson.toJson(Map.of("error", e.getMessage())));
        }
    }
}
