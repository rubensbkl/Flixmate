package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Feedback;
import model.Genre;
import model.Movie;

import util.AiOutput;

import java.lang.reflect.Type;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

/**
 * Classe auxiliar para processar recomendações de filmes
 */
public class RecommendationHelper {
    // ArrayList de IDs de filmes candidatos
    private ArrayList<Integer> candidateMovieIds;
    
    // Mapeamento para armazenar informações temporariamente
    private Map<Integer, MovieWithGenres> moviesWithGenresMap;
    
    /**
     * Construtor
     */
    public RecommendationHelper() {
        this.candidateMovieIds = new ArrayList<>();
        this.moviesWithGenresMap = new HashMap<>();
    }
    
    /**
     * Adiciona um filme com seus gêneros ao mapa
     */
    public void addMovieWithGenres(Movie movie, List<Genre> genres) {
        if (movie != null) {
            MovieWithGenres movieWithGenres = new MovieWithGenres(movie, genres);
            moviesWithGenresMap.put(movie.getId(), movieWithGenres);
        }
    }
    
    /**
     * Adiciona um ID de filme candidato à lista
     */
    public void addCandidateMovieId(int movieId) {
        if (!candidateMovieIds.contains(movieId)) {
            candidateMovieIds.add(movieId);
        }
    }
    
    /**
     * Retorna a lista de IDs de filmes candidatos
     */
    public ArrayList<Integer> getCandidateMovieIds() {
        return new ArrayList<>(candidateMovieIds);
    }
    
    /**
     * Retorna o filme com gêneros pelo ID
     */
    public MovieWithGenres getMovieWithGenres(int movieId) {
        return moviesWithGenresMap.get(movieId);
    }
    
    /**
     * Verifica se um filme está no mapa
     */
    public boolean hasMovie(int movieId) {
        return moviesWithGenresMap.containsKey(movieId);
    }
    
    /**
     * Limpa todos os dados armazenados
     */
    public void clear() {
        candidateMovieIds.clear();
        moviesWithGenresMap.clear();
    }
    
    /**
     * Retorna o número total de filmes armazenados
     */
    public int getMovieCount() {
        return moviesWithGenresMap.size();
    }
    
    /**
     * Retorna o número total de filmes candidatos
     */
    public int getCandidateCount() {
        return candidateMovieIds.size();
    }
    
    /**
     * Cria o prompt para a IA de forma otimizada
     */
    public String createAIPrompt(List<Feedback> interacoes, List<Genre> generosFavoritos) {
        StringBuilder promptUser = new StringBuilder();
        promptUser.append("As informações do usuário são as seguintes:\n\n");
        
        // Adicionar gêneros favoritos
        promptUser.append("GÊNEROS FAVORITOS DO USUÁRIO:\n");
        for (Genre genre : generosFavoritos) {
            promptUser.append("- ID ").append(genre.getId()).append(": ").append(genre.getName()).append("\n");
        }
        promptUser.append("\n");

        // Adicionar interações anteriores com detalhes dos filmes
        promptUser.append("HISTÓRICO DE INTERAÇÕES DO USUÁRIO:\n");
        if (interacoes.isEmpty()) {
            promptUser.append("- Nenhuma interação anterior registrada\n");
        } else {
            for (Feedback interacao : interacoes) {
                int movieId = interacao.getMovieId();
                MovieWithGenres movie = moviesWithGenresMap.get(movieId);
                
                if (movie != null) {
                    promptUser.append("- Filme ID ").append(movieId)
                        .append(": \"").append(movie.getTitle()).append("\" (").append(movie.getReleaseDate()).append(")\n")
                        .append("   Gêneros: ").append(movie.getGenresAsString()).append("\n")
                        .append("   Popularidade: ").append(movie.getPopularity())
                        .append(", Adulto: ").append(movie.isAdult())
                        .append(", Idioma: ").append(movie.getOriginalLanguage())
                        .append(interacao.getFeedback() ? " ✓ GOSTOU" : " ✗ NÃO GOSTOU")
                        .append("\n");
                }
            }
        }
        promptUser.append("\n");

        // Adicionar filmes candidatos
        promptUser.append("FILMES CANDIDATOS PARA RECOMENDAÇÃO:\n");
        int counter = 1;
        for (Integer movieId : candidateMovieIds) {
            MovieWithGenres movie = moviesWithGenresMap.get(movieId);
            if (movie != null) {
                promptUser.append(counter++).append(". ID ").append(movie.getId())
                        .append(": \"").append(movie.getTitle()).append("\" (").append(movie.getReleaseDate()).append(")\n")
                        .append("   Gêneros: ").append(movie.getGenresAsString()).append("\n")
                        .append("   Popularidade: ").append(movie.getPopularity())
                        .append(", Adulto: ").append(movie.isAdult())
                        .append(", Idioma: ").append(movie.getOriginalLanguage())
                        .append("\n");
            }
        }

        promptUser.append("\nEscolha o filme com melhor afinidade com o usuário com base nos gêneros favoritos e no histórico de interações. " +
                      "Considere que os filmes que o usuário gostou têm mais peso que os que não gostou. " +
                      "Retorne apenas o 'id' do filme escolhido, como um número inteiro.");
        
        return promptUser.toString();
    }
    
    /**
     * Verifica se há dados suficientes para gerar uma recomendação
     */
    public boolean hasEnoughDataForRecommendation() {
        return !candidateMovieIds.isEmpty() && !moviesWithGenresMap.isEmpty();
    }

    public static String gerarJsonParaAzureML(int userId, List<Integer> candidateMovieIds) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < candidateMovieIds.size(); i++) {
            int movieId = candidateMovieIds.get(i);
            json.append("{")
                .append("\"user_id\": ").append(userId).append(", ")
                .append("\"movie_id\": ").append(movieId)
                .append("}");

            if (i < candidateMovieIds.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        return json.toString();
    }

    public List<AiOutput> parseRespostaAzure(String json) {
        List<AiOutput> lista = new ArrayList<>();

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonObject results = root.getAsJsonObject("Results");
        JsonArray outputs = results.getAsJsonArray("WebServiceOutput0");

        // A Azure retorna uma lista com 1 objeto contendo Rated Item 1..5, Predicted Rating 1..5, etc.
        if (outputs.size() > 0) {
            JsonObject item = outputs.get(0).getAsJsonObject();

            // Os pares vêm em índices de 1 a 5 (5 recomendações)
            for (int i = 1; i <= 5; i++) {
                String movieKey = "Rated Item " + i;
                String ratingKey = "Predicted Rating " + i;

                if (item.has(movieKey) && item.has(ratingKey)) {
                    AiOutput ao = new AiOutput();
                    ao.user_id = Integer.parseInt(item.get("User").getAsString());
                    ao.movie_id = Integer.parseInt(item.get(movieKey).getAsString());
                    ao.predicted_rating = item.get(ratingKey).getAsDouble();

                    lista.add(ao);
                }
            }
        }
        return lista;
    }
}