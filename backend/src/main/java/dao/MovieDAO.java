package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.Movie;

public class MovieDAO extends DAO {

    public MovieDAO(String serverName, String mydatabase, int porta, String username, String password) {
        super();
        conectar(serverName, mydatabase, porta, username, password);
    }

    public void finalize() {
        close();
    }

    /**
     * Insere um novo usuário no banco de dados
     * 
     * @param user O usuário a ser inserido
     * @return true se a inserção foi bem-sucedida, false caso contrário
     */
    public boolean insert(Movie movie) {
        System.out.println("Inserting movie: " + movie.toString());
        boolean status = false;
        try {
            String sql = "INSERT INTO movies (id, title, release_date, original_language, popularity, adult) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, movie.getId());
            st.setString(2, movie.getTitle());
            st.setString(3, movie.getReleaseDate());
            st.setString(4, movie.getOriginalLanguage());
            st.setDouble(5, movie.getPopularity());
            st.setBoolean(6, movie.getAdult());
            
            int rowsAffected = st.executeUpdate();
            status = rowsAffected > 0;
            
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir filme: " + e.getMessage(), e);
        }
        return status;
    }

    /**
     * Verifica se o filme já existe no banco de dados
     * 
     * @param movieId O ID do filme a ser verificado
     * @return true se o filme existe, false caso contrário
     */
    public boolean exists(int movieId) throws SQLException {
        String sql = "SELECT 1 FROM movies WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Retorna true se o filme for encontrado
            }
        }
    }

    /**
     * Busca um filme pelo seu ID
     * 
     * @param movieId O ID do filme a ser buscado
     * @return O filme encontrado ou null se não encontrado
     */
    public Movie getMovieById(int movieId) {
        Movie movie = null;
        try {
            String sql = "SELECT * FROM movies WHERE id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, movieId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                movie = new Movie();
                movie.setId(rs.getInt("id"));
                movie.setTitle(rs.getString("title"));
                movie.setReleaseDate(rs.getString("release_date"));
                movie.setOriginalLanguage(rs.getString("original_language"));
                movie.setPopularity(rs.getDouble("popularity"));
                movie.setAdult(rs.getBoolean("adult"));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar filme: " + e.getMessage(), e);
        }
        return movie;
    }

}