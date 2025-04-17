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

import model.Feedback;
import model.Genre;
// import com.google.gson.JsonObject;
import util.RecommendationHelper;

public class AIService {
    private String endpoint;
    private String apiKey;
    private String deploymentName;

    public AIService(String azureOpenAIEndpoint, String azureOpenAIKey, String azureOpenAIDeploymentName,
            TMDBService tmdb) {
        this.endpoint = azureOpenAIEndpoint;
        this.apiKey = azureOpenAIKey;
        this.deploymentName = azureOpenAIDeploymentName;
    }

    public int gerarRecomendacao(List<Feedback> interacoes, List<Genre> generosFavoritos,
            RecommendationHelper helper) {

        // Construir o prompt system
        String promptSystem = "Você é um sistema de recomendação de filmes. Seu trabalho é escolher o filme mais adequado para o usuário com base nas informações fornecidas. "
                +
                "Eu vou te enviar uma lista de filmes que o usuário já avaliou, e uma lista de filmes candidatos para recomendação. "
                +
                "Você deve analisar essas informações e escolher o filme com a maior afinidade com o usuário, considerando os gêneros favoritos dele e seu histórico de avaliações. "
                +
                "No final, retorne apenas o 'id' do filme escolhido, como um número inteiro, e nada mais.";

        // Obter o prompt do usuário da classe RecommendationHelper
        String promptUser = helper.createAIPrompt(interacoes, generosFavoritos);

        System.out.println("==================== PROMPT ENVIADO À IA ====================");
        System.out.println("SYSTEM PROMPT: " + promptSystem);
        System.out.println("\nUSER PROMPT: " + promptUser);
        System.out.println("==============================================================");

        // Inicializa o cliente da Azure OpenAI
        OpenAIClient client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(endpoint)
                .buildClient();

        // Monta as mensagens
        List<ChatRequestMessage> messages = Arrays.asList(
                new ChatRequestSystemMessage(promptSystem),
                new ChatRequestUserMessage(promptUser));

        // Define as opções da completude
        ChatCompletionsOptions options = new ChatCompletionsOptions(messages);
        options.setMaxTokens(6); // pequena resposta, só um id inteiro
        options.setTemperature(0.0); // determinístico, sem criatividade

        // Faz a requisição e obtém a resposta
        ChatCompletions response = client.getChatCompletions(deploymentName, options);
        ChatResponseMessage message = response.getChoices().get(0).getMessage();

        System.out.println("Resposta da IA: " + message.getContent());

        // Converte para inteiro e retorna
        try {
            return Integer.parseInt(message.getContent().trim());
        } catch (NumberFormatException e) {
            System.err.println("Erro ao processar resposta da IA: " + message.getContent());
            throw new RuntimeException("A IA não retornou um ID de filme válido", e);
        }
    }

    // Método auxiliar para formatar os IDs de gêneros
    private String formatGenreIds(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return "Nenhum gênero";
        }
        return genreIds.toString();
    }

}
