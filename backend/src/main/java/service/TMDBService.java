package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import model.Movie;

public class TMDBService {
    private String API_KEY;
    private final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/";

    public TMDBService(String apiKey) {
        this.API_KEY = apiKey;
    }

    public Movie getRandomSimilarMovie(int movieId) throws IOException, InterruptedException {
        try {
            // Constrói a URL para buscar filmes similares
            String url = String.format("https://api.themoviedb.org/3/movie/%d/similar?api_key=%s&language=pt-BR&page=1",
                    movieId, API_KEY);
            
            // Configura e envia a requisição HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
            // Processa a resposta JSON
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray results = jsonResponse.getAsJsonArray("results");
    
            // Verifica se há resultados
            if (results.size() == 0) {
                return null; // Sem filmes similares
            }
    
            // Escolhe um filme aleatório da lista de resultados
            Random random = new Random();
            int randomIndex = random.nextInt(results.size());
            JsonObject movieJson = results.get(randomIndex).getAsJsonObject();
    
            // Extrai os detalhes básicos do filme
            int id = movieJson.get("id").getAsInt();
            String title = movieJson.get("title").getAsString();
            String releaseDate = getJsonStringOrDefault(movieJson, "release_date", "");
            String originalLanguage = movieJson.get("original_language").getAsString();
            double popularity = movieJson.get("popularity").getAsDouble();
            boolean adult = movieJson.get("adult").getAsBoolean();
            
            // Extrai os IDs de gênero usando ArrayList para compatibilidade com o método existente
            ArrayList<Integer> genreIds = new ArrayList<>();
            if (movieJson.has("genre_ids") && movieJson.get("genre_ids").isJsonArray()) {
                JsonArray genreIdsJson = movieJson.getAsJsonArray("genre_ids");
                for (int i = 0; i < genreIdsJson.size(); i++) {
                    genreIds.add(genreIdsJson.get(i).getAsInt());
                }
            } else {
                System.out.println("Aviso: Filme sem informações de gênero: " + id);
            }
    
            // Cria e retorna o objeto Movie
            return new Movie(id, title, releaseDate, originalLanguage, popularity, adult, genreIds);
        } catch (Exception e) {
            System.err.println("Erro ao buscar filme similar para ID " + movieId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Movie getMovieDetails(int movieId) {
        try {
            // Fetch movie data from API
            String url = String.format("%s%d?api_key=%s&language=pt-BR", BASE_URL, movieId, API_KEY);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject movieJson = JsonParser.parseString(response.body()).getAsJsonObject();
    
            // Extract basic movie details
            int id = movieJson.get("id").getAsInt();
            String title = movieJson.get("title").getAsString();
            String releaseDate = getJsonStringOrDefault(movieJson, "release_date", "");
            String originalLanguage = movieJson.get("original_language").getAsString();
            double popularity = movieJson.get("popularity").getAsDouble();
            boolean adult = movieJson.get("adult").getAsBoolean();
            
            // Extract genre IDs
            ArrayList<Integer> genreIds = extractGenreIds(movieJson, movieId);
            
            // Create and return movie object
            return new Movie(id, title, releaseDate, originalLanguage, popularity, adult, genreIds);
        } catch (Exception e) {
            System.err.println("Error fetching movie details for ID " + movieId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private String getJsonStringOrDefault(JsonObject json, String key, String defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : defaultValue;
    }
    
    private ArrayList<Integer> extractGenreIds(JsonObject movieJson, int movieId) {
        ArrayList<Integer> genreIds = new ArrayList<>();
        
        if (!movieJson.has("genres") || !movieJson.get("genres").isJsonArray()) {
            throw new IllegalArgumentException("Invalid genre format: 'genres' is missing or not an array for movie ID " + movieId);
        }
        
        JsonArray genres = movieJson.getAsJsonArray("genres");
        for (int i = 0; i < genres.size(); i++) {
            JsonObject genre = genres.get(i).getAsJsonObject();
            if (!genre.has("id")) {
                throw new IllegalArgumentException("Invalid genre format: missing 'id' field for movie ID " + movieId);
            }
            genreIds.add(genre.get("id").getAsInt());
        }
        
        return genreIds;
    }

    public List<String> getRandomMovies() {
        List<String> movieTitles = new ArrayList<>();
        try {
            String urlStr = "https://api.themoviedb.org/3/discover/movie?api_key=" + API_KEY
                    + "&sort_by=popularity.desc&page=1&language=pt-BR";
            URL url = new URI(urlStr).toURL(); // ✅ substituindo new URL(String)
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                System.err.println("Erro ao buscar filmes aleatórios: " + conn.getResponseCode());
                return movieTitles;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            conn.disconnect();

            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray results = jsonResponse.getAsJsonArray("results");

            for (int i = 0; i < results.size(); i++) {
                JsonObject movie = results.get(i).getAsJsonObject();
                movieTitles.add(movie.get("title").getAsString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return movieTitles;
    }

    public JsonArray getPopularMovies(int page) throws IOException, InterruptedException {
        String url = String.format("https://api.themoviedb.org/3/movie/popular?api_key=%s&page=%d", API_KEY, page);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        return jsonResponse.getAsJsonArray("results");
    }

    public JsonArray getTrendingMovies(int page) throws IOException, InterruptedException {
        String url = String.format("https://api.themoviedb.org/3/trending/movie/week?api_key=%s&page=%d", API_KEY,
                page);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        return jsonResponse.getAsJsonArray("results");
    }

    public JsonArray getTopRatedMovies(int page) throws IOException, InterruptedException {
        String url = String.format("https://api.themoviedb.org/3/movie/top_rated?api_key=%s&page=%d", API_KEY, page);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        return jsonResponse.getAsJsonArray("results");
    }

    public JsonArray getUpcomingMovies(int page) throws IOException, InterruptedException {
        String url = String.format("https://api.themoviedb.org/3/movie/upcoming?api_key=%s&page=%d", API_KEY, page);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        return jsonResponse.getAsJsonArray("results");
    }

}