package app.controller;

import model.Favorite;
import model.Genre;
import model.Movie;
import model.Recommendation;
import model.WatchLater;
import service.*;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RecommendationController {

    @org.springframework.beans.factory.annotation.Autowired
    private RecommendationService recommendationService;
    @org.springframework.beans.factory.annotation.Autowired
    private MovieService movieService;
    @org.springframework.beans.factory.annotation.Autowired
    private MovieGenreService movieGenreService;
    @org.springframework.beans.factory.annotation.Autowired
    private GenreService genreService;
    @org.springframework.beans.factory.annotation.Autowired
    private WatchLaterService watchLaterService;
    @org.springframework.beans.factory.annotation.Autowired
    private FavoriteService favoriteService;
    private final FlixAi flixAi = new FlixAi();
    @org.springframework.beans.factory.annotation.Autowired
    private TMDBUtil tmdb;
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

    @PostMapping("/recommendation/delete")
    public ResponseEntity<String> deleteRecommendation(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                       @RequestBody String body) {
        try {
            int userId = extractUserId(authHeader);
            JsonObject bodyObj = JsonParser.parseString(body).getAsJsonObject();
            int movieId = bodyObj.get("movieId").getAsInt();

            boolean deleted = recommendationService.deleteRecommendation(userId, movieId);
            flixAi.train(userId, movieId, false);
            if (deleted) {
                return ResponseEntity.ok(gson.toJson(Map.of("status", "ok", "message", "Recomendação deletada com sucesso")));
            } else {
                return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "Erro ao deletar recomendação")));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao deletar recomendação")));
        }
    }

    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<String> getRecommendationsByUser(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                           @PathVariable int userId) {
        try {
            extractUserId(authHeader);
            ArrayList<Recommendation> recommendations = recommendationService.getRecommendationsByUserId(userId);

            if (recommendations.isEmpty()) {
                return ResponseEntity.ok(gson.toJson(Map.of("error", "Nenhuma recomendação encontrada")));
            }

            List<Map<String, Object>> moviesData = new ArrayList<>();
            for (Recommendation recommendation : recommendations) {
                int movieId = recommendation.getMovieId();
                Movie movie = movieService.getMovieById(movieId);

                if (movie == null) {
                    return ResponseEntity.ok(gson.toJson(Map.of("error", "Filme não encontrado para ID: " + movieId)));
                }

                List<Integer> movieGenresIds = movieGenreService.getGenreIdsForMovie(movieId);
                ArrayList<Genre> movieGenres = new ArrayList<>();
                for (Integer genreId : movieGenresIds) {
                    Genre genre = genreService.getGenreById(genreId);
                    if (genre != null) {
                        movieGenres.add(genre);
                    }
                }

                Map<String, Object> movieData = new HashMap<>();
                movieData.put("id", movie.getId());
                movieData.put("title", movie.getTitle());
                movieData.put("poster_path", movie.getPosterPath());
                movieData.put("release_date", movie.getReleaseDate());
                movieData.put("score", recommendation.getScore());
                movieData.put("genres", movieGenres.stream().map(Genre::getName).collect(Collectors.toList()));
                moviesData.add(movieData);
            }

            return ResponseEntity.ok(gson.toJson(Map.of("status", "ok", "movies", moviesData)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao buscar recomendações: " + e.getMessage())));
        }
    }

    @PostMapping("/recommendation/watched")
    public ResponseEntity<String> toggleWatchLater(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                   @RequestBody String body) {
        try {
            int userId = extractUserId(authHeader);
            JsonObject bodyObj = JsonParser.parseString(body).getAsJsonObject();
            int movieId = bodyObj.get("movieId").getAsInt();
            boolean watched = bodyObj.get("watched").getAsBoolean();
            
            WatchLater watchLater = new WatchLater(userId, movieId);
            boolean success = watchLaterService.toggleWatchLater(watchLater, watched);

            if (success) {
                boolean currentStatus = watchLaterService.isInWatchLater(watchLater);
                return ResponseEntity.ok(gson.toJson(Map.of(
                        "status", "ok",
                        "message", watched ? "Filme adicionado à watchlist" : "Filme removido da watchlist",
                        "currentStatus", currentStatus)));
            } else {
                return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "Erro ao atualizar watchlist")));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao processar watchlist: " + e.getMessage())));
        }
    }

    @PostMapping("/recommendation/favorite")
    public ResponseEntity<String> toggleFavorite(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                 @RequestBody String body) {
        try {
            int userId = extractUserId(authHeader);
            JsonObject bodyObj = JsonParser.parseString(body).getAsJsonObject();
            int movieId = bodyObj.get("movieId").getAsInt();
            boolean status = bodyObj.get("favorite").getAsBoolean();
            
            Favorite favoriteObj = new Favorite(userId, movieId);
            boolean success = favoriteService.toggleFavorite(favoriteObj, status);

            if (success) {
                boolean currentStatus = favoriteService.isInFavorites(favoriteObj);
                flixAi.train(userId, movieId, currentStatus);
                return ResponseEntity.ok(gson.toJson(Map.of(
                        "status", "ok",
                        "message", status ? "Filme adicionado aos favoritos" : "Filme removido dos favoritos",
                        "currentStatus", currentStatus)));
            } else {
                return ResponseEntity.badRequest().body(gson.toJson(Map.of("error", "Erro ao atualizar favoritos")));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao processar favoritos: " + e.getMessage())));
        }
    }

    @GetMapping("/recommendation")
    public ResponseEntity<String> getSingleRecommendation(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            int userId = extractUserId(authHeader);

            ArrayList<Integer> recommendedMovies = recommendationService.getRecommendedMoviesIds(userId);
            ArrayList<Integer> allMovies = movieService.getAllMoviesIds();

            if (allMovies.isEmpty()) {
                // Se banco tá vazio, trazemos um filme popular do TMDB
                JsonArray discoverMovies = tmdb.getPopularMovies(1);
                if (discoverMovies.size() > 0) {
                    JsonObject firstMovie = discoverMovies.get(0).getAsJsonObject();
                    return ResponseEntity.ok(firstMovie.toString());
                }
                return ResponseEntity.badRequest().body("{\"erro\": \"Não há filmes disponíveis para recomendar.\"}");
            }

            final int NUM_CANDIDATOS = 500;
            List<Integer> candidatos = allMovies.stream()
                    .filter(id -> !recommendedMovies.contains(id))
                    .collect(Collectors.toCollection(ArrayList::new));

            if (candidatos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"erro\": \"Não há filmes não avaliados para recomendar.\"}");
            }

            Collections.shuffle(candidatos, new SecureRandom());
            candidatos = candidatos.subList(0, Math.min(NUM_CANDIDATOS, candidatos.size()));

            JsonObject recomendacao = flixAi.recommend(userId, candidatos);
            JsonArray recommendedMoviesJSON = recomendacao.getAsJsonArray("recommended_movies");

            if (recommendedMoviesJSON.size() == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"erro\": \"Não há filmes não avaliados para recomendar.\"}");
            }

            JsonObject firstMovie = recommendedMoviesJSON.get(0).getAsJsonObject();
            int melhorFilmeId = firstMovie.get("id").getAsInt();
            double score = firstMovie.get("score").getAsDouble();

            recommendationService.storeRecommendation(userId, melhorFilmeId, score);

            JsonObject movie = tmdb.getMovieDetails(melhorFilmeId);
            if (movie == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"erro\": \"Filme não encontrado.\"}");
            }

            return ResponseEntity.ok(movie.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro interno: " + e.getMessage())));
        }
    }
}
