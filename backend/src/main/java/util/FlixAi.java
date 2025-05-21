package util;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FlixAi {

    private static final String AI_URL = "http://ai:5005"; // ou o endereço do container

    public static String callAzureML(String jsonInput) throws IOException {
        String endpoint = "http://a314453b-f767-4359-b82f-fc2b7b1bbced.brazilsouth.azurecontainer.io/score";
        String apiKey = "6Bwh23f27x3k3JpQChCAWtSWSwbwbD2B"; // substitua com sua chave válida

        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);

        // Envia o corpo da requisição (agora usando jsonInput!)
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Verifica status da resposta
        int status = connection.getResponseCode();
        InputStreamReader streamReader = status < 400
                ? new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                : new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8);

        try (BufferedReader br = new BufferedReader(streamReader)) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }

            if (status >= 400) {
                throw new IOException("Erro do endpoint (HTTP " + status + "): " + response);
            }

            System.out.println("Resposta do endpoint: " + response);
            return response.toString();
        }
    }

    public void train(int userId, int movieId, boolean rating) {
        try {
            JsonObject ratingObj = new JsonObject();
            ratingObj.addProperty("user", String.valueOf(userId));
            ratingObj.addProperty("movie", String.valueOf(movieId));
            ratingObj.addProperty("rating", rating ? 1 : 0);

            JsonArray ratingsArray = new JsonArray();
            ratingsArray.add(ratingObj);

            JsonObject payload = new JsonObject();
            payload.add("ratings", ratingsArray);

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

    public JsonObject surprise(int userId, List<Integer> candidateIds) throws Exception {
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
            .uri(URI.create(AI_URL + "/surprise"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Erro na resposta IA surprise: " + response.body());
            return null;
        }

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }
}
