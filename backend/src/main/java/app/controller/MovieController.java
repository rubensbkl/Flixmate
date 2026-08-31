package app.controller;

import model.Genre;
import model.Movie;
import model.Feedback;
import model.WatchLater;
import model.Favorite;
import service.MovieGenreService;
import service.MovieService;
import service.FeedbackService;
import service.WatchLaterService;
import service.FavoriteService;
import service.RecommendationService;
import util.JWTUtil;
import com.google.gson.Gson;

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
public class MovieController {

    @org.springframework.beans.factory.annotation.Autowired
    private MovieService movieService;
    
    @org.springframework.beans.factory.annotation.Autowired
    private MovieGenreService movieGenreService;
    
    @org.springframework.beans.factory.annotation.Autowired
    private FeedbackService feedbackService;
    
    @org.springframework.beans.factory.annotation.Autowired
    private WatchLaterService watchLaterService;

    @org.springframework.beans.factory.annotation.Autowired
    private FavoriteService favoriteService;

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

    @GetMapping("/movies/search")
    public ResponseEntity<String> searchMovies(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                               @RequestParam(defaultValue = "") String query,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "25") int limit,
                                               @RequestParam(defaultValue = "popularity") String sortBy,
                                               @RequestParam(required = false) String genres,
                                               @RequestParam(required = false) String yearFrom,
                                               @RequestParam(required = false) String yearTo) {
        try {
            extractUserId(authHeader);

            query = query.trim();
            if (page < 1) page = 1;
            if (limit < 1 || limit > 100) limit = 25;

            ArrayList<Movie> movies = movieService.searchWithFilters(query, page, limit, sortBy, genres, yearFrom, yearTo);
            int totalResults = movieService.countSearchResultsWithFilters(query, sortBy, genres, yearFrom, yearTo);
            int totalPages = (int) Math.ceil((double) totalResults / limit);

            List<Map<String, Object>> results = new ArrayList<>();
            for (Movie movie : movies) {
                List<Genre> movieGenres = movieGenreService.buscarGenerosDoFilme(movie.getId());
                List<String> genreNames = movieGenres.stream().map(Genre::getName).collect(Collectors.toList());

                results.add(Map.of(
                        "id", movie.getId(),
                        "title", movie.getTitle(),
                        "poster_path", movie.getPosterPath(),
                        "release_date", movie.getReleaseDate(),
                        "genres", genreNames));
            }

            Map<String, Object> response = Map.of(
                    "status", "ok",
                    "page", page,
                    "total_pages", totalPages,
                    "total_results", totalResults,
                    "results", results);

            return ResponseEntity.ok(gson.toJson(response));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao buscar filmes: " + e.getMessage())));
        }
    }

    @GetMapping("/movie/{movieId}/details")
    public ResponseEntity<String> getMovieDetails(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                  @PathVariable("movieId") int movieId) {
        try {
            int userId = extractUserId(authHeader);

            Map<String, Object> response = new HashMap<>();

            Movie movie = movieService.getMovieById(movieId);
            if (movie == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Map.of("error", "Filme não encontrado")));
            }

            Feedback fback = feedbackService.getFeedback(userId, movieId);
            Boolean rating = fback != null ? fback.getFeedback() : null;

            if (rating != null)
                response.put("rating", rating ? 1 : 0);
            else
                response.put("rating", null);

            List<Genre> genres = movieGenreService.buscarGenerosDoFilme(movieId);

            List<String> genreNames = genres.stream()
                    .map(Genre::getName)
                    .collect(Collectors.toList());

            Map<String, Object> movieData = new HashMap<>();
            movieData.put("id", movie.getId());
            movieData.put("title", movie.getTitle());
            movieData.put("overview", movie.getOverview());
            movieData.put("rating", movie.getRating());
            movieData.put("releaseDate", movie.getReleaseDate());
            movieData.put("originalLanguage", movie.getOriginalLanguage());
            movieData.put("popularity", movie.getPopularity());
            movieData.put("posterPath", movie.getPosterPath());
            movieData.put("backdropPath", movie.getBackdropPath());
            movieData.put("genres", genreNames);

            response.put("movieData", movieData);

            return ResponseEntity.ok(gson.toJson(response));

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(gson.toJson(Map.of("error", "ID de filme inválido")));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "Erro ao buscar dados do filme: " + e.getMessage())));
        }
    }

    @GetMapping("/movie/{movieId}/watchlist")
    public ResponseEntity<String> isInWatchlist(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                @PathVariable("movieId") int movieId) {
        try {
            int userId = extractUserId(authHeader);
            WatchLater watchLater = new WatchLater(userId, movieId);
            boolean isInWatchlist = watchLaterService.isInWatchLater(watchLater);
            return ResponseEntity.ok(gson.toJson(Map.of("status", "ok", "movieId", movieId, "isInWatchlist", isInWatchlist)));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(gson.toJson(Map.of("error", "ID de filme inválido")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(gson.toJson(Map.of("error", "Erro ao verificar watchlist: " + e.getMessage())));
        }
    }

    @GetMapping("/movie/{movieId}/favorite")
    public ResponseEntity<String> isInFavorites(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                @PathVariable("movieId") int movieId) {
        try {
            int userId = extractUserId(authHeader);
            Favorite favoriteObj = new Favorite(userId, movieId);
            boolean isFavorite = favoriteService.isInFavorites(favoriteObj);
            return ResponseEntity.ok(gson.toJson(Map.of("status", "ok", "movieId", movieId, "isFavorite", isFavorite)));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(gson.toJson(Map.of("error", "ID de filme inválido")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(gson.toJson(Map.of("error", "Erro ao verificar favorito: " + e.getMessage())));
        }
    }

    @GetMapping("/movie/{movieId}/recommended")
    public ResponseEntity<String> isRecommended(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                @PathVariable("movieId") int movieId) {
        try {
            int userId = extractUserId(authHeader);
            boolean isRecommended = recommendationService.isMovieRecommended(userId, movieId);
            return ResponseEntity.ok(gson.toJson(Map.of("status", "ok", "movieId", movieId, "isRecommended", isRecommended)));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(gson.toJson(Map.of("error", "ID de filme inválido")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(gson.toJson(Map.of("error", "Erro ao verificar recomendação: " + e.getMessage())));
        }
    }
}
