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
    public boolean insert(Recommendation recommendation) {
        boolean status = false;
        try {
            String sql = "INSERT INTO recommendations (user_id, movie_id, watched, favorite) VALUES (?, ?, ?, ?)";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, recommendation.getUserId());
            st.setInt(2, recommendation.getMovieId());
            st.setBoolean(3, recommendation.isWatched());
            st.setBoolean(4, recommendation.isFavorite());

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
                boolean watched = rs.getBoolean("watched");
                boolean favorite = rs.getBoolean("favorite");

                Recommendation recommendation = new Recommendation(userId, movieId, watched, favorite);
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
     * Remove todas as recomendações de um usuário
     * 
     * @param userId O ID do usuário
     * @return true se a remoção foi bem-sucedida, false caso contrário
     */
    public boolean clearUserRecommendations(int userId) {
        boolean status = false;
        try {
            String sql = "DELETE FROM recommendations WHERE user_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);

            st.executeUpdate(); // Mesmo que não exclua nenhum registro, consideramos sucesso
            status = true;
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao limpar recomendações do usuário: " + e.getMessage());
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
     * Conta o número de recomendações para um usuário
     * 
     * @param userId O ID do usuário
     * @return O número de recomendações
     */
    public int countRecommendations(int userId) {
        int count = 0;
        try {
            String sql = "SELECT COUNT(*) FROM recommendations WHERE user_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao contar recomendações: " + e.getMessage());
        }
        return count;
    }

    public ArrayList<Recommendation> getFavoritesByUserId(int userId) {
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        try {
            String sql = "SELECT * FROM recommendations WHERE user_id = ? AND favorite = true";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int movieId = rs.getInt("movie_id");
                boolean watched = rs.getBoolean("watched");
                boolean favorite = rs.getBoolean("favorite");

                Recommendation recommendation = new Recommendation(userId, movieId, watched, favorite);
                recommendations.add(recommendation);
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao obter recomendações: " + e.getMessage());
        }
        return recommendations;
    }

    public ArrayList<Recommendation> getWatchedByUserId(int userId) {
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        try {
            String sql = "SELECT * FROM recommendations WHERE user_id = ? AND watched = true";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int movieId = rs.getInt("movie_id");
                boolean watched = rs.getBoolean("watched");
                boolean favorite = rs.getBoolean("favorite");

                Recommendation recommendation = new Recommendation(userId, movieId, watched, favorite);
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
     * Atualiza uma recomendação existente
     * 
     * @param recommendation A recomendação a ser atualizada
     * @return true se a atualização foi bem-sucedida, false caso contrário
     */
    public boolean update(Recommendation recommendation) {
        boolean status = false;
        try {
            String sql = "UPDATE recommendations SET watched = ?, favorite = ? WHERE user_id = ? AND movie_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setBoolean(1, recommendation.isWatched());
            st.setBoolean(2, recommendation.isFavorite());
            st.setInt(3, recommendation.getUserId());
            st.setInt(4, recommendation.getMovieId());

            int rowsAffected = st.executeUpdate();
            status = (rowsAffected > 0);
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar recomendação: " + e.getMessage());
        }
        return status;
    }

    // getRecommendationByUserIdAndMovieId
    public Recommendation getRecommendationByUserIdAndMovieId(int userId, int movieId) {
        Recommendation recommendation = null;
        try {
            String sql = "SELECT * FROM recommendations WHERE user_id = ? AND movie_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            st.setInt(2, movieId);

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                boolean watched = rs.getBoolean("watched");
                boolean favorite = rs.getBoolean("favorite");

                recommendation = new Recommendation(userId, movieId, watched, favorite);
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao obter recomendação: " + e.getMessage());
        }
        return recommendation;
    }

}