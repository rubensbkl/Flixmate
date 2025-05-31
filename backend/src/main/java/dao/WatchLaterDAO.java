package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class WatchLaterDAO extends DAO {
    
    public WatchLaterDAO(String serverName, String mydatabase, int porta, String username, String password) {
        super();
        conectar(serverName, mydatabase, porta, username, password);
    }
    
    public ArrayList<Integer> getWatchLaterMovieIds(int userId) {
        ArrayList<Integer> movieIds = new ArrayList<>();
        String sql = "SELECT movie_id FROM watchlater WHERE user_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            
            while (rs.next()) {
                movieIds.add(rs.getInt("movie_id"));
            }
            
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar watchlater: " + e.getMessage(), e);
        }
        
        return movieIds;
    }

public boolean addToWatchLater(int userId, int movieId) {
    String sql = "INSERT INTO watchlater (user_id, movie_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
    
    try (PreparedStatement st = conexao.prepareStatement(sql)) {
        st.setInt(1, userId);
        st.setInt(2, movieId);
        
        int rowsAffected = st.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        throw new RuntimeException("Erro ao adicionar filme à watchlist: " + e.getMessage(), e);
    }
}

public boolean removeFromWatchLater(int userId, int movieId) {
    String sql = "DELETE FROM watchlater WHERE user_id = ? AND movie_id = ?";
    
    try (PreparedStatement st = conexao.prepareStatement(sql)) {
        st.setInt(1, userId);
        st.setInt(2, movieId);
        
        int rowsAffected = st.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        throw new RuntimeException("Erro ao remover filme da watchlist: " + e.getMessage(), e);
    }
}

public boolean isInWatchLater(int userId, int movieId) {
    String sql = "SELECT 1 FROM watchlater WHERE user_id = ? AND movie_id = ?";
    
    try (PreparedStatement st = conexao.prepareStatement(sql)) {
        st.setInt(1, userId);
        st.setInt(2, movieId);
        
        ResultSet rs = st.executeQuery();
        boolean exists = rs.next();
        rs.close();
        return exists;
        
    } catch (SQLException e) {
        throw new RuntimeException("Erro ao verificar watchlist: " + e.getMessage(), e);
    }
}

}
