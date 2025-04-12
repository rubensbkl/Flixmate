package dao;

import java.sql.*;

import model.Recommendation;

public class RecommendationDAO extends DAO {

    public RecommendationDAO(String serverName, String mydatabase, int porta, String username, String password) {
        super();
        conectar(serverName, mydatabase, porta, username, password);
    }

    public void finalize() {
        close();
    }

    public boolean insert(Recommendation recommendation) {
        boolean status = false;

        try {
            String sql = "INSERT INTO recommendations (user_id, movie_id) VALUES (?, ?)";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, recommendation.getUserId());
            st.setInt(2, recommendation.getMovieId());
            st.executeUpdate();
            st.close();
            status = true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return status;
    }
    
}
