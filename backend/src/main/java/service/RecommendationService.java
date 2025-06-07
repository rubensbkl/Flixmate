package service;

import java.util.ArrayList;

import dao.RecommendationDAO;
import model.Recommendation;

public class RecommendationService {
    private RecommendationDAO recommendationDAO;
    private TMDBService tmdbService;

    public RecommendationService(RecommendationDAO recommendationDAO, TMDBService tmdbService) {
        this.recommendationDAO = recommendationDAO;
        this.tmdbService = tmdbService;
    }


    public boolean storeRecommendation(int userId, int movieId, double score) {
        return recommendationDAO.insert(userId, movieId, score);
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


    public boolean isMovieRecommended(int userId, int movieId) {
    return recommendationDAO.isMovieRecommended(userId, movieId);
}

}
