package service;

import java.util.Arrays;
import java.util.List;

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

import model.Interaction;
// import com.google.gson.JsonObject;

public class AIService {
    private String endpoint;
    private String apiKey;
    private String deploymentName;
    private TMDBService tmdb;

    public AIService(String azureOpenAIEndpoint, String azureOpenAIKey, String azureOpenAIDeploymentName, TMDBService tmdb) {
        this.endpoint = azureOpenAIEndpoint;
        this.apiKey = azureOpenAIKey;
        this.deploymentName = azureOpenAIDeploymentName;
        this.tmdb = tmdb;
    }

    public String gerarRecomendacao(List<Interaction> interacoes) {
        // Buscar filmes aleatórios

        List<String> filmesAleatorios = tmdb.getRandomMovies();

        // Construir o prompt
        StringBuilder prompt = new StringBuilder("Usuário interagiu com os seguintes filmes: ");
        for (Interaction interacao : interacoes) {
            prompt.append("Filme ID: ").append(interacao.getMovieId());
            prompt.append(interacao.getInteraction() ? " (Gostou)" : " (Não gostou)").append(", ");
        }
        prompt.append("Aqui estão alguns filmes aleatórios para a recomendação: ");
        prompt.append(String.join(", ", filmesAleatorios));

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
