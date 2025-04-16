package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
     * Insere múltiplas associações de gêneros para um filme
     * 
     * @param movieId O ID do filme
     * @param genreIds Lista de IDs de gêneros
     * @return true se todas as inserções foram bem-sucedidas, false caso contrário
     */
    public boolean insertMovieGenres(int movieId, List<Integer> genreIds) {
        boolean allSuccessful = true;
        
        for (Integer genreId : genreIds) {
            MovieGenre movieGenre = new MovieGenre(movieId, genreId);
            boolean status = insert(movieGenre);
            if (!status) {
                allSuccessful = false;
            }
        }
        
        return allSuccessful;
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
     * @return Uma lista de objetos Genre representando os gêneros associados ao filme
     */
    public ArrayList<Genre> getGenresByMovieId(int movieId) {
        ArrayList<Genre> genres = new ArrayList<>();
        try {
            String sql = "SELECT g.id, g.name FROM genres g " +
                         "JOIN movie_genres mg ON g.id = mg.genre_id " +
                         "WHERE mg.movie_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, movieId);
            
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                genres.add(new Genre(id, name));
            }
            
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar gêneros por ID de filme: " + e.getMessage());
        }
        return genres;
    }

    /**
     * Busca todos os filmes associados a um gênero específico
     * 
     * @param genreId O ID do gênero
     * @return Uma lista de objetos MovieGenre representando os filmes associados ao gênero
     */
    // public List<MovieGenre> getMoviesByGenreId(int genreId) {
    //     List<MovieGenre> movies = new ArrayList<>();
    //     try {
    //         String sql = "SELECT mg.movie_id, g.name FROM movie_genres mg " +
    //                      "JOIN genres g ON mg.genre_id = g.id " +
    //                      "WHERE mg.genre_id = ?";
    //         PreparedStatement st = conexao.prepareStatement(sql);
    //         st.setInt(1, genreId);
            
    //         ResultSet rs = st.executeQuery();
    //         while (rs.next()) {
    //             int movieId = rs.getInt("movie_id");
    //             String name = rs.getString("name");
    //             movies.add(new MovieGenre(movieId, genreId, name));
    //         }
            
    //         rs.close();
    //         st.close();
    //     } catch (SQLException e) {
    //         System.err.println("Erro ao buscar filmes por ID de gênero: " + e.getMessage());
    //     }
    //     return movies;
    // }
    /**
     * Busca todos os gêneros associados a um filme específico
     * 
     * @param movieId O ID do filme
     * @return Uma lista de objetos MovieGenre representando os gêneros associados ao filme
     */
    // public List<MovieGenre> getMovieGenresByMovieId(int movieId) {
    //     List<MovieGenre> movieGenres = new ArrayList<>();
    //     try {
    //         String sql = "SELECT mg.movie_id, mg.genre_id, g.name FROM movie_genres mg " +
    //                      "JOIN genres g ON mg.genre_id = g.id " +
    //                      "WHERE mg.movie_id = ?";
    //         PreparedStatement st = conexao.prepareStatement(sql);
    //         st.setInt(1, movieId);
            
    //         ResultSet rs = st.executeQuery();
    //         while (rs.next()) {
    //             int genreId = rs.getInt("genre_id");
    //             String name = rs.getString("name");
    //             movieGenres.add(new MovieGenre(movieId, genreId, name));
    //         }
            
    //         rs.close();
    //         st.close();
    //     } catch (SQLException e) {
    //         System.err.println("Erro ao buscar gêneros por ID de filme: " + e.getMessage());
    //     }
    //     return movieGenres;
    // }


}