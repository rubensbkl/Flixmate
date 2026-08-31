package app.controller;

import dao.PasswordResetDAO;
import dao.UserDAO;
import model.User;
import service.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/password")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    @Autowired
    private PasswordResetDAO passwordResetDAO;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${mail.from}")
    private String mailFrom;

    @Value("${frontend.url}")
    private String frontendUrl;

    private final Gson gson = new Gson();

    @PostMapping("/forgot")
    public ResponseEntity<String> forgotPassword(@RequestBody String body) {
        try {
            JsonObject req = JsonParser.parseString(body).getAsJsonObject();
            String email = req.has("email") ? req.get("email").getAsString() : null;

            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "Email não fornecido")));
            }

            User user = userService.getUserByEmail(email);
            if (user == null) {
                // Não revelar que o usuário não existe por segurança
                return ResponseEntity.ok(gson.toJson(Map.of("status", "success", "message", "Se o email estiver cadastrado, um link de recuperação será enviado.")));
            }

            String token = UUID.randomUUID().toString();
            Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + 3600 * 1000); // 1 hora

            if (passwordResetDAO.createResetToken(user.getId(), token, expiresAt)) {
                sendResetEmail(user.getEmail(), token);
                return ResponseEntity.ok(gson.toJson(Map.of("status", "success", "message", "Se o email estiver cadastrado, um link de recuperação será enviado.")));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(gson.toJson(Map.of("error", "Erro ao gerar token de recuperação")));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro interno no servidor")));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestBody String body) {
        try {
            JsonObject req = JsonParser.parseString(body).getAsJsonObject();
            String token = req.has("token") ? req.get("token").getAsString() : null;
            String newPassword = req.has("newPassword") ? req.get("newPassword").getAsString() : null;

            if (token == null || newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "Dados inválidos ou senha muito curta")));
            }

            int userId = passwordResetDAO.getUserIdByToken(token);
            if (userId == -1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(gson.toJson(Map.of("error", "Token inválido ou expirado")));
            }

            User user = userService.getUserById(userId);
            if (user != null) {
                user.setPassword(newPassword);
                if (userDAO.update(user)) {
                    passwordResetDAO.markTokenAsUsed(token);
                    return ResponseEntity.ok(gson.toJson(Map.of("status", "success", "message", "Senha redefinida com sucesso")));
                }
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao redefinir a senha")));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro interno no servidor")));
        }
    }

    private void sendResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject("FlixMate - Redefinição de Senha");
        
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        message.setText("Olá!\n\nVocê solicitou a redefinição da sua senha no FlixMate.\n" +
                "Clique no link abaixo para criar uma nova senha (o link expira em 1 hora):\n\n" +
                resetLink + "\n\nSe você não solicitou isso, pode ignorar este email.");
        
        mailSender.send(message);
    }
}
