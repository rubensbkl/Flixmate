package app.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import service.UserService;
import util.JWTUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/oauth")
@CrossOrigin(origins = "*")
public class OAuthController {

    @Value("${oauth.google.clientId}")
    private String googleClientId;

    @Value("${oauth.google.clientSecret}")
    private String googleClientSecret;

    @Value("${oauth.github.clientId}")
    private String githubClientId;

    @Value("${oauth.github.clientSecret}")
    private String githubClientSecret;

    @Value("${oauth.google.redirectUri}")
    private String redirectUri;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtil jwtUtil;

    private final Gson gson = new Gson();
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/{provider}/url")
    public ResponseEntity<String> getAuthUrl(@PathVariable String provider) {
        String url = "";
        if ("google".equalsIgnoreCase(provider)) {
            url = "https://accounts.google.com/o/oauth2/v2/auth?" +
                    "client_id=" + googleClientId +
                    "&redirect_uri=" + redirectUri +
                    "&response_type=code" +
                    "&scope=email profile" +
                    "&state=google" +
                    "&access_type=offline";
        } else if ("github".equalsIgnoreCase(provider)) {
            url = "https://github.com/login/oauth/authorize?" +
                    "client_id=" + githubClientId +
                    "&redirect_uri=" + redirectUri +
                    "&state=github" +
                    "&scope=user:email read:user";
        } else {
            return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "Provedor inválido")));
        }

        return ResponseEntity.ok(gson.toJson(Map.of("url", url)));
    }

    @PostMapping("/{provider}/callback")
    public ResponseEntity<String> callback(@PathVariable String provider, @RequestBody Map<String, String> body, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String code = body.get("code");
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "Código não fornecido")));
        }

        try {
            OAuthUserInfo userInfo = null;

            if ("google".equalsIgnoreCase(provider)) {
                userInfo = getGoogleUserInfo(code);
            } else if ("github".equalsIgnoreCase(provider)) {
                userInfo = getGithubUserInfo(code);
            } else {
                return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "Provedor inválido")));
            }

            if (userInfo == null || userInfo.getEmail() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(gson.toJson(Map.of("error", "Falha ao obter informações do usuário")));
            }


            User user = userService.getUserByEmail(userInfo.getEmail());

            int loggedInUserId = -1;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String authToken = authHeader.substring(7);
                    loggedInUserId = jwtUtil.verifyToken(authToken).getClaim("userId").asInt();
                } catch (Exception e) {}
            }

            String action = body.containsKey("action") ? body.get("action") : "login";
            boolean isGoogle = "google".equalsIgnoreCase(provider);
            boolean isGithub = "github".equalsIgnoreCase(provider);

            if (action.equals("connect")) {
                if (loggedInUserId == -1) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(gson.toJson(Map.of("error", "Você precisa estar logado para conectar uma conta.")));
                }
                if (user != null && user.getId() != loggedInUserId) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(gson.toJson(Map.of("error", "Esta conta OAuth já está vinculada a outro usuário.")));
                }
                user = userService.getUserById(loggedInUserId);
                if (user != null) {
                    if (isGoogle) user.setGoogleConnected(true);
                    if (isGithub) user.setGithubConnected(true);
                    userService.updateUser(user);
                }
            } else if (action.equals("signup")) {
                if (user == null) {
                    user = new User();
                    user.setEmail(userInfo.getEmail());
                    user.setFirstName(userInfo.getFirstName());
                    user.setLastName(userInfo.getLastName() != null ? userInfo.getLastName() : "");
                    user.setPassword(java.util.UUID.randomUUID().toString());
                    user.setGender('O'); // Default
                    user.setHasPassword(false);
                    if (isGoogle) user.setGoogleConnected(true);
                    if (isGithub) user.setGithubConnected(true);
                    userService.insertUser(user);
                    user = userService.getUserByEmail(userInfo.getEmail());
                } else {
                    if ((isGoogle && user.isGoogleConnected()) || (isGithub && user.isGithubConnected())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(gson.toJson(Map.of("error", "Este OAuth já está cadastrado. Faça login em vez de criar uma nova conta.")));
                    } else {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(gson.toJson(Map.of("error", "Já existe uma conta com este e-mail. Faça login com senha e vincule nas configurações.")));
                    }
                }
            } else { // login
                if (user == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(gson.toJson(Map.of("error", "Não existe conta vinculada a este OAuth. Cadastre-se primeiro.")));
                } else {
                    if ((isGoogle && !user.isGoogleConnected()) || (isGithub && !user.isGithubConnected())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(gson.toJson(Map.of("error", "Conta não vinculada. Faça login com senha e vincule nas configurações.")));
                    }
                }
            }

            String token = jwtUtil.generateToken(user.getEmail(), user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", Map.of(
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "email", user.getEmail(),
                    "isAdmin", user.isAdmin(),
                    "googleConnected", user.isGoogleConnected(),
                    "githubConnected", user.isGithubConnected(),
                    "hasPassword", user.hasPassword()));

            return ResponseEntity.ok(gson.toJson(response));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(gson.toJson(Map.of("error", "Erro no processamento do OAuth: " + e.getMessage())));
        }
    }

    private OAuthUserInfo getGoogleUserInfo(String code) {
        // Trocar código por token
        String tokenUrl = "https://oauth2.googleapis.com/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", googleClientId);
        map.add("client_secret", googleClientSecret);
        map.add("code", code);
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(tokenUrl, request, String.class);
        
        JsonObject tokenJson = JsonParser.parseString(tokenResponse.getBody()).getAsJsonObject();
        String accessToken = tokenJson.get("access_token").getAsString();

        // Obter informações do usuário
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        
        HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<String> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userRequest, String.class);
        
        JsonObject userJson = JsonParser.parseString(userResponse.getBody()).getAsJsonObject();
        
        OAuthUserInfo info = new OAuthUserInfo();
        info.setEmail(userJson.get("email").getAsString());
        info.setFirstName(userJson.has("given_name") ? userJson.get("given_name").getAsString() : "Google User");
        info.setLastName(userJson.has("family_name") ? userJson.get("family_name").getAsString() : "");
        return info;
    }

    private OAuthUserInfo getGithubUserInfo(String code) {
        // Trocar código por token
        String tokenUrl = "https://github.com/login/oauth/access_token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", githubClientId);
        requestBody.add("client_secret", githubClientSecret);
        requestBody.add("code", code);
        requestBody.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(tokenUrl, request, String.class);
        
        JsonObject tokenJson = JsonParser.parseString(tokenResponse.getBody()).getAsJsonObject();
        
        if (tokenJson.has("error")) {
            throw new RuntimeException("Erro ao obter token do GitHub: " + tokenJson.get("error_description").getAsString());
        }
        
        String accessToken = tokenJson.get("access_token").getAsString();

        // Obter informações do usuário
        String userInfoUrl = "https://api.github.com/user";
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        
        HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<String> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userRequest, String.class);
        
        JsonObject userJson = JsonParser.parseString(userResponse.getBody()).getAsJsonObject();
        
        // Obter e-mail (pois pode ser privado no objeto user principal)
        String emailUrl = "https://api.github.com/user/emails";
        ResponseEntity<String> emailResponse = restTemplate.exchange(emailUrl, HttpMethod.GET, userRequest, String.class);
        
        String email = "";
        try {
            com.google.gson.JsonArray emailsArray = JsonParser.parseString(emailResponse.getBody()).getAsJsonArray();
            for (int i = 0; i < emailsArray.size(); i++) {
                JsonObject em = emailsArray.get(i).getAsJsonObject();
                if (em.get("primary").getAsBoolean()) {
                    email = em.get("email").getAsString();
                    break;
                }
            }
        } catch(Exception e) {
            // Ignorar erro e usar o e-mail público se não encontrar os e-mails
            if (userJson.has("email") && !userJson.get("email").isJsonNull()) {
                email = userJson.get("email").getAsString();
            }
        }

        String name = userJson.has("name") && !userJson.get("name").isJsonNull() ? userJson.get("name").getAsString() : userJson.get("login").getAsString();
        String[] nameParts = name.split(" ", 2);
        
        OAuthUserInfo info = new OAuthUserInfo();
        info.setEmail(email);
        info.setFirstName(nameParts[0]);
        info.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        
        return info;
    }

    private static class OAuthUserInfo {
        private String email;
        private String firstName;
        private String lastName;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }
}
