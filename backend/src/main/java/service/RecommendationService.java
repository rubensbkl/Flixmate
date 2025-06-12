package service;

import java.util.ArrayList;

import dao.RecommendationDAO;
import model.Recommendation;
import util.TMDBUtil;

public class RecommendationService {
    private RecommendationDAO recommendationDAO;
    private TMDBUtil tmdbUtil;

    public RecommendationService(RecommendationDAO recommendationDAO, TMDBUtil tmdbUtil) {
        this.recommendationDAO = recommendationDAO;
        this.tmdbUtil = tmdbUtil;
    }

    /**
     * Armazena uma recomendação de filme para um usuário.
     *
     * @param userId  ID do usuário
     * @param movieId ID do filme
     * @param score   Pontuação da recomendação
     * @return true se a recomendação foi armazenada com sucesso, false caso
     *         contrário
     */
    public boolean storeRecommendation(int userId, int movieId, double score) {
        return recommendationDAO.insert(userId, movieId, score);
    }

    /**
     * Obtém todas as recomendações de um usuário.
     *
     * @param userId ID do usuário
     * @return Lista de recomendações do usuário
     */
    public ArrayList<Recommendation> getRecommendationsByUserId(int userId) {
        ArrayList<Recommendation> recommendations = recommendationDAO.getRecommendationsByUserId(userId);
        return recommendations;
    }

    /**
     * Obtém recomendações de filmes para um usuário com base em seu histórico de
     * visualização.
     *
     * @param userId ID do usuário
     * @return Lista de recomendações de filmes
     */
    public ArrayList<Recommendation> getFavoritesByUserId(int movieId) {
        ArrayList<Recommendation> recommendations = recommendationDAO.getFavoritesByUserId(movieId);
        return recommendations;
    }

    /**
     * Obtém recomendações de filmes que o usuário já assistiu.
     *
     * @param movieId ID do filme
     * @return Lista de recomendações de filmes assistidos
     */
    public ArrayList<Recommendation> getWatchedByUserId(int movieId) {
        ArrayList<Recommendation> recommendations = recommendationDAO.getWatchedByUserId(movieId);
        return recommendations;
    }

    /**
     * Obtém uma recomendação específica de um usuário para um filme.
     *
     * @param userId  ID do usuário
     * @param movieId ID do filme
     * @return A recomendação do usuário para o filme, ou null se não houver
     *         recomendação
     */
    public Recommendation getRecommendationByUserIdAndMovieId(int userId, int movieId) {
        return recommendationDAO.getRecommendationByUserIdAndMovieId(userId, movieId);
    }

    /**
     * Atualiza uma recomendação de filme para um usuário.
     *
     * @param userId  ID do usuário
     * @param movieId ID do filme
     * @param score   Nova pontuação da recomendação
     * @return true se a recomendação foi atualizada com sucesso, false caso
     *         contrário
     */
    public boolean deleteRecommendation(int userId, int movieId) {
        return recommendationDAO.deleteRecommendation(userId, movieId);
    }

    /**
     * Verifica se um filme já foi recomendado para um usuário.
     *
     * @param userId  ID do usuário
     * @param movieId ID do filme
     * @return true se o filme já foi recomendado, false caso contrário
     */
    public boolean checkIfMovieAlreadyRecommended(int userId, int movieId) {
        return recommendationDAO.isMovieRecommended(userId, movieId);
    }

    /**
     * Obtém uma lista de IDs de filmes recomendados para um usuário.
     *
     * @param userId ID do usuário
     * @return Lista de IDs de filmes recomendados
     */
    public ArrayList<Integer> getRecommendedMoviesIds(int userId) {
        ArrayList<Recommendation> recommendations = recommendationDAO.getRecommendationsByUserId(userId);
        ArrayList<Integer> recommendedMoviesIds = new ArrayList<>();
        for (Recommendation recommendation : recommendations) {
            recommendedMoviesIds.add(recommendation.getMovieId());
        }
        return recommendedMoviesIds;
    }

    /**
     * Verifica se um filme específico foi recomendado para um usuário.
     *
     * @param userId  ID do usuário
     * @param movieId ID do filme
     * @return true se o filme foi recomendado, false caso contrário
     */
    public boolean isMovieRecommended(int userId, int movieId) {
        return recommendationDAO.isMovieRecommended(userId, movieId);
    }

}
