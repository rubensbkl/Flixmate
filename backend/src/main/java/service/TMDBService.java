package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.cdimascio.dotenv.Dotenv;


public class TMDBService {
    private static final Dotenv dotenv = Dotenv.load(); // Carrega do .env
    private static final String API_KEY = dotenv.get("TMDB_API_KEY");
    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/";

    public static JsonObject getMovieDetails(int movieId) {
        try {
            String urlStr = BASE_URL + movieId + "?api_key=" + API_KEY + "&language=pt-BR";
            URL url = new URL(urlStr); // Solve JAVA 11+ issue compliance
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
}
