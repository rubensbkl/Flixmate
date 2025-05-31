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
}
