package util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import model.Feedback;

public class FlixAi {

    private static final String AI_URL = "http://ai:5005"; // ou o endereço do container

    public void train(int userId, int movieId, boolean rating) {
        JsonObject ratingObj = new JsonObject();
        ratingObj.addProperty("user", String.valueOf(userId));
        ratingObj.addProperty("movie", String.valueOf(movieId));
        ratingObj.addProperty("rating", rating ? 1 : 0);

        JsonArray ratingsArray = new JsonArray();
        ratingsArray.add(ratingObj);

        JsonObject payload = new JsonObject();
        payload.add("ratings", ratingsArray);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AI_URL + "/train"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            System.err.println("Erro ao enviar dados para IA local: " + e.getMessage());
        }
    }

    public JsonObject recommend(int userId, List<Integer> candidateIds) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("user", String.valueOf(userId));

        JsonArray candidateArray = new JsonArray();
        for (Integer id : candidateIds) {
            candidateArray.add(String.valueOf(id));
        }
        payload.add("candidate_ids", candidateArray);

        HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(AI_URL + "/recommend"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Erro na resposta IA recommend: " + response.body());
            return null;
        }

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }
}