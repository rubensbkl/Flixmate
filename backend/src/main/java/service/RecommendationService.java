package service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dao.RecommendationDAO;
import model.Feedback;
import model.Recommendation;

public class RecommendationService {
    private RecommendationDAO recommendationDAO;
    private TMDBService tmdbService;

    public RecommendationService(RecommendationDAO recommendationDAO, TMDBService tmdbService) {
        this.recommendationDAO = recommendationDAO;
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

    public boolean storeRecommendation(int userId, int movieId) {
        Recommendation rec = new Recommendation(userId, movieId);
        return recommendationDAO.insert(rec);
    }

    public ArrayList<Recommendation> getRecommendationsByUserId(int userId) {
        ArrayList<Recommendation> recommendations = recommendationDAO.getRecommendationsByUserId(userId);
        return recommendations;
    }

    public ArrayList<Recommendation> getFavoritesByUserId(int movieId) {
        ArrayList<Recommendation> recommendations = recommendationDAO.getFavoritesByUserId(movieId);
        return recommendations;
    }

    public ArrayList<Recommendation> getWatchedByUserId(int movieId) {
        ArrayList<Recommendation> recommendations = recommendationDAO.getWatchedByUserId(movieId);
        return recommendations;
    }

  
    public boolean updateRecommendation(int userId, int movieId, boolean watched, boolean favorite) {
        Recommendation rec = new Recommendation(userId, movieId, watched, favorite);
        return recommendationDAO.update(rec);
    }

    // getRecommendationByUserIdAndMovieId
    public Recommendation getRecommendationByUserIdAndMovieId(int userId, int movieId) {
        return recommendationDAO.getRecommendationByUserIdAndMovieId(userId, movieId);
    }

    // deleteRecommendation
    public boolean deleteRecommendation(int userId, int movieId) {
        return recommendationDAO.deleteRecommendation(userId, movieId);
    }

    //checkIfMovieAlreadyRecommended
    public boolean checkIfMovieAlreadyRecommended(int userId, int movieId) {
        return recommendationDAO.isMovieRecommended(userId, movieId);
    }

    public ArrayList<Integer> getRecommendedMoviesIds(int userId) {
        ArrayList<Recommendation> recommendations = recommendationDAO.getRecommendationsByUserId(userId);
        ArrayList<Integer> recommendedMoviesIds = new ArrayList<>();
        for (Recommendation recommendation : recommendations) {
            recommendedMoviesIds.add(recommendation.getMovieId());
        }
        return recommendedMoviesIds;
    }
}
