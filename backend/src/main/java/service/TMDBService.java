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

public class TMDBService {
    private String API_KEY;
    private final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/";

    public TMDBService(String apiKey) {
        this.API_KEY = apiKey;
    }

    /**
     * Versão sobrecarregada que usa popularidade mínima padrão de 1.5
     */
    public JsonObject getRandomSimilarMovie(int movieId) throws IOException, InterruptedException {
        return getRandomSimilarMovie(movieId, 1.5);
    }

    /**
     * Busca filmes similares a um filme específico
     * 
     * @param movieId ID do filme
     * @return JsonArray com filmes similares ou null em caso de erro
     */
    private JsonArray fetchSimilarMovies(int movieId) throws IOException, InterruptedException {
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

        if (!jsonResponse.has("results") || !jsonResponse.get("results").isJsonArray()) {
            System.err.println("Resposta da API não contém resultados válidos para filmes similares ao ID " + movieId);
            return null;
        }

        return jsonResponse.getAsJsonArray("results");
    }

    /**
     * Converte um JsonArray para uma lista de JsonObjects
     */
    private List<JsonObject> convertJsonArrayToList(JsonArray jsonArray) {
        List<JsonObject> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            if (jsonArray.get(i).isJsonObject()) {
                JsonObject movie = jsonArray.get(i).getAsJsonObject();
                if (isValidMovie(movie)) {
                    list.add(movie);
                }
            }
        }
        return list;
    }

    /**
     * Retorna um filme similar aleatório com popularidade mínima
     * 
     * @param movieId       ID do filme para buscar similares
     * @param minPopularity Popularidade mínima (padrão 50.0)
     * @return JsonObject do filme similar ou null se não encontrar
     */
    public JsonObject getRandomSimilarMovie(int movieId, double minPopularity)
            throws IOException, InterruptedException {
        try {
            // Buscar filmes similares
            JsonArray similarMovies = fetchSimilarMovies(movieId);

            if (similarMovies == null || similarMovies.size() == 0) {
                System.out.println("Nenhum filme similar encontrado para o ID " + movieId);
                return null;
            }

            // Filtrar por popularidade
            List<JsonObject> popularSimilarMovies = filterMoviesByPopularity(similarMovies, minPopularity);

            // Se encontrar filmes populares, selecionar um aleatoriamente
            if (!popularSimilarMovies.isEmpty()) {
                JsonObject selectedMovie = selectRandomMovie(popularSimilarMovies);
                logSelectedMovie(selectedMovie, "similar com popularidade > " + minPopularity);
                return selectedMovie;
            }

            // Se não encontrar filmes com a popularidade mínima, selecionar qualquer filme
            // similar
            System.out.println("Nenhum filme similar com popularidade > " + minPopularity +
                    " encontrado. Selecionando qualquer filme similar.");
            JsonObject anyMovie = selectRandomMovie(convertJsonArrayToList(similarMovies));
            logSelectedMovie(anyMovie, "similar (sem filtro de popularidade)");
            return anyMovie;

        } catch (Exception e) {
            System.err.println("Erro ao buscar filme similar para ID " + movieId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public JsonObject getMovieDetails(int movieId) {
        try {
            String urlStr = String.format("%s%d?api_key=%s&language=pt-BR", BASE_URL, movieId, API_KEY);
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

    public JsonArray getDiscoverMovies(int page) throws Exception {
        String url = "https://api.themoviedb.org/3/discover/movie?page=" + page + "&language=pt-BR&api_key=" + API_KEY;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erro ao buscar filmes do Discover: " + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.getAsJsonArray("results");
    }

    public List<JsonObject> getMoviesDetails(List<Integer> movieIds) {
        List<JsonObject> movies = new ArrayList<>();

        for (Integer id : movieIds) {
            try {
                String url = "https://api.themoviedb.org/3/movie/" + id + "?language=pt-BR&api_key=" + API_KEY;

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

    private String getJsonStringOrDefault(JsonObject json, String key, String defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : defaultValue;
    }

    private ArrayList<Integer> extractGenreIds(JsonObject movieJson, int movieId) {
        ArrayList<Integer> genreIds = new ArrayList<>();

        if (!movieJson.has("genres") || !movieJson.get("genres").isJsonArray()) {
            throw new IllegalArgumentException(
                    "Invalid genre format: 'genres' is missing or not an array for movie ID " + movieId);
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

    /**
     * Verifica se um filme é válido (possui campos obrigatórios)
     * 
     * @param movieJson JsonObject do filme
     * @return true se o filme é válido
     */
    private boolean isValidMovie(JsonObject movieJson) {
        return movieJson.has("id") && movieJson.has("title") && movieJson.has("popularity");
    }

    /**
     * Verifica se um filme tem popularidade maior que o limiar
     * 
     * @param movieJson JsonObject do filme
     * @param threshold Limiar de popularidade
     * @return true se a popularidade for maior que o limiar
     */
    private boolean hasHighPopularity(JsonObject movieJson, double threshold) {
        return movieJson.get("popularity").getAsDouble() > threshold;
    }

    /**
     * Seleciona um filme aleatório de uma lista
     * 
     * @param movies Lista de filmes
     * @return JsonObject do filme selecionado
     */
    private JsonObject selectRandomMovie(List<JsonObject> movies) {
        Random random = new Random();
        int randomIndex = random.nextInt(movies.size());
        return movies.get(randomIndex);
    }

    /**
     * Registra informações sobre o filme selecionado
     * 
     * @param movie         JsonObject do filme
     * @param selectionType Tipo de seleção (ex: "alta popularidade", "fallback")
     */
    private void logSelectedMovie(JsonObject movie, String selectionType) {
        System.out.println("Filme selecionado (" + selectionType + "): " +
                movie.get("title").getAsString() +
                ", Popularidade: " + movie.get("popularity").getAsDouble());
    }

    /**
     * Busca um filme popular como fallback quando não encontra filmes com alta
     * popularidade
     * 
     * @return JsonObject do filme ou null em caso de erro
     */
    private JsonObject getFallbackPopularMovie() throws IOException, InterruptedException {
        System.out.println("Buscando um filme popular sem restrição de popularidade.");

        // Buscar na primeira página de filmes populares (maior chance de encontrar
        // populares)
        String url = String.format("https://api.themoviedb.org/3/movie/popular?api_key=%s&language=pt-BR&page=1",
                API_KEY);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

        if (!jsonResponse.has("results") || !jsonResponse.get("results").isJsonArray()) {
            System.err.println("Resposta da API não contém resultados válidos para fallback");
            return null;
        }

        JsonArray results = jsonResponse.getAsJsonArray("results");

        if (results.size() > 0) {
            // Pegar o filme mais popular (geralmente o primeiro da lista)
            JsonObject movieJson = results.get(0).getAsJsonObject();
            logSelectedMovie(movieJson, "fallback");
            return movieJson;
        }

        System.err.println("Não foi possível encontrar nenhum filme popular.");
        return null;
    }

    /**
     * Filtra uma lista de filmes com base em um limiar de popularidade
     * 
     * @param movies              JsonArray de filmes a serem filtrados
     * @param popularityThreshold Limiar mínimo de popularidade
     * @return Lista de filmes que atendem ao critério
     */
    private List<JsonObject> filterMoviesByPopularity(JsonArray movies, double popularityThreshold) {
        List<JsonObject> filteredMovies = new ArrayList<>();

        for (int i = 0; i < movies.size(); i++) {
            JsonObject movieJson = movies.get(i).getAsJsonObject();

            if (isValidMovie(movieJson) && hasHighPopularity(movieJson, popularityThreshold)) {
                filteredMovies.add(movieJson);
            }
        }

        return filteredMovies;
    }

    /**
     * Busca filmes populares em uma página aleatória
     * 
     * @param maxPage Número máximo de páginas para escolha aleatória
     * @return JsonArray com os resultados ou null em caso de erro
     */
    private JsonArray fetchMoviesFromRandomPage(int maxPage) throws IOException, InterruptedException {
        // Gerar um número de página aleatório
        Random random = new Random();
        int randomPage = random.nextInt(maxPage) + 1;

        // Construir a URL para buscar filmes populares na página aleatória
        String url = String.format("https://api.themoviedb.org/3/movie/popular?api_key=%s&language=pt-BR&page=%d",
                API_KEY, randomPage);

        // Configurar e enviar a requisição HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Processar a resposta JSON
        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

        // Verificar se a resposta contém resultados
        if (!jsonResponse.has("results") || !jsonResponse.get("results").isJsonArray()) {
            System.err.println("Resposta da API não contém resultados válidos para página " + randomPage);
            return null;
        }

        return jsonResponse.getAsJsonArray("results");
    }

    /**
     * Tenta encontrar um filme com popularidade maior que o limiar especificado
     * 
     * @param popularityThreshold Limiar mínimo de popularidade
     * @param maxAttempts         Número máximo de tentativas
     * @return JsonObject do filme ou null se não encontrar
     */
    private JsonObject findHighPopularityMovie(double popularityThreshold, int maxAttempts)
            throws IOException, InterruptedException {
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Buscar filmes populares em uma página aleatória
            JsonArray moviesFromRandomPage = fetchMoviesFromRandomPage(500);

            if (moviesFromRandomPage == null || moviesFromRandomPage.size() == 0) {
                continue; // Tentar novamente
            }

            // Filtrar filmes com alta popularidade
            List<JsonObject> highPopularityMovies = filterMoviesByPopularity(moviesFromRandomPage, popularityThreshold);

            // Se encontramos filmes com alta popularidade, escolher um aleatoriamente
            if (!highPopularityMovies.isEmpty()) {
                JsonObject selectedMovie = selectRandomMovie(highPopularityMovies);
                logSelectedMovie(selectedMovie, "alta popularidade");
                return selectedMovie;
            } else {
                System.out.println("Nenhum filme com popularidade > " + popularityThreshold +
                        " encontrado. Tentativa " + (attempt + 1) + " de " + maxAttempts);
            }
        }

        return null;
    }

    /**
     * Retorna um filme aleatório da API do TMDB com popularidade maior que 200
     * 
     * @return JsonObject com os detalhes do filme aleatório ou null em caso de erro
     */
    public JsonObject getARandomMovie() {
        try {
            // Tenta encontrar filme com alta popularidade
            JsonObject highPopularityMovie = findHighPopularityMovie(5, 5);

            // Se encontrou um filme com alta popularidade, retorna-o
            if (highPopularityMovie != null) {
                return highPopularityMovie;
            }

            // Caso não tenha encontrado, busca um filme popular como fallback
            return getFallbackPopularMovie();
        } catch (Exception e) {
            System.err.println("Erro ao buscar filme aleatório: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    // get Random Page
    public int getRandomPage() {
        Random random = new Random();
        return random.nextInt(550) + 1; // Gera um número aleatório entre 1 e 20
    }

    /**
     * Retorna uma lista de títulos de filmes aleatórios
     * 
     * @return Lista de títulos de filmes
     */
    public JsonArray getManyRandomMovies(int count) {
        JsonArray movies = new JsonArray();
        try {
            // Get random page
            int randomPage = getRandomPage();
            // Constrói a URL para buscar filmes aleatórios
            String urlStr = "https://api.themoviedb.org/3/discover/movie?api_key=" + API_KEY
                    + "&sort_by=popularity.desc&language=pt-BR&page=" + randomPage;
            URL url = new URI(urlStr).toURL(); // ✅ substituindo new URL(String)
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                System.err.println("Erro ao buscar filmes aleatórios: " + conn.getResponseCode());
                return movies;
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

            for (int i = 0; i < count && i < results.size(); i++) {
                JsonObject movie = results.get(i).getAsJsonObject();
                movies.add(movie);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return movies;
    }

    public List<String> getRandomMovies() {
        List<String> movieTitles = new ArrayList<>();
        try {
            String urlStr = "https://api.themoviedb.org/3/discover/movie?api_key=" + API_KEY
                    + "&page=1&language=pt-BR";
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