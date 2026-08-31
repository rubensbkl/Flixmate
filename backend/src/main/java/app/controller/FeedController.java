package app.controller;

import model.Feedback;
import service.FeedbackService;
import service.MovieGenreService;
import service.MovieService;
import util.FlixAi;
import util.JWTUtil;
import util.TMDBUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FeedController {

    @org.springframework.beans.factory.annotation.Autowired
    private MovieService movieService;
    private final FlixAi flixAi = new FlixAi();
    @org.springframework.beans.factory.annotation.Autowired
    private TMDBUtil tmdb;
    @org.springframework.beans.factory.annotation.Autowired
    private FeedbackService feedbackService;
    @org.springframework.beans.factory.annotation.Autowired
    private MovieGenreService movieGenreService;
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

    @PostMapping("/feed")
    public ResponseEntity<String> getFeed(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                          @RequestBody String body) {
        try {
            int userId = extractUserId(authHeader);
            JsonObject requestBody = gson.fromJson(body, JsonObject.class);
            int page = requestBody.has("page") ? requestBody.get("page").getAsInt() : 1;

            final int NUM_CANDIDATOS = 500;
            int offset = (page - 1) * NUM_CANDIDATOS;
            List<Integer> candidatos = movieService.getUnratedMovieIds(userId, NUM_CANDIDATOS, offset);

            if (candidatos.isEmpty()) {
                // Banco vazio ou sem candidatos: retorna TMDB popular direto
                JsonArray discoverMovies = tmdb.getPopularMovies(page);
                List<JsonObject> finalMovies = new ArrayList<>();
                discoverMovies.forEach(item -> finalMovies.add(item.getAsJsonObject()));
                return ResponseEntity.ok(gson.toJson(Map.of("status", "ok", "movies", finalMovies)));
            }

            JsonObject aiResponse = flixAi.getFeed(userId, 20, candidatos);
            JsonArray aiMoviesArray = aiResponse.getAsJsonArray("all_recommendations");

            if (aiMoviesArray == null) {
                throw new RuntimeException("Feed da IA retornou vazio ou inválido.");
            }

            List<Integer> aiMovieIds = new ArrayList<>();
            aiMoviesArray.forEach(item -> {
                JsonArray pair = item.getAsJsonArray();
                int movieId = pair.get(0).getAsInt();
                aiMovieIds.add(movieId);
            });

            List<JsonObject> aiMoviesDetails = tmdb.getMoviesDetails(aiMovieIds);
            JsonArray discoverMovies = tmdb.getPopularMovies(page);

            Map<Integer, JsonObject> uniqueMoviesMap = new HashMap<>();
            aiMoviesDetails.forEach(movie -> {
                int id = movie.get("id").getAsInt();
                uniqueMoviesMap.put(id, movie);
            });

            discoverMovies.forEach(item -> {
                JsonObject movie = item.getAsJsonObject();
                int id = movie.get("id").getAsInt();
                uniqueMoviesMap.putIfAbsent(id, movie);
            });

            List<JsonObject> finalMovies = new ArrayList<>(uniqueMoviesMap.values());
            Collections.shuffle(finalMovies, new SecureRandom());

            return ResponseEntity.ok(gson.toJson(Map.of("status", "ok", "movies", finalMovies)));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao buscar filmes: " + e.getMessage())));
        }
    }

    @GetMapping("/rate/{movieId}")
    public ResponseEntity<String> getRate(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                          @PathVariable int movieId) {
        try {
            int userId = extractUserId(authHeader);
            Feedback feedback = feedbackService.getFeedback(userId, movieId);

            if (feedback != null) {
                return ResponseEntity.ok(gson.toJson(Map.of(
                        "movieId", movieId,
                        "currentRating", feedback.getFeedback()
                )));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Map.of("error", "Avaliação não encontrada")));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro no servidor: " + e.getMessage())));
        }
    }

    @DeleteMapping("/rate/{movieId}")
    public ResponseEntity<String> deleteRate(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @PathVariable int movieId) {
        try {
            int userId = extractUserId(authHeader);
            boolean removed = feedbackService.removeRating(userId, movieId);

            if (removed) {
                return ResponseEntity.ok(gson.toJson(Map.of("status", "Avaliação removida com sucesso")));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Map.of("error", "Avaliação não encontrada")));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro no servidor: " + e.getMessage())));
        }
    }

    @PostMapping("/rate")
    public ResponseEntity<String> postRate(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @RequestBody String body) {
        try {
            int userId = extractUserId(authHeader);
            JsonObject bodyObj = JsonParser.parseString(body).getAsJsonObject();
            int movieId = bodyObj.get("movieId").getAsInt();
            boolean ratingValue = bodyObj.get("rating").getAsBoolean();

            boolean movieExists = movieService.movieExists(movieId);
            if (!movieExists) {
                JsonObject movieObj = tmdb.getMovieDetails(movieId);
                if (movieObj == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(gson.toJson(Map.of("error", "Filme não encontrado no TMDB")));
                } else {
                    movieService.storeMovie(movieObj);
                    movieGenreService.storeMovieGenres(movieObj);
                }
            }

            int result = feedbackService.storeOrUpdateRating(userId, movieId, ratingValue);

            if (result == 1 || result == 2) {
                flixAi.train(userId, movieId, ratingValue);
            }

            String operation;
            String message;
            Boolean currentRating = null;

            switch (result) {
                case 1 -> { operation = "CREATE"; message = "Rating criado"; currentRating = ratingValue; }
                case 2 -> { operation = "UPDATE"; message = "Rating atualizado"; currentRating = ratingValue; }
                case 3 -> { operation = "IGNORED"; message = "Rating ignorado"; }
                default -> {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(gson.toJson(Map.of("error", "Erro ao processar rating")));
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("operation", operation);
            response.put("currentRating", currentRating);
            response.put("message", message);

            return ResponseEntity.ok(gson.toJson(response));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro no servidor: " + e.getMessage())));
        }
    }
}
