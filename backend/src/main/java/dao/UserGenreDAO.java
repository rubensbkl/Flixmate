package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Genre;
import model.UserGenre;

public class UserGenreDAO extends DAO {

    public UserGenreDAO(String serverName, String mydatabase, int porta, String username, String password) {
        super();
        conectar(serverName, mydatabase, porta, username, password);
    }

    public void finalize() {
        close();
    }

    /**
     * Adiciona um gênero preferido para um usuário, ignorando duplicatas
     * @param userGenre O objeto de relação entre usuário e gênero
     * @return true se a inserção foi feita (ou já existia), false se houve erro
     */
    public boolean insert(UserGenre userGenre) {
        try {
            String sql = "INSERT INTO user_genres (user_id, genre_id) VALUES (?, ?) " +
                         "ON CONFLICT (user_id, genre_id) DO NOTHING";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userGenre.getUserId());
            st.setInt(2, userGenre.getGenreId());
            
            st.executeUpdate();
            st.close();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir gênero preferido: " + e.getMessage());
            return false;
        }
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
            String sql = "DELETE FROM user_genres WHERE user_id = ? AND genre_id = ?";
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
            String sql = "DELETE FROM user_genres WHERE user_id = ?";
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
     * Obtém todos os gêneros preferidos de um usuário com seus detalhes
     * @param userId ID do usuário
     * @return Lista de objetos Genre com os detalhes dos gêneros preferidos
     */
    public ArrayList<Genre> getPreferredGenres(int userId) {
        ArrayList<Genre> genres = new ArrayList<>();
        try {
            String sql = "SELECT g.id, g.name FROM genres g " +
                         "JOIN user_genres upg ON g.id = upg.genre_id " +
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
     * Obtém um gênero pelo seu ID
     * @param genreId ID do gênero
     * @return Objeto Genre com os detalhes do gênero
     */
    public Genre getGenreById(int genreId) {
        Genre genre = null;
        try {
            String sql = "SELECT * FROM genres WHERE id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, genreId);
            
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                genre = new Genre(
                    rs.getInt("id"),
                    rs.getString("name")
                );
            }
            
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar gênero por ID: " + e.getMessage());
        }
        return genre;
    }

    /**
     * Verifica se o gênero existe na tabela de gêneros
     * @param genreId ID do gênero
     * @return true se o gênero existe, false caso contrário
     */
    public boolean genreExists(int genreId) {
        boolean exists = false;
        try {
            String sql = "SELECT COUNT(*) FROM genres WHERE id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, genreId);
            
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                exists = (rs.getInt(1) > 0);
            }
            
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao verificar existência do gênero: " + e.getMessage());
        }
        return exists;
    }

    


}