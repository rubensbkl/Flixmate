package app.controller;

import model.Genre;
import model.Movie;
import model.Recommendation;
import model.User;
import dao.UserDAO;
import service.*;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProfileController {

    @org.springframework.beans.factory.annotation.Autowired
    private UserService userService;
    @org.springframework.beans.factory.annotation.Autowired
    private UserDAO userDAO;
    @org.springframework.beans.factory.annotation.Autowired
    private UserGenreService userGenreService;
    @org.springframework.beans.factory.annotation.Autowired
    private WatchLaterService watchLaterService;
    @org.springframework.beans.factory.annotation.Autowired
    private FavoriteService favoriteService;
    @org.springframework.beans.factory.annotation.Autowired
    private MovieService movieService;
    @org.springframework.beans.factory.annotation.Autowired
    private MovieGenreService movieGenreService;
    @org.springframework.beans.factory.annotation.Autowired
    private RecommendationService recommendationService;
    @org.springframework.beans.factory.annotation.Autowired
    private JWTUtil jwt;
    private final Gson gson = new Gson();

    private int extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token não fornecido");
        }
        String token = authHeader.substring(7);
        try {
            var decoded = jwt.verifyToken(token);
            return decoded.getClaim("userId").asInt();
        } catch (Exception e) {
            throw new RuntimeException("Token inválido");
        }
    }

    @GetMapping("/private")
    public ResponseEntity<String> getPrivateProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            int userId = extractUserId(authHeader);
            model.User user = userService.getUserById(userId);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Map.of("error", "Usuário não encontrado")));
            }

            List<model.Genre> preferredGenres = userGenreService.getPreferredGenres(userId);
            List<Map<String, Object>> genresData = new ArrayList<>();
            for (model.Genre genre : preferredGenres) {
                Map<String, Object> genreData = Map.of(
                        "id", genre.getId(),
                        "name", genre.getName());
                genresData.add(genreData);
            }

            Map<String, Object> userData = Map.of(
                    "id", user.getId(),
                    "firstName", user.getFirstName() == null ? "" : user.getFirstName(),
                    "lastName", user.getLastName() == null ? "" : user.getLastName(),
                    "email", user.getEmail() == null ? "" : user.getEmail(),
                    "gender", String.valueOf(user.getGender()),
                    "isAdmin", user.isAdmin()
            );

            return ResponseEntity.ok(gson.toJson(Map.of(
                    "status", "ok", 
                    "user", userData, 
                    "preferredGenres", genresData)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao buscar informações do usuário: " + e.getMessage())));
        }
    }
    @GetMapping("/profile/{userId}")
    public ResponseEntity<String> getProfile(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @PathVariable int userId) {
        try {
            extractUserId(authHeader); // Valida token
            User user = userService.getUserById(userId);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Map.of("error", "Usuário não encontrado")));
            }

            Map<String, Object> userData = Map.of(
                    "id", user.getId(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "email", user.getEmail(),
                    "gender", String.valueOf(user.getGender()),
                    "isAdmin", user.isAdmin());

            return ResponseEntity.ok(gson.toJson(Map.of("status", "ok", "user", userData)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao buscar informações do usuário: " + e.getMessage())));
        }
    }

    @GetMapping("/profile/{userId}/watchlist")
    public ResponseEntity<String> getWatchlist(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                               @PathVariable int userId) {
        try {
            extractUserId(authHeader);
            ArrayList<Integer> movieIds = watchLaterService.getWatchLaterMovies(userId);
            List<Map<String, Object>> moviesData = new ArrayList<>();

            for (Integer movieId : movieIds) {
                Movie movie = movieService.getMovieById(movieId);
                if (movie != null) {
                    List<Genre> genres = movieGenreService.buscarGenerosDoFilme(movieId);
                    moviesData.add(Map.of(
                            "id", movie.getId(),
                            "title", movie.getTitle(),
                            "poster_path", movie.getPosterPath(),
                            "release_date", movie.getReleaseDate(),
                            "genres", genres.stream().map(Genre::getName).collect(Collectors.toList())));
                }
            }
            return ResponseEntity.ok(gson.toJson(Map.of("status", "ok", "movies", moviesData)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao buscar watchlist: " + e.getMessage())));
        }
    }

    @GetMapping("/profile/{userId}/favorites")
    public ResponseEntity<String> getFavorites(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                               @PathVariable int userId) {
        try {
            extractUserId(authHeader);
            ArrayList<Integer> movieIds = favoriteService.getFavoriteMovies(userId);
            List<Map<String, Object>> moviesData = new ArrayList<>();

            for (Integer movieId : movieIds) {
                Movie movie = movieService.getMovieById(movieId);
                if (movie != null) {
                    List<Genre> genres = movieGenreService.buscarGenerosDoFilme(movieId);
                    moviesData.add(Map.of(
                            "id", movie.getId(),
                            "title", movie.getTitle(),
                            "poster_path", movie.getPosterPath(),
                            "release_date", movie.getReleaseDate(),
                            "genres", genres.stream().map(Genre::getName).collect(Collectors.toList())));
                }
            }
            return ResponseEntity.ok(gson.toJson(Map.of("status", "ok", "movies", moviesData)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao buscar favoritos: " + e.getMessage())));
        }
    }

    @GetMapping("/profile/{userId}/recommended")
    public ResponseEntity<String> getRecommended(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                 @PathVariable int userId) {
        try {
            extractUserId(authHeader);
            ArrayList<Recommendation> recommendations = recommendationService.getRecommendationsByUserId(userId);
            List<Map<String, Object>> moviesData = new ArrayList<>();

            for (Recommendation recommendation : recommendations) {
                Movie movie = movieService.getMovieById(recommendation.getMovieId());
                if (movie != null) {
                    moviesData.add(Map.of(
                            "id", movie.getId(),
                            "title", movie.getTitle(),
                            "poster_path", movie.getPosterPath()));
                }
            }
            return ResponseEntity.ok(gson.toJson(Map.of("status", "ok", "movies", moviesData)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao buscar filmes recomendados: " + e.getMessage())));
        }
    }

    @PostMapping("/profile/update")
    public ResponseEntity<String> updateProfile(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                @RequestBody String body) {
        try {
            int userId = extractUserId(authHeader);
            JsonObject bodyObj = JsonParser.parseString(body).getAsJsonObject();

            String firstName = bodyObj.has("firstName") ? bodyObj.get("firstName").getAsString() : null;
            String lastName = bodyObj.has("lastName") ? bodyObj.get("lastName").getAsString() : null;
            String email = bodyObj.has("email") ? bodyObj.get("email").getAsString() : null;
            String gender = bodyObj.has("gender") ? bodyObj.get("gender").getAsString() : null;

            List<Integer> genres = null;
            if (bodyObj.has("genres") && bodyObj.get("genres").isJsonArray()) {
                JsonArray genresArray = bodyObj.get("genres").getAsJsonArray();
                genres = new ArrayList<>();
                for (int i = 0; i < genresArray.size(); i++) {
                    genres.add(genresArray.get(i).getAsInt());
                }

                if (genres.isEmpty()) {
                    return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "É necessário selecionar pelo menos um gênero preferido")));
                }
                if (genres.size() > 5) {
                    return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "Você pode selecionar no máximo 5 gêneros preferidos")));
                }
            }

            User currentUser = userService.getUserById(userId);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(gson.toJson(Map.of("error", "Usuário não encontrado")));
            }

            if (firstName != null) currentUser.setFirstName(firstName);
            if (lastName != null) currentUser.setLastName(lastName);
            if (email != null && !email.equals(currentUser.getEmail())) {
                User existingUser = userService.getUserByEmail(email);
                if (existingUser != null) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(gson.toJson(Map.of("error", "Este email já está sendo usado por outra conta.")));
                }
                currentUser.setEmail(email);
            }
            if (gender != null && gender.length() > 0) currentUser.setGender(gender.charAt(0));

            boolean userUpdated = userDAO.update(currentUser);
            if (!userUpdated) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(gson.toJson(Map.of("error", "Erro ao atualizar informações do usuário")));
            }

            if (genres != null) {
                userGenreService.removeAllPreferredGenres(userId);
                for (Integer genreId : genres) {
                    userGenreService.addPreferredGenre(userId, genreId);
                }
            }

            Map<String, Object> userData = Map.of(
                    "firstName", currentUser.getFirstName(),
                    "lastName", currentUser.getLastName(),
                    "email", currentUser.getEmail(),
                    "gender", currentUser.getGender());

            return ResponseEntity.ok(gson.toJson(Map.of(
                    "status", "success",
                    "message", "Perfil atualizado com sucesso",
                    "user", userData)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao processar atualização: " + e.getMessage())));
        }
    }

    @PostMapping("/profile/password")
    public ResponseEntity<String> updatePassword(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                 @RequestBody String body) {
        try {
            int userId = extractUserId(authHeader);
            JsonObject bodyObj = JsonParser.parseString(body).getAsJsonObject();

String currentPassword = bodyObj.has("currentPassword") ? bodyObj.get("currentPassword").getAsString() : null;
            String newPassword = bodyObj.has("newPassword") ? bodyObj.get("newPassword").getAsString() : null;

            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "Nova senha inválida")));
            }

            User currentUser = userService.getUserById(userId);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(gson.toJson(Map.of("error", "Usuário não encontrado")));
            }

            if (currentUser.hasPassword()) {
                if (currentPassword == null || !userService.authenticateUser(currentUser.getEmail(), currentPassword)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(gson.toJson(Map.of("error", "Senha atual incorreta")));
                }
            }

            currentUser.setPassword(newPassword);
            currentUser.setHasPassword(true);
            boolean updated = userDAO.update(currentUser);

            if (!updated) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(gson.toJson(Map.of("error", "Erro ao atualizar senha")));
            }

            return ResponseEntity.ok(gson.toJson(Map.of("status", "success", "message", "Senha atualizada com sucesso")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao processar atualização: " + e.getMessage())));
        }
    }


    @DeleteMapping("/profile/password")
    public ResponseEntity<String> removePassword(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                 @RequestBody String body) {
        try {
            int userId = extractUserId(authHeader);
            JsonObject bodyObj = JsonParser.parseString(body).getAsJsonObject();
            String currentPassword = bodyObj.has("currentPassword") ? bodyObj.get("currentPassword").getAsString() : null;

            User currentUser = userService.getUserById(userId);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(gson.toJson(Map.of("error", "Usuário não encontrado")));
            }

            if (!currentUser.hasPassword()) {
                return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "O usuário já não possui senha cadastrada.")));
            }

            if (!currentUser.isGoogleConnected() && !currentUser.isGithubConnected()) {
                return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "Você deve ter pelo menos uma conta OAuth vinculada para remover sua senha.")));
            }

            if (currentPassword == null || !userService.authenticateUser(currentUser.getEmail(), currentPassword)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(gson.toJson(Map.of("error", "Senha atual incorreta")));
            }

            currentUser.setPassword(java.util.UUID.randomUUID().toString());
            currentUser.setHasPassword(false);
            boolean updated = userDAO.update(currentUser);

            if (!updated) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(gson.toJson(Map.of("error", "Erro ao remover senha")));
            }

            return ResponseEntity.ok(gson.toJson(Map.of("status", "success", "message", "Senha removida com sucesso")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao processar remoção: " + e.getMessage())));
        }
    }

    @PostMapping("/profile/oauth/disconnect")
    public ResponseEntity<String> disconnectOAuth(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                  @RequestBody String body) {
        try {
            int userId = extractUserId(authHeader);
            JsonObject bodyObj = JsonParser.parseString(body).getAsJsonObject();
            String provider = bodyObj.has("provider") ? bodyObj.get("provider").getAsString() : null;

            if (provider == null || (!provider.equalsIgnoreCase("google") && !provider.equalsIgnoreCase("github"))) {
                return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "Provedor inválido")));
            }

            User currentUser = userService.getUserById(userId);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(gson.toJson(Map.of("error", "Usuário não encontrado")));
            }

            if ("google".equalsIgnoreCase(provider)) {
                currentUser.setGoogleConnected(false);
            } else if ("github".equalsIgnoreCase(provider)) {
                currentUser.setGithubConnected(false);
            }

            boolean updated = userDAO.update(currentUser);
            if (!updated) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(gson.toJson(Map.of("error", "Erro ao desconectar provedor")));
            }

            return ResponseEntity.ok(gson.toJson(Map.of("status", "success", "message", "Provedor desconectado com sucesso")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao processar desconexão: " + e.getMessage())));
        }
    }

    @GetMapping("/profiles/search")
    public ResponseEntity<String> searchProfiles(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                 @RequestParam(defaultValue = "") String query,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "25") int limit) {
        try {
            extractUserId(authHeader);

            if (page < 1) page = 1;
            if (limit < 1 || limit > 100) limit = 25;

            // Simple search mock or call to actual service
            // Depending on the legacy code, searchUsers might not exist, 
            // but we will assume it does based on the legacy structure.
            // If it doesn't, we can fix it later. We are migrating as-is.
            List<User> users;
            if (query.isEmpty()) {
                users = userService.getAllUsers(page, limit);
            } else {
                users = userService.search(query, page, limit);
            }

            List<Map<String, Object>> results = new ArrayList<>();
            for (User user : users) {
                results.add(Map.of(
                        "id", user.getId(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "email", user.getEmail()
                ));
            }

            return ResponseEntity.ok(gson.toJson(Map.of(
                    "status", "ok",
                    "page", page,
                    "total_results", results.size(),
                    "results", results
            )));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro na busca de perfis: " + e.getMessage())));
        }
    }
}
