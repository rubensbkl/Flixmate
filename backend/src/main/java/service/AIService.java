package service;

import java.util.List;
import java.util.Arrays;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
// import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
// import com.azure.ai.openai.models.ChatRequestAssistantMessage;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
// import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.core.credential.AzureKeyCredential;
// import com.azure.core.util.Configuration;

import model.Interaction;
// import com.google.gson.JsonObject;

public class AIService {

    public static  String gerarRecomendacao(List<Interaction> interacoes) {
        // Buscar filmes aleatórios
        List<String> filmesAleatorios = TMDBService.getRandomMovies();

        // Construir o prompt
        StringBuilder prompt = new StringBuilder("Usuário interagiu com os seguintes filmes: ");
        for (Interaction interacao : interacoes) {
            prompt.append("Filme ID: ").append(interacao.getMovieId());
            prompt.append(interacao.getInteraction() ? " (Gostou)" : " (Não gostou)").append(", ");
        }
        prompt.append("Aqui estão alguns filmes aleatórios para a recomendação: ");
        prompt.append(String.join(", ", filmesAleatorios));

        String endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
        String apiKey = System.getenv("AZURE_OPENAI_API_KEY");
        String deploymentName = System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME");

        // Inicializa o cliente da Azure OpenAI
        OpenAIClient client = new OpenAIClientBuilder()
            .credential(new AzureKeyCredential(apiKey))
            .endpoint(endpoint)
            .buildClient();

        // Monta as mensagens
        List<ChatRequestMessage> messages = Arrays.asList(
                new ChatRequestSystemMessage("Você é um assistente de cinema."),
                new ChatRequestUserMessage(prompt.toString())
        );

        // Define as opções da completude
        ChatCompletionsOptions options = new ChatCompletionsOptions(messages);
        options.setMaxTokens(256);
        options.setTemperature(0.7);

        // Faz a requisição e obtém a resposta
        ChatCompletions response = client.getChatCompletions(deploymentName, options);
        ChatResponseMessage message = response.getChoices().get(0).getMessage();

        System.out.println("Resposta do modelo: " + message.getContent());

        return message.getContent();
    }

}
