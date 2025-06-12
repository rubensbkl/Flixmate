package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.WatchLater;
public class WatchLaterDAO extends DAO {

    public WatchLaterDAO(String serverName, String mydatabase, int porta, String username, String password) {
        super();
        conectar(serverName, mydatabase, porta, username, password);
    }

    @Override
    public void finalize() {
        close();
    }

    /**
     * Busca os IDs dos filmes na watchlist de um usuário específico.
     * 
     * @param userId ID do usuário
     * @return Lista de IDs de filmes na watchlist
     */
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

    /**
     * Adiciona um filme à watchlist de um usuário.
     * Se o filme já estiver na watchlist, não faz nada (ON CONFLICT DO NOTHING).
     * 
     * @param userId  ID do usuário
     * @param movieId ID do filme
     * @return true se a adição foi bem-sucedida, false caso contrário
     */
    public boolean addToWatchLater(WatchLater watchLater)  {
        String sql = "INSERT INTO watchlater (user_id, movie_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setInt(1, watchLater.getUserId());
            st.setInt(2, watchLater.getMovieId());

            int rowsAffected = st.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar filme à watchlist: " + e.getMessage(), e);
        }
    }

    /**
     * Remove um filme da watchlist de um usuário.
     * Retorna true se a remoção foi bem-sucedida, false caso contrário.
     * 
     * @param userId  ID do usuário
     * @param movieId ID do filme
     * @return true se a remoção foi bem-sucedida, false caso contrário
     */
    public boolean removeFromWatchLater(WatchLater watchLater) {
        String sql = "DELETE FROM watchlater WHERE user_id = ? AND movie_id = ?";

        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setInt(1, watchLater.getUserId());
            st.setInt(2, watchLater.getMovieId());

            int rowsAffected = st.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover filme da watchlist: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica se um filme está na watchlist de um usuário.
     * Retorna true se o filme estiver na watchlist, false caso contrário.
     * 
     * @param userId  ID do usuário
     * @param movieId ID do filme
     * @return true se o filme estiver na watchlist, false caso contrário
     */
    public boolean isInWatchLater(WatchLater watchLater) {
        String sql = "SELECT 1 FROM watchlater WHERE user_id = ? AND movie_id = ?";

        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setInt(1, watchLater.getUserId());
            st.setInt(2, watchLater.getMovieId());

            ResultSet rs = st.executeQuery();
            boolean exists = rs.next();
            rs.close();
            return exists;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar watchlist: " + e.getMessage(), e);
        }
    }

}
