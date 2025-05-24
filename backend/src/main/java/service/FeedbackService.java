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
            if (movieService.getMovieById(movieId) == null) {
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
    public ArrayList<Feedback> getFeedbacksByUserId(int userId) {
        try {
            // Verificar se o feedback existe
            ArrayList<Feedback> feedbacks = feedbackDAO.getFeedbacksByUserId(userId);
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

    public boolean clearAllById(int userId) {
        try {
            // Verificar se o feedback existe
            if (feedbackDAO.getFeedbacksByUserId(userId) == null) {
                System.err.println("Nenhum feedback encontrado para o usuário " + userId);
                throw new NoSuchFieldException("Nenhum feedback encontrado");
            }

            // Verificar se o usuário existe
            if (userId <= 0) {
                System.err.println("Usuário não encontrado");
                throw new NoSuchFieldException("Usuário não encontrado");
            }

            // Verificar se ha feedbacks para o usuário
            if (feedbackDAO.getFeedbacksByUserId(userId).isEmpty()) {
                System.err.println("Nenhum feedback encontrado para o usuário " + userId);
                throw new NoSuchFieldException("Nenhum feedback encontrado");
            }

            // Limpar todos os feedbacks do usuário
            if (feedbackDAO.clearAllById(userId)) {
                System.out.println("Feedbacks removidos com sucesso");
            } else {
                System.err.println("Erro ao remover feedbacks");
                throw new SQLException("Erro ao remover feedbacks");
            }
            return true;
        } catch (Exception e) {
            // Tratar exceções específicas
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

    public Integer getRating(int userId, int movieId) {
        try {
            // Verificar se o feedback existe
            Feedback feedback = feedbackDAO.getFeedback(userId, movieId);
            if (feedback == null) {
                System.err.println("Feedback não encontrado");
                throw new NoSuchFieldException("Feedback não encontrado");
            }
            return feedback.getFeedback() ? 1 : 0;
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
     * Armazena ou atualiza a avaliação (rating) de um usuário para um filme.
     * Se já existir um feedback para o usuário e filme informados, verifica se o valor do feedback é igual ao novo valor.
     * Caso seja igual, não realiza nenhuma alteração e retorna false.
     * Caso seja diferente, atualiza o feedback existente com o novo valor.
     * Se não existir feedback, cria um novo registro de feedback.
     *
     * @param userId  o ID do usuário que está avaliando
     * @param movieId o ID do filme a ser avaliado
     * @param rating  o valor da avaliação (true para positivo, false para negativo)
     * @return true se o feedback foi armazenado ou atualizado com sucesso, false caso contrário
     */
    public boolean storeOrUpdateRating(int userId, int movieId, boolean rating) {
        try {
            Feedback existingFeedback = feedbackDAO.getFeedback(userId, movieId);
            if (existingFeedback != null) {
                if (existingFeedback.getFeedback() == rating) {
                    System.err.println("Feedback já existe com o mesmo valor");
                    return false;
                }
                existingFeedback.setFeedback(rating);
                return feedbackDAO.update(existingFeedback);
            } else {
                return storeFeedback(userId, movieId, rating);
            }
        } catch (Exception e) {
            System.err.println("Erro ao armazenar ou atualizar feedback: " + e.getMessage());
            return false;
        }
    }

}