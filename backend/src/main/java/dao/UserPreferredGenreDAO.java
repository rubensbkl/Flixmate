package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Genre;
import model.UserPreferredGenre;

public class UserPreferredGenreDAO extends DAO {

    public UserPreferredGenreDAO(String serverName, String mydatabase, int porta, String username, String password) {
        super();
        conectar(serverName, mydatabase, porta, username, password);
    }

    public void finalize() {
        close();
    }

    /**
     * Adiciona um gênero preferido para um usuário
     * @param userPreferredGenre O objeto de relação entre usuário e gênero
     * @return true se a adição foi bem-sucedida, false caso contrário
     */
    public boolean insert(UserPreferredGenre userPreferredGenre) {
        boolean status = false;
        try {
            String sql = "INSERT INTO user_preferred_genres (user_id, genre_id) VALUES (?, ?) " +
                         "ON CONFLICT (user_id, genre_id) DO NOTHING RETURNING id";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userPreferredGenre.getUserId());
            st.setInt(2, userPreferredGenre.getGenreId());
            
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                userPreferredGenre.setId(rs.getInt(1));
                status = true;
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao inserir gênero preferido: " + e.getMessage());
        }
        return status;
    }

    /**
     * Remove um gênero preferido de um usuário
     * @param userId ID do usuário
     * @param genreId ID do gênero
     * @return true se a remoção foi bem-sucedida, false caso contrário
     */
    public boolean delete(int userId, int genreId) {
        boolean status = false;
        try {
            String sql = "DELETE FROM user_preferred_genres WHERE user_id = ? AND genre_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            st.setInt(2, genreId);
            
            int affectedRows = st.executeUpdate();
            status = (affectedRows > 0);
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao remover gênero preferido: " + e.getMessage());
        }
        return status;
    }

    /**
     * Remove todos os gêneros preferidos de um usuário
     * @param userId ID do usuário
     * @return true se a remoção foi bem-sucedida, false caso contrário
     */
    public boolean deleteAllForUser(int userId) {
        boolean status = false;
        try {
            String sql = "DELETE FROM user_preferred_genres WHERE user_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            
            st.executeUpdate(); // Mesmo que não exclua nenhum registro, consideramos sucesso
            status = true;
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao remover todos os gêneros preferidos: " + e.getMessage());
        }
        return status;
    }

    /**
     * Verifica se um gênero é preferido pelo usuário
     * @param userId ID do usuário
     * @param genreId ID do gênero
     * @return true se o gênero é preferido pelo usuário, false caso contrário
     */
    public boolean isGenrePreferred(int userId, int genreId) {
        boolean isPreferred = false;
        try {
            String sql = "SELECT 1 FROM user_preferred_genres WHERE user_id = ? AND genre_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            st.setInt(2, genreId);
            
            ResultSet rs = st.executeQuery();
            isPreferred = rs.next();
            
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao verificar se gênero é preferido: " + e.getMessage());
        }
        return isPreferred;
    }

    /**
     * Obtém todos os IDs de gêneros preferidos de um usuário
     * @param userId ID do usuário
     * @return Lista de IDs dos gêneros preferidos
     */
    public List<Integer> getPreferredGenreIds(int userId) {
        List<Integer> genreIds = new ArrayList<>();
        try {
            String sql = "SELECT genre_id FROM user_preferred_genres WHERE user_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                genreIds.add(rs.getInt("genre_id"));
            }
            
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar IDs de gêneros preferidos: " + e.getMessage());
        }
        return genreIds;
    }

    /**
     * Obtém todos os gêneros preferidos de um usuário com seus detalhes
     * @param userId ID do usuário
     * @return Lista de objetos Genre com os detalhes dos gêneros preferidos
     */
    public List<Genre> getPreferredGenres(int userId) {
        List<Genre> genres = new ArrayList<>();
        try {
            String sql = "SELECT g.id, g.name FROM genres g " +
                         "JOIN user_preferred_genres upg ON g.id = upg.genre_id " +
                         "WHERE upg.user_id = ? " +
                         "ORDER BY g.name";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Genre genre = new Genre(
                    rs.getInt("id"),
                    rs.getString("name")
                );
                genres.add(genre);
            }
            
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar gêneros preferidos: " + e.getMessage());
        }
        return genres;
    }

    /**
     * Obtém detalhes completos das relações entre usuário e gêneros preferidos
     * @param userId ID do usuário
     * @return Lista de objetos UserPreferredGenre com os detalhes dos gêneros preferidos
     */
    public List<UserPreferredGenre> getUserPreferredGenresWithDetails(int userId) {
        List<UserPreferredGenre> userPreferredGenres = new ArrayList<>();
        try {
            String sql = "SELECT upg.id, upg.user_id, upg.genre_id, g.name " +
                         "FROM user_preferred_genres upg " +
                         "JOIN genres g ON upg.genre_id = g.id " +
                         "WHERE upg.user_id = ? " +
                         "ORDER BY g.name";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                UserPreferredGenre upg = new UserPreferredGenre(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("genre_id")
                );
                
                // Adiciona o objeto Genre associado
                Genre genre = new Genre(
                    rs.getInt("genre_id"),
                    rs.getString("name")
                );
                upg.setGenre(genre);
                
                userPreferredGenres.add(upg);
            }
            
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar detalhes dos gêneros preferidos: " + e.getMessage());
        }
        return userPreferredGenres;
    }

    /**
     * Conta quantos usuários têm um determinado gênero como preferido
     * @param genreId ID do gênero
     * @return Número de usuários que preferem o gênero
     */
    public int countUsersWithPreferredGenre(int genreId) {
        int count = 0;
        try {
            String sql = "SELECT COUNT(DISTINCT user_id) FROM user_preferred_genres WHERE genre_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, genreId);
            
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao contar usuários com gênero preferido: " + e.getMessage());
        }
        return count;
    }
}