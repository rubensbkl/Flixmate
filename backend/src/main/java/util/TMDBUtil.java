package util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TMDBUtil {
    private String API_KEY;
    private final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/";

    /**
     * Construtor da classe TMDBUtil
     * 
     * @param apiKey Chave de API do The Movie Database (TMDB)
     */
    public TMDBUtil(String apiKey) {
        this.API_KEY = apiKey;
    }

    /**
     * Busca detalhes de um filme específico pelo ID
     * 
     * @param movieId ID do filme a ser buscado
     * @return JsonObject com os detalhes do filme ou null em caso de erro
     */
    public JsonObject getMovieDetails(int movieId) {
        try {
            String urlStr = String.format("%s%d?api_key=%s", BASE_URL, movieId, API_KEY);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlStr))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Apenas retorna o JsonObject bruto da API
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (Exception e) {
            System.err.println("Error fetching movie details for ID " + movieId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca um filme aleatório com alta popularidade
     * 
     * @param page                Página de resultados a ser buscada
     * @param popularityThreshold Limiar de popularidade para considerar um filme
     *                            "popular"
     * @return JsonObject do filme selecionado ou null se nenhum filme for
     *         encontrado
     */
    public List<JsonObject> getMoviesDetails(List<Integer> movieIds) {
        List<JsonObject> movies = new ArrayList<>();

        for (Integer id : movieIds) {
            try {
                String url = "https://api.themoviedb.org/3/movie/" + id + "?&api_key=" + API_KEY;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonObject movie = JsonParser.parseString(response.body()).getAsJsonObject();
                    movies.add(movie);
                } else {
                    System.err.println("Erro ao buscar detalhes do filme ID " + id);
                }

            } catch (Exception e) {
                System.err.println("Erro ao buscar detalhes do filme ID " + id + ": " + e.getMessage());
            }
        }

        return movies;
    }

    /**
     * Busca um filme aleatório com alta popularidade
     * 
     * @param page                Página de resultados a ser buscada
     * @param popularityThreshold Limiar de popularidade para considerar um filme
     *                            "popular"
     * @return JsonObject do filme selecionado ou null se nenhum filme for
     *         encontrado
     */
    public JsonArray getTopRatedMovies(int page) throws IOException, InterruptedException {
        String url = String.format("https://api.themoviedb.org/3/movie/top_rated?api_key=%s&page=%d", API_KEY, page);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erro ao buscar filmes mais bem avaliados: " + response.body());
        }

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        return jsonResponse.getAsJsonArray("results");
    }

}