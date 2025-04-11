package service;

import java.util.List;

import com.azure.ai.inference.ChatCompletionsClient;
import com.azure.ai.inference.ChatCompletionsClientBuilder;
import com.azure.ai.inference.models.ChatCompletions;
import com.azure.ai.inference.models.ChatCompletionsOptions;
import com.azure.ai.inference.models.ChatRequestMessage;
import com.azure.ai.inference.models.ChatRequestSystemMessage;
import com.azure.ai.inference.models.ChatRequestUserMessage;
import com.azure.core.credential.AzureKeyCredential;

import model.Interaction;
import com.google.gson.JsonObject;

import java.util.Arrays;

public class AIService {
    private static final String API_KEY = "82jqQ7M5CgecwRt4Q81YEZQU4OWg4KtfEf2DorLXAxY7WURXlwLrJQQJ99BDACHYHv6XJ3w3AAAAACOGMHy8";
    private static final String ENDPOINT = "https://15251-m9cpj4n3-eastus2.cognitiveservices.azure.com/";
    private static final String MODEL = "gpt-35-turbo";

    private static final ChatCompletionsClient client = new ChatCompletionsClientBuilder()
        .credential(new AzureKeyCredential(API_KEY))
        .endpoint(ENDPOINT)
        .buildClient();

    public static String gerarRecomendacao(List<Interaction> interacoes) {
        StringBuilder likes = new StringBuilder();
        StringBuilder dislikes = new StringBuilder();

        for (Interaction i : interacoes) {
            JsonObject movie = TMDBService.getMovieDetails(i.getMovieId());
            if (movie != null && movie.has("title")) {
                String title = movie.get("title").getAsString();
                if (i.getInteraction()) {
                    likes.append(title).append(", ");
                } else {
                    dislikes.append(title).append(", ");
                }
            }
        }

        String prompt = String.format(
            "Filmes que gostei: %s. Filmes que não gostei: %s. Me recomende um filme diferente, com base em diretores, gêneros e características similares.",
            likes.toString(), dislikes.toString()
        );

        List<ChatRequestMessage> messages = Arrays.asList(
            new ChatRequestSystemMessage("Você é um sistema de recomendação de filmes baseado no gosto do usuário."),
            new ChatRequestUserMessage(prompt)
        );

        ChatCompletionsOptions options = new ChatCompletionsOptions(messages);
        options.setMaxTokens(512);
        options.setTemperature(0.7);
        options.setTopP(1.0);
        options.setModel(MODEL);

        ChatCompletions completions = client.complete(options);
        return completions.getChoices().get(0).getMessage().getContent();
    }
}
