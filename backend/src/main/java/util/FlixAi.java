package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FlixAi {
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
}
