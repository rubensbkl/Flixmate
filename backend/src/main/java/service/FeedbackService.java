package service;

import java.sql.SQLException;

import dao.FeedbackDAO;
import model.Feedback;

public class FeedbackService {
    private final FeedbackDAO feedbackDAO;
    private final MovieService movieService;

    public FeedbackService(FeedbackDAO feedbackDAO, MovieService movieService) {
        this.feedbackDAO = feedbackDAO;
        this.movieService = movieService;
    }

    public boolean registrarFeedback(Feedback feedback) {
        try {
            // Verificar se o feedback é válido
            if (feedback == null || feedback.getUserId() <= 0 || feedback.getMovieId() <= 0) {
                throw new NoSuchFieldException("Feedback inválido");
            }

            if (feedbackDAO.getFeedback(feedback.getUserId(), feedback.getMovieId()) != null) {
                System.err.println("Feedback já existe para o usuário " + feedback.getUserId() + " e filme "
                        + feedback.getMovieId());
                throw new IllegalStateException("Feedback já existe");
            }

            // Obter o filme do feedback
            int movieId = feedback.getMovieId();

            boolean movieSaved = movieService.buscarESalvarFilme(movieId);

            if (!movieSaved) {
                System.err.println("Erro ao salvar filme ID " + movieId);
                throw new IllegalArgumentException("Erro ao salvar filme");
            }

            // Inserir o feedback
            if (feedbackDAO.insert(feedback)) {
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
}