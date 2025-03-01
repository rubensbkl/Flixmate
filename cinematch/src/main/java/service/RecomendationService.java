//package service;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//import dao.UserMovieDAO;
//import io.github.cdimascio.dotenv.Dotenv;
//import model.UserMovie;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class RecommendationService {
//    private final String aiApiKey;
//    private final HttpClient httpClient;
//    private final Gson gson;
//    private final UserMovieDAO userMovieDAO;
//    private final MovieService movieService;
//
//    public RecommendationService() {
//        Dotenv dotenv = Dotenv.load();
//        this.aiApiKey = dotenv.get("AI_API_KEY");
//        this.httpClient = HttpClient.newHttpClient();
//        this.gson = new Gson();
//        this.userMovieDAO = new UserMovieDAO();
//        this.movieService = new MovieService();
//    }
//
//    public List<Map<String, Object>> getRecommendationsForUser(String userEmail) {
//        // 1. Recuperar o histórico de filmes curtidos pelo usuário
//        List<UserMovie> userMovies = userMovieDAO.getUserMoviesByEmail(userEmail);
//
//        if (userMovies.isEmpty()) {
//            return new ArrayList<>(); // Sem histórico, sem recomendações
//        }
//
//        // 2. Preparar dados para enviar à API de IA
//        List<Map<String, Object>> likedMovies = new ArrayList<>();
//
//        for (UserMovie userMovie : userMovies) {
//            Map<String, Object> movieDetails = movieService.getMovieDetails(userMovie.getMovieId());
//            likedMovies.add(movieDetails);
//        }
//
//        // 3. Chamar a API de IA para obter recomendações
//        try {
//            // Construir o payload para a API
//            Map<String, Object> payload = new HashMap<>();
//            payload.put("userProfile", likedMovies);
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("https://sua-api-de-ia.com/recommendations"))
//                    .header("Content-Type", "application/json")
//                    .header("Authorization", "Bearer " + aiApiKey)
//                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//            if (response.statusCode() == 200) {
//                // Processar as recomendações da API
//                List<Map<String, Object>> recommendations = new ArrayList<>();
//
//                // Aqui você processaria a resposta da API de IA
//                // Este é apenas um exemplo genérico
//
//                return recommendations;
//            } else {
//                System.err.println("Error getting recommendations: " + response.statusCode());
//                return new ArrayList<>();
//            }
//        } catch (IOException | InterruptedException e) {
//            System.err.println("Error calling AI API: " + e.getMessage());
//            return new ArrayList<>();
//        }
//    }
//}