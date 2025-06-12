package service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.FeedbackDAO;
import model.Feedback;

public class FeedbackService {
    private final FeedbackDAO feedbackDAO;
    private final MovieService movieService;

    public FeedbackService(FeedbackDAO feedbackDAO, MovieService movieService) {
        this.feedbackDAO = feedbackDAO;
        this.movieService = movieService;
    }

    /**
     * Obtém o feedback de um usuário para um filme específico.
     *
     * @param userId  ID do usuário
     * @param movieId ID do filme
     * @return Feedback do usuário para o filme, ou null se não houver feedback
     */
    public Feedback getFeedback(int userId, int movieId) {
        try {
            Feedback feedback = feedbackDAO.getFeedback(userId, movieId);
            if (feedback == null) {
                System.err.println("Feedback não encontrado");
                throw new NoSuchFieldException("Feedback não encontrado");
            }
            return feedback;
        } catch (Exception e) {
            if (e instanceof NoSuchFieldException) {
                System.err.println("Erro: " + e.getMessage());
                return null;
            } else {
                System.err.println("Erro inesperado: " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * Obtém todos os feedbacks de um usuário específico.
     *
     * @param userId ID do usuário
     * @return Lista de feedbacks do usuário, ou null se não houver feedbacks
     */
    public ArrayList<Feedback> getFeedbacksByUserId(int userId) {
        try {
            ArrayList<Feedback> feedbacks = feedbackDAO.getFeedbacksByUserId(userId);
            if (feedbacks == null || feedbacks.isEmpty()) {
                System.err.println("Nenhum feedback encontrado para o usuário " + userId);
                throw new NoSuchFieldException("Nenhum feedback encontrado");
            }
            return feedbacks;
        } catch (Exception e) {
            if (e instanceof NoSuchFieldException) {
                System.err.println("Erro: " + e.getMessage());
                return null;
            } else {
                System.err.println("Erro inesperado: " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * Limpa todos os feedbacks de um usuário específico.
     *
     * @param userId ID do usuário
     * @return true se os feedbacks foram removidos com sucesso, false caso
     *         contrário
     */
    public boolean clearAllById(int userId) {
        try {
            if (feedbackDAO.getFeedbacksByUserId(userId) == null) {
                System.err.println("Nenhum feedback encontrado para o usuário " + userId);
                throw new NoSuchFieldException("Nenhum feedback encontrado");
            }

            if (userId <= 0) {
                System.err.println("Usuário não encontrado");
                throw new NoSuchFieldException("Usuário não encontrado");
            }

            if (feedbackDAO.getFeedbacksByUserId(userId).isEmpty()) {
                System.err.println("Nenhum feedback encontrado para o usuário " + userId);
                throw new NoSuchFieldException("Nenhum feedback encontrado");
            }

            if (feedbackDAO.clearAllById(userId)) {
                System.out.println("Feedbacks removidos com sucesso");
            } else {
                System.err.println("Erro ao remover feedbacks");
                throw new SQLException("Erro ao remover feedbacks");
            }
            return true;
        } catch (Exception e) {
            if (e instanceof NoSuchFieldException) {
                System.err.println("Erro: " + e.getMessage());
                return false;
            } else if (e instanceof SQLException) {
                System.err.println("Erro: " + e.getMessage());
                return false;
            } else {
                System.err.println("Erro inesperado: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Obtém uma lista de IDs de filmes que o usuário avaliou.
     *
     * @param userId ID do usuário
     * @return Lista de IDs de filmes avaliados pelo usuário
     */
    public ArrayList<Integer> getRatedMoviesIds(int userId) {
        ArrayList<Integer> feedbacksIds = new ArrayList<>();
        try {
            List<Feedback> feedbacks = feedbackDAO.getFeedbacksByUserId(userId);
            if (feedbacks == null || feedbacks.isEmpty()) {
                System.err.println("Nenhum feedback encontrado");
                throw new NoSuchFieldException("Nenhum feedback encontrado");
            }

            for (Feedback feedback : feedbacks) {
                feedbacksIds.add(feedback.getMovieId());
            }
            return feedbacksIds;
        } catch (Exception e) {
            // Tratar exceções específicas
            if (e instanceof NoSuchFieldException) {
                System.err.println("Erro: " + e.getMessage());
                return null;
            } else {
                System.err.println("Erro inesperado: " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * Obtém o rating de um usuário para um filme específico.
     *
     * @param userId  ID do usuário
     * @param movieId ID do filme
     * @return 1 se o feedback for positivo, 0 se for negativo, null se não houver
     *         feedback
     */
    public Integer getRating(int userId, int movieId) {
        try {
            Feedback feedback = feedbackDAO.getFeedback(userId, movieId);
            if (feedback == null) {
                System.err.println("Feedback não encontrado");
                throw new NoSuchFieldException("Feedback não encontrado");
            }
            return feedback.getFeedback() ? 1 : 0;
        } catch (Exception e) {
            if (e instanceof NoSuchFieldException) {
                System.err.println("Erro: " + e.getMessage());
                return null;
            } else {
                System.err.println("Erro inesperado: " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * Armazena ou atualiza o rating de um usuário para um filme específico.
     *
     * @param userId  ID do usuário
     * @param movieId ID do filme
     * @param rating  Valor do rating (true para positivo, false para negativo)
     * @return 1 se o rating foi criado, 2 se atualizado, 3 se ignorado, 0 em caso
     *         de erro
     */
    public int storeOrUpdateRating(int userId, int movieId, boolean rating) {
        try {
            Feedback feedback = feedbackDAO.getFeedback(userId, movieId);
            if (feedback != null) {
                if (feedback.getFeedback() == rating) {
                    System.out.println("[🏅:⚪] RATING IGNORED: [userId: " + rating + ", movieId: " + userId
                            + ", rating: " + rating + "]");
                    return 3;
                } else {
                    feedback.setFeedback(rating);
                    boolean updated = feedbackDAO.update(feedback);
                    if (updated) {
                        System.out.println("[🏅:🔵] RATING UPDATE SUCCESS: [userId: " + rating + ", movieId: " + userId
                                + ", rating: " + rating + "]");
                    } else {
                        System.out.println("[🏅:🔴] RATING UPDATE ERROR: [userId: " + rating + ", movieId: " + userId
                                + ", rating: " + rating + "]");
                    }
                    return updated ? 2 : 0;
                }
            } else {
                feedback = new Feedback(userId, movieId, rating);
                boolean created = feedbackDAO.insert(feedback);
                if (created) {
                    System.out.println("[🏅:🟢] RATING CREATE SUCCESS: [userId: " + rating + ", movieId: " + userId
                            + ", rating: " + rating + "]");
                } else {
                    System.out.println("[🏅:🔴] RATING CREATE ERROR: [userId: " + rating + ", movieId: " + userId
                            + ", rating: " + rating + "]");
                }
                return created ? 1 : 0;
            }
        } catch (Exception e) {
            System.err.println("[🏅:🔴] RATING ERROR: storeOrUpdateRating - " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Remove o rating de um usuário para um filme específico.
     *
     * @param userId  ID do usuário
     * @param movieId ID do filme
     * @return true se o rating foi removido com sucesso, false caso contrário
     */
    public boolean removeRating(int userId, int movieId) {
        try {
            return feedbackDAO.removeFeedback(userId, movieId);
        } catch (Exception e) {
            System.err.println("Erro ao remover feedback: " + e.getMessage());
            return false;
        }
    }

}