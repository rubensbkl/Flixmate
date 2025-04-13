package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

public class TMDBService {
    private String API_KEY;
    private final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/";

    public TMDBService(String apiKey) {
        this.API_KEY = apiKey;
    }

    public JsonObject getMovieDetails(int movieId) {
        try {
            String urlStr = BASE_URL + movieId + "?api_key=" + API_KEY + "&language=pt-BR";
            URL url = new URI(urlStr).toURL(); // ✅ substituindo new URL(String)
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                System.err.println("Erro ao buscar filme: " + conn.getResponseCode());
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            conn.disconnect();

            return JsonParser.parseString(response.toString()).getAsJsonObject();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getRandomMovies() {
        List<String> movieTitles = new ArrayList<>();
        try {
            String urlStr = "https://api.themoviedb.org/3/discover/movie?api_key=" + API_KEY + "&sort_by=popularity.desc&page=1&language=pt-BR";
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
        String url = String.format("https://api.themoviedb.org/3/trending/movie/week?api_key=%s&page=%d", API_KEY, page);
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