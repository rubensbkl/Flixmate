package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

public class TMDBService {
    private String API_KEY;
    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/";

    public TMDBService(String apiKey) {
        this.API_KEY = apiKey;
    }

    public static JsonObject getMovieDetails(int movieId) {
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

    public static List<String> getRandomMovies() {
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

    public static JsonArray getPopularMovies(int page) {
        try {
            String urlStr = "https://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY + "&language=pt-BR&page=" + page;
            URL url = new URI(urlStr).toURL(); // ✅ substituindo new URL(String)
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                System.err.println("Erro ao buscar filmes populares: " + conn.getResponseCode());
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

            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            return jsonResponse.getAsJsonArray("results");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}