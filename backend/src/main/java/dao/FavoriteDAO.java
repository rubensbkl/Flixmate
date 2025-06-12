package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.Favorite;
public class FavoriteDAO extends DAO {

    public FavoriteDAO(String serverName, String mydatabase, int porta, String username, String password) {
        super();
        conectar(serverName, mydatabase, porta, username, password);
    }

    /**
     * Busca os IDs dos filmes favoritos de um usuário específico.
     */
    public ArrayList<Integer> getFavoriteMovieIds(int userId) {
        ArrayList<Integer> movieIds = new ArrayList<>();
        String sql = "SELECT movie_id FROM favorite WHERE user_id = ? ORDER BY created_at DESC";

        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                movieIds.add(rs.getInt("movie_id"));
            }

            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar favoritos: " + e.getMessage(), e);
        }

        return movieIds;
    }

    /**
     * Adiciona um filme aos favoritos de um usuário.
     * Se o filme já estiver nos favoritos, não faz nada (ON CONFLICT DO NOTHING).
     */
    public boolean addToFavorites(Favorite favorite) {

        String sql = "INSERT INTO favorite (user_id, movie_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setInt(1, favorite.getUserId());
            st.setInt(2, favorite.getMovieId());

            int rowsAffected = st.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar filme aos favoritos: " + e.getMessage(), e);
        }
    }

    /**
     * Remove um filme dos favoritos de um usuário.
     * Retorna true se a remoção foi bem-sucedida, false caso contrário.
     */
    public boolean removeFromFavorites(Favorite favorite) {
        String sql = "DELETE FROM favorite WHERE user_id = ? AND movie_id = ?";

        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setInt(1, favorite.getUserId());
            st.setInt(2, favorite.getMovieId());

            int rowsAffected = st.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover filme dos favoritos: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica se um filme está nos favoritos de um usuário.
     * Retorna true se o filme estiver nos favoritos, false caso contrário.
     */
    public boolean isInFavorites(Favorite favorite) {
        String sql = "SELECT 1 FROM favorite WHERE user_id = ? AND movie_id = ?";

        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setInt(1, favorite.getUserId());
            st.setInt(2, favorite.getMovieId());

            ResultSet rs = st.executeQuery();
            boolean exists = rs.next();
            rs.close();
            return exists;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar favoritos: " + e.getMessage(), e);
        }
    }
}