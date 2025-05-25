package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
            String sql = "INSERT INTO movies (id, title, overview, rating, release_date, original_language, popularity, adult, poster_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, movie.getId());
            st.setString(2, movie.getTitle());
            st.setString(3, movie.getOverview());
            st.setDouble(4, movie.getRating());
            st.setString(5, movie.getReleaseDate());
            st.setString(6, movie.getOriginalLanguage());
            st.setDouble(7, movie.getPopularity());
            st.setBoolean(8, movie.isAdult());
            st.setString(9, movie.getPosterPath());
            
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
    public boolean exists(int movieId) {
        String sql = "SELECT 1 FROM movies WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Retorna true se o filme for encontrado
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar se o filme existe: " + e.getMessage(), e);
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
                movie.setOverview(rs.getString("overview"));
                movie.setRating(rs.getDouble("rating"));
                movie.setReleaseDate(rs.getString("release_date"));
                movie.setOriginalLanguage(rs.getString("original_language"));
                movie.setPopularity(rs.getDouble("popularity"));
                movie.setAdult(rs.getBoolean("adult"));
                movie.setPosterPath(rs.getString("poster_path"));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar filme: " + e.getMessage(), e);
        }
        return movie;
    }

    /**
     * Atualiza um filme existente no banco de dados
     * 
     * @param movie O filme com os dados atualizados
     * @return true se a atualização foi bem-sucedida, false caso contrário
     */
    public boolean update(Movie movie) {
        boolean status = false;
        try {
            String sql = "UPDATE movies SET title = ?, release_date = ?, original_language = ?, popularity = ?, adult = ? WHERE id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setString(1, movie.getTitle());
            st.setString(2, movie.getReleaseDate());
            st.setString(3, movie.getOriginalLanguage());
            st.setDouble(4, movie.getPopularity());
            st.setBoolean(5, movie.isAdult());
            st.setInt(6, movie.getId());
            
            int rowsAffected = st.executeUpdate();
            status = rowsAffected > 0;
            
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar filme: " + e.getMessage(), e);
        }
        return status;
    }

    public ArrayList<Integer> getAllMoviesIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        try {
            String sql = "SELECT id FROM movies";
            PreparedStatement st = conexao.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar IDs dos filmes: " + e.getMessage(), e);
        }
        return ids;
    }

    public ArrayList<Movie> search(String query, int page, int limit) {
        ArrayList<Movie> movies = new ArrayList<>();

        String sql = "SELECT id, title, poster_path, release_date, popularity FROM movies " +
                    "WHERE LOWER(title) LIKE ? AND adult = false " +
                    "ORDER BY popularity DESC " +
                    "LIMIT ? OFFSET ?";

        try {
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setString(1, "%" + query.toLowerCase() + "%");
            st.setInt(2, limit);
            st.setInt(3, (page - 1) * limit);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Movie movie = new Movie();
                movie.setId(rs.getInt("id"));
                movie.setTitle(rs.getString("title"));
                movie.setPosterPath(rs.getString("poster_path"));
                movie.setReleaseDate(rs.getString("release_date"));
                movie.setPopularity(rs.getDouble("popularity")); // Corrigido aqui
                movies.add(movie);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar filmes: " + e.getMessage(), e);
        }

        System.out.println("Filmes encontrados: " + movies.size());
        for (Movie movie : movies) {
            System.out.println(movie.toString());
        }

        return movies;
    }

}