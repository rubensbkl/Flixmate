package service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import model.Feedback;

public class RecommendationService {

    private TMDBService tmdbService;

    public RecommendationService(TMDBService tmdbService) {
        this.tmdbService = tmdbService;
    }

    public JsonArray getCandidateMoviesJSON(List<Feedback> interacoes) {
        JsonArray candidateMovies = new JsonArray();
        Set<Integer> addedMovieIds = new HashSet<>();

        // Para cada interação positiva, tentamos buscar um filme similar
        for (Feedback interacao : interacoes) {
            try {
                // Apenas considerar filmes que o usuário gostou para buscar similares
                if (interacao.getFeedback()) {
                    JsonObject similar = tmdbService.getRandomSimilarMovie(interacao.getMovieId());

                    if (similar != null && similar.has("id")) {
                        int movieId = similar.get("id").getAsInt();

                        // Verificar se já adicionamos este filme
                        if (!addedMovieIds.contains(movieId)) {
                            candidateMovies.add(similar);
                            addedMovieIds.add(movieId);
                        }
                    }
                } else {
                    // Se a interação foi negativa, buscar um filme alternativo
                    JsonObject alternative = tmdbService.getARandomMovie();
                    if (alternative != null && alternative.has("id")) {
                        int movieId = alternative.get("id").getAsInt();

                        // Verificar se já adicionamos este filme
                        if (!addedMovieIds.contains(movieId)) {
                            candidateMovies.add(alternative);
                            addedMovieIds.add(movieId);
                        }
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Erro ao buscar similares para o filme ID: " + interacao.getMovieId());
            }
        }

        return candidateMovies;
    }
}
