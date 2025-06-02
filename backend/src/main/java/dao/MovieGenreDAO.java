package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.Genre;
import model.MovieGenre;

public class MovieGenreDAO extends DAO {

    public MovieGenreDAO(String serverName, String mydatabase, int porta, String username, String password) {
        super();
        conectar(serverName, mydatabase, porta, username, password);
    }

    public void finalize() {
        close();
    }

    /**
     * Insere uma associação entre filme e gênero
     * 
     * @param movieGenre O objeto representando a associação
     * @return true se a inserção foi bem-sucedida, false caso contrário
     */
    public boolean insert(MovieGenre movieGenre) {
        boolean status = false;
        try {
            String sql = "INSERT INTO movie_genres (movie_id, genre_id) VALUES (?, ?)";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, movieGenre.getMovieId());
            st.setInt(2, movieGenre.getGenreId());

            int rowsAffected = st.executeUpdate();
            status = rowsAffected > 0;

            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao inserir relação filme-gênero: " + e.getMessage());
        }
        return status;
    }

    /**
     * Remove todas as associações de gênero para um filme específico
     * 
     * @param movieId O ID do filme
     * @return true se a remoção foi bem-sucedida, false caso contrário
     */
    public boolean deleteByMovieId(int movieId) {
        boolean status = false;
        try {
            String sql = "DELETE FROM movie_genres WHERE movie_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, movieId);

            int rowsAffected = st.executeUpdate();
            status = rowsAffected > 0;

            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao remover relações filme-gênero: " + e.getMessage());
        }
        return status;
    }

    /**
     * Remove uma associação entre filme e gênero
     * 
     * @param movieGenre O objeto representando a associação
     * @return true se a remoção foi bem-sucedida, false caso contrário
     */
    public boolean delete(MovieGenre movieGenre) {
        boolean status = false;
        try {
            String sql = "DELETE FROM movie_genres WHERE movie_id = ? AND genre_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, movieGenre.getMovieId());
            st.setInt(2, movieGenre.getGenreId());

            int rowsAffected = st.executeUpdate();
            status = rowsAffected > 0;

            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao remover relação filme-gênero: " + e.getMessage());
        }
        return status;
    }

    /**
     * Busca todos os gêneros associados a um filme específico
     * 
     * @param movieId O ID do filme
     * @return Uma lista de objetos Genre representando os gêneros associados ao
     *         filme
     */
    public ArrayList<Genre> getGenresByMovieId(int movieId) {
        ArrayList<Genre> genres = new ArrayList<>();
        try {
            String sql = "SELECT g.id, g.name FROM movie_genres mg " +
                    "JOIN genres g ON mg.genre_id = g.id " +
                    "WHERE mg.movie_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, movieId);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int genreId = rs.getInt("id");
                String name = rs.getString("name");
                genres.add(new Genre(genreId, name));
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar gêneros por ID de filme: " + e.getMessage());
        }
        return genres;
    }

    /**
     * Verifica se um filme já possui gêneros associados
     * 
     * @param movieId O ID do filme
     * @return true se o filme já tem gêneros associados, false caso contrário
     */
    public boolean checkIfMovieHasGenres(int movieId) {
        boolean hasGenres = false;
        try {
            String sql = "SELECT COUNT(*) FROM movie_genres WHERE movie_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, movieId);

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                hasGenres = rs.getInt(1) > 0;
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao verificar se o filme tem gêneros: " + e.getMessage());
        }
        return hasGenres;
    }

}