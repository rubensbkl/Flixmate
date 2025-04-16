package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Feedback;

public class FeedbackDAO extends DAO {

    public FeedbackDAO(String serverName, String mydatabase, int porta, String username, String password) {
        super();
        conectar(serverName, mydatabase, porta, username, password);
    }

    public void finalize() {
        close();
    }

    /**
     * Insere uma nova interação no banco de dados
     * @param feedback A interação a ser inserida
     * @return true se a inserção foi bem-sucedida, false caso contrário
     */
    public boolean insert(Feedback feedback) {
        boolean status = false;
        try {
            String sql = "INSERT INTO feedbacks (user_id, movie_id, feedback) VALUES (?, ?, ?)";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, feedback.getUserId());
            st.setInt(2, feedback.getMovieId());
            st.setBoolean(3, feedback.getFeedback());
            
            int affectedRows = st.executeUpdate();
            status = (affectedRows > 0);
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao inserir interação: " + e.getMessage());
        }
        return status;
    }

    /**
     * Busca todas as interações de um usuário específico
     * @param userId O ID do usuário
     * @return Lista de interações do usuário
     */
    public List<Feedback> getFeedbacksByUserId(int userId) {
        List<Feedback> interacoes = new ArrayList<>();
        try {
            String sql = "SELECT * FROM feedbacks WHERE user_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                int movieId = rs.getInt("movie_id");
                boolean feedback = rs.getBoolean("feedback");
                interacoes.add(new Feedback(userId, movieId, feedback));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar interações do usuário: " + e.getMessage());
        }
        return interacoes;
    }

    /**
     * Busca interações de um usuário com um filme específico
     * @param userId O ID do usuário
     * @param movieId O ID do filme
     * @return A interação encontrada ou null se não existir
     */
    public Feedback getFeedback(int userId, int movieId) {
        Feedback feedback = null;
        try {
            String sql = "SELECT * FROM feedbacks WHERE user_id = ? AND movie_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            st.setInt(2, movieId);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                boolean value = rs.getBoolean("feedback");
                feedback = new Feedback(userId, movieId, value);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar interação específica: " + e.getMessage());
        }
        return feedback;
    }

    /**
     * Remove todas as interações de um usuário
     * @param userId O ID do usuário
     * @return true se a remoção foi bem-sucedida, false caso contrário
     */
    public boolean clear(int userId) {
        boolean status = false;
        try {
            String sql = "DELETE FROM feedbacks WHERE user_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            
            int affectedRows = st.executeUpdate();
            status = (affectedRows > 0); // Considera sucesso mesmo se não houver registros para excluir
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao limpar interações do usuário: " + e.getMessage());
        }
        return status;
    }

    /**
     * Atualiza uma interação existente
     * @param feedback A interação com os dados atualizados
     * @return true se a atualização foi bem-sucedida, false caso contrário
     */
    public boolean update(Feedback feedback) {
        boolean status = false;
        try {
            String sql = "UPDATE feedbacks SET feedback = ? WHERE user_id = ? AND movie_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setBoolean(1, feedback.getFeedback());
            st.setInt(2, feedback.getUserId());
            st.setInt(3, feedback.getMovieId());
            
            int affectedRows = st.executeUpdate();
            status = (affectedRows > 0);
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar interação: " + e.getMessage());
        }
        return status;
    }

    /**
     * Conta o número de interações de um usuário
     * @param userId O ID do usuário
     * @return O número de interações
     */
    public int countfeedbacks(int userId) {
        int count = 0;
        try {
            String sql = "SELECT COUNT(*) FROM feedbacks WHERE user_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao contar interações: " + e.getMessage());
        }
        return count;
    }
}