package service;

import java.sql.SQLException;
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

    public boolean storeFeedback(int userId, int movieId, boolean feedback) {
        // Criar um objeto Feedback
        Feedback feedbackObj = new Feedback(userId, movieId, feedback);

        try {
            // Verificar se o feedback é válido
            if (feedbackObj == null || feedbackObj.getUserId() <= 0 || feedbackObj.getMovieId() <= 0) {
                throw new NoSuchFieldException("Feedback inválido");
            }

            // Verificar se o feedback já existe
            if (feedbackDAO.getFeedback(feedbackObj.getUserId(), feedbackObj.getMovieId()) != null) {
                System.err.println("Feedback já existe para o usuário " + feedbackObj.getUserId() + " e filme "
                        + feedbackObj.getMovieId());
                throw new IllegalStateException("Feedback já existe");
            }

            // Verificar se o filme existe
            if (movieService.buscarFilmePorId(movieId) == null) {
                System.err.println("Filme não encontrado");
                throw new NoSuchFieldException("Filme não encontrado");
            }

            // Inserir o feedback
            if (feedbackDAO.insert(feedbackObj)) {
                System.out.println("Feedback registrado com sucesso");
            } else {
                System.err.println("Erro ao registrar feedback");
                throw new SQLException("Erro ao registrar feedback");
            }

            return true;
        } catch (Exception e) {
            // Tratar exceções específicas
            if (e instanceof NoSuchFieldException) {
                System.err.println("Erro: " + e.getMessage());
                return false;
            } else if (e instanceof IllegalStateException) {
                System.err.println("Erro: " + e.getMessage());
                return true;
            } else if (e instanceof SQLException) {
                System.err.println("Erro: " + e.getMessage());
                return false;
            } else {
                System.err.println("Erro inesperado: " + e.getMessage());
            }
            return false;
        }
    }

    public Feedback getFeedback(int userId, int movieId) {
        try {
            // Verificar se o feedback existe
            Feedback feedback = feedbackDAO.getFeedback(userId, movieId);
            if (feedback == null) {
                System.err.println("Feedback não encontrado");
                throw new NoSuchFieldException("Feedback não encontrado");
            }
            return feedback;
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

    // feedbackDAO.getFeedbacksByUserId
    public List<Feedback> getFeedbacksByUserId(int userId) {
        try {
            // Verificar se o feedback existe
            List<Feedback> feedbacks = feedbackDAO.getFeedbacksByUserId(userId);
            if (feedbacks == null || feedbacks.isEmpty()) {
                System.err.println("Nenhum feedback encontrado para o usuário " + userId);
                throw new NoSuchFieldException("Nenhum feedback encontrado");
            }
            return feedbacks;
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

}