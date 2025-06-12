package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.Recommendation;

public class RecommendationDAO extends DAO {

    public RecommendationDAO(String serverName, String mydatabase, int porta, String username, String password) {
        super();
        conectar(serverName, mydatabase, porta, username, password);
    }

    public void finalize() {
        close();
    }

    /**
     * Insere uma nova recomendação no banco de dados
     * 
     * @param recommendation A recomendação a ser inserida
     * @return true se a inserção foi bem-sucedida, false caso contrário
     */
    public boolean insert(int userId, int movieId, double score) {
        boolean status = false;
        try {
            String sql = "INSERT INTO recommendations (user_id, movie_id, score) VALUES (?, ?, ?)";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            st.setInt(2, movieId);
            st.setDouble(3, score);

            int rowsAffected = st.executeUpdate();
            status = (rowsAffected > 0);
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao inserir recomendação: " + e.getMessage());
        }
        return status;
    }


    /**
     * Obtém todas as recomendações para um usuário específico
     * 
     * @param userId O ID do usuário
     * @return Lista de recomendações do usuário
     */
    public ArrayList<Recommendation> getRecommendationsByUserId(int userId) {
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        try {
            String sql = "SELECT * FROM recommendations WHERE user_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int movieId = rs.getInt("movie_id");
                double score = rs.getDouble("score");

                Recommendation recommendation = new Recommendation(userId, movieId, score);
                recommendations.add(recommendation);
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao obter recomendações: " + e.getMessage());
        }
        return recommendations;
    }

    // deleteRecommendation 
    /**
     * Remove uma recomendação específica do banco de dados
     * 
     * @param userId  O ID do usuário
     * @param movieId O ID do filme
     * @return true se a remoção foi bem-sucedida, false caso contrário
     */
    public boolean deleteRecommendation(int userId, int movieId) {
        boolean status = false;
        try {
            String sql = "DELETE FROM recommendations WHERE user_id = ? AND movie_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            st.setInt(2, movieId);

            int rowsAffected = st.executeUpdate();
            status = (rowsAffected > 0);
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao remover recomendação: " + e.getMessage());
        }
        return status;
    }

    /**
     * Verifica se um filme já foi recomendado para um usuário
     * 
     * @param userId  O ID do usuário
     * @param movieId O ID do filme
     * @return true se o filme já foi recomendado, false caso contrário
     */
    public boolean isMovieRecommended(int userId, int movieId) {
        boolean exists = false;
        try {
            String sql = "SELECT 1 FROM recommendations WHERE user_id = ? AND movie_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            st.setInt(2, movieId);

            ResultSet rs = st.executeQuery();
            exists = rs.next();

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao verificar recomendação: " + e.getMessage());
        }
        return exists;
    }

    /**
     * Obtém todas as recomendações favoritas de um usuário específico
     * 
     * @param userId O ID do usuário
     * @return Lista de recomendações favoritas do usuário
     */
    public ArrayList<Recommendation> getFavoritesByUserId(int userId) {
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        try {
            String sql = "SELECT * FROM recommendations WHERE user_id = ? AND favorite = true";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int movieId = rs.getInt("movie_id");
                double score = rs.getDouble("score");

                Recommendation recommendation = new Recommendation(userId, movieId, score);
                recommendations.add(recommendation);
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao obter recomendações: " + e.getMessage());
        }
        return recommendations;
    }

    /**
     * Obtém todas as recomendações assistidas de um usuário específico
     * 
     * @param userId O ID do usuário
     * @return Lista de recomendações assistidas do usuário
     */
    public ArrayList<Recommendation> getWatchedByUserId(int userId) {
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        try {
            String sql = "SELECT * FROM recommendations WHERE user_id = ? AND watched = true";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int movieId = rs.getInt("movie_id");
                double score = rs.getDouble("score");

                Recommendation recommendation = new Recommendation(userId, movieId, score);
                recommendations.add(recommendation);
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao obter recomendações: " + e.getMessage());
        }
        return recommendations;
    }

    /**
     * Obtém uma recomendação específica por ID de usuário e ID de filme
     * 
     * @param userId  O ID do usuário
     * @param movieId O ID do filme
     * @return A recomendação encontrada ou null se não existir
     */
    public Recommendation getRecommendationByUserIdAndMovieId(int userId, int movieId) {
        Recommendation recommendation = null;
        try {
            String sql = "SELECT * FROM recommendations WHERE user_id = ? AND movie_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            st.setInt(2, movieId);

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                double score = rs.getDouble("score");

                recommendation = new Recommendation(userId, movieId, score);
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao obter recomendação: " + e.getMessage());
        }
        return recommendation;
    }


}