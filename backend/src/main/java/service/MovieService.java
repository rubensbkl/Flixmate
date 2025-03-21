package service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieService {
    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson;

    public MovieService() {
        Dotenv dotenv = Dotenv.load();
        this.apiKey = dotenv.get("TMDB_API_KEY");
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public List<Map<String, Object>> getTrendingMovies() {
        String url = "https://api.themoviedb.org/3/trending/movie/week?api_key=" + apiKey;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                JsonArray results = jsonResponse.getAsJsonArray("results");

                List<Map<String, Object>> movies = new ArrayList<>();
                for (JsonElement result : results) {
                    JsonObject movieJson = result.getAsJsonObject();

                    Map<String, Object> movie = new HashMap<>();
                    movie.put("id", movieJson.get("id").getAsInt());
                    movie.put("title", movieJson.get("title").getAsString());
                    movie.put("poster", "https://image.tmdb.org/t/p/w500" + movieJson.get("poster_path").getAsString());
                    movie.put("rating", movieJson.get("vote_average").getAsFloat());

                    movies.add(movie);
                }

                return movies;
            } else {
                System.err.println("Error fetching trending movies: " + response.statusCode());
                return new ArrayList<>();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error fetching trending movies: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getMovieDetails(int movieId) {
        String url = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject movieJson = gson.fromJson(response.body(), JsonObject.class);

                Map<String, Object> movie = new HashMap<>();
                movie.put("id", movieJson.get("id").getAsInt());
                movie.put("title", movieJson.get("title").getAsString());
                movie.put("poster", "https://image.tmdb.org/t/p/w500" + movieJson.get("poster_path").getAsString());
                movie.put("rating", movieJson.get("vote_average").getAsFloat());
                movie.put("overview", movieJson.get("overview").getAsString());
                movie.put("releaseDate", movieJson.get("release_date").getAsString());
                movie.put("runtime", movieJson.get("runtime").getAsInt());

                return movie;
            } else {
                System.err.println("Error fetching movie details: " + response.statusCode());
                return new HashMap<>();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error fetching movie details: " + e.getMessage());
            return new HashMap<>();
        }
    }
}