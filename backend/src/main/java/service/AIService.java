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
import util.RecommendationHelper;

public class AIService {
    private String endpoint;
    private String apiKey;
    private String deploymentName;
    

    public AIService(String azureOpenAIEndpoint, String azureOpenAIKey, String azureOpenAIDeploymentName) {
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

    // public int surpreendame(JsonArray filmesDeGenero, List<Genre> generosFavoritos) {

    //     if (filmesDeGenero == null || filmesDeGenero.size() == 0) {
    //         System.err.println("Lista de filmes candidatos está vazia. Não é possível chamar a IA.");
    //         // Poderia retornar um ID padrão, -1, ou lançar uma exceção específica.
    //         throw new RuntimeException("Nenhum filme candidato fornecido para a função 'surpreenda-me'.");
    //    }

    //    // Construir o prompt system específico para esta função
    //    String promptSystem = "Você é um sistema de recomendação de filmes. Sua tarefa é escolher UM filme de uma lista fornecida que melhor se alinhe aos gêneros favoritos de um usuário. "
    //            +
    //            "Eu lhe darei os gêneros favoritos do usuário e uma lista de filmes candidatos (com título, ID e gêneros). " +
    //            "Analise os gêneros de cada filme candidato e escolha aquele que parece mais interessante para alguém que gosta dos gêneros favoritos informados. " +
    //            "Retorne APENAS o 'id' numérico do filme escolhido e nada mais.";

    //    // Construir o prompt do usuário
    //    StringBuilder promptUserBuilder = new StringBuilder();
    //    promptUserBuilder.append("Gêneros Favoritos do Usuário:\n");
    //    if (generosFavoritos == null || generosFavoritos.isEmpty()) {
    //        promptUserBuilder.append("- Nenhum gênero favorito especificado.\n");
    //    } else {
    //        generosFavoritos.forEach(genre -> promptUserBuilder.append("- ").append(genre.getName()).append(" (ID: ").append(genre.getId()).append(")\n"));
    //    }

    //    promptUserBuilder.append("\nLista de Filmes Candidatos:\n");
    //    for (JsonElement filmeElement : filmesDeGenero) {
    //        JsonObject filmeObj = filmeElement.getAsJsonObject();
    //        int id = filmeObj.has("id") ? filmeObj.get("id").getAsInt() : -1;
    //        String title = filmeObj.has("title") ? filmeObj.get("title").getAsString() : "Título Desconhecido";
    //        JsonArray genreIdsArray = filmeObj.has("genre_ids") ? filmeObj.get("genre_ids").getAsJsonArray() : new JsonArray();

    //        promptUserBuilder.append("- Título: ").append(title).append(", ID: ").append(id);
    //        if (genreIdsArray.size() > 0) {
    //            promptUserBuilder.append(", Gêneros IDs: [");
    //            for (int i = 0; i < genreIdsArray.size(); i++) {
    //                promptUserBuilder.append(genreIdsArray.get(i).getAsInt()).append(i < genreIdsArray.size() - 1 ? ", " : "");
    //            }
    //            promptUserBuilder.append("]");
    //        }
    //        promptUserBuilder.append("\n");
    //    }
    //    promptUserBuilder.append("\nCom base nos gêneros favoritos do usuário e na lista de filmes acima, qual ID de filme você escolhe?");

    //    String promptUser = promptUserBuilder.toString();

    //    System.out.println("==================== PROMPT ENVIADO À IA (surpreendame) ====================");
    //    System.out.println("SYSTEM PROMPT: " + promptSystem);
    //    System.out.println("\nUSER PROMPT: " + promptUser);
    //    System.out.println("===========================================================================");

    //    // Inicializa o cliente da Azure OpenAI
    //     OpenAIClient client = new OpenAIClientBuilder()
    //             .credential(new AzureKeyCredential(apiKey))
    //             .endpoint(endpoint)
    //             .buildClient();

    //     // Monta as mensagens
    //     List<ChatRequestMessage> messages = Arrays.asList(
    //             new ChatRequestSystemMessage(promptSystem),
    //             new ChatRequestUserMessage(promptUser));

    //     // Define as opções da completude
    //     ChatCompletionsOptions options = new ChatCompletionsOptions(messages);
    //     options.setMaxTokens(6); // pequena resposta, só um id inteiro
    //     options.setTemperature(0.0); // determinístico, sem criatividade

    //     // Faz a requisição e obtém a resposta
    //     ChatCompletions response = client.getChatCompletions(deploymentName, options);
    //     ChatResponseMessage message = response.getChoices().get(0).getMessage();

    //     System.out.println("Resposta da IA: " + message.getContent());

    //     // Converte para inteiro e retorna
    //     try {
    //         return Integer.parseInt(message.getContent().trim());
    //     } catch (NumberFormatException e) {
    //         System.err.println("Erro ao processar resposta da IA: " + message.getContent());
    //         throw new RuntimeException("A IA não retornou um ID de filme válido", e);
    //     }

    // }
    
}