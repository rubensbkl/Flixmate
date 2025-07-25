package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
        boolean status = false;
        try {
            String sql = "INSERT INTO movies (id, title, overview, rating, release_date, original_language, popularity, poster_path, backdrop_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, movie.getId());
            st.setString(2, movie.getTitle());
            st.setString(3, movie.getOverview());
            st.setDouble(4, movie.getRating());
            st.setString(5, movie.getReleaseDate());
            st.setString(6, movie.getOriginalLanguage());
            st.setDouble(7, movie.getPopularity());
            st.setString(8, movie.getPosterPath());
            st.setString(9, movie.getBackdropPath());

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
                movie.setPosterPath(rs.getString("poster_path"));
                movie.setBackdropPath(rs.getString("backdrop_path"));
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
            String sql = "UPDATE movies SET title = ?, release_date = ?, original_language = ?, popularity = ? WHERE id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setString(1, movie.getTitle());
            st.setString(2, movie.getReleaseDate());
            st.setString(3, movie.getOriginalLanguage());
            st.setDouble(4, movie.getPopularity());
            st.setInt(5, movie.getId());

            int rowsAffected = st.executeUpdate();
            status = rowsAffected > 0;

            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar filme: " + e.getMessage(), e);
        }
        return status;
    }

    /**
     * Remove um filme do banco de dados
     * 
     * @param movieId O ID do filme a ser removido
     * @return true se a remoção foi bem-sucedida, false caso contrário
     */
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

    /**
     * Busca filmes com base em uma consulta de pesquisa.
     * 
     * @param query A consulta de pesquisa
     * @param page  A página atual (começa em 1)
     * @param limit O número de filmes por página
     * @return Uma lista de filmes que correspondem à consulta
     */
    public ArrayList<Movie> search(String query, int page, int limit) {
        ArrayList<Movie> movies = new ArrayList<>();

        String sql = "SELECT id, title, poster_path, release_date, popularity FROM movies " +
                "WHERE LOWER(title) LIKE ? " +
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
                movie.setReleaseDate(rs.getString("release_date"));
                movie.setPopularity(rs.getDouble("popularity"));
                movie.setPosterPath(rs.getString("poster_path"));
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

    /**
     * Conta o número total de resultados de busca para uma consulta específica
     * 
     * @param query A consulta de pesquisa
     * @return O número total de filmes que correspondem à consulta
     */
    public int countSearchResults(String query) {
        int total = 0;

        String sql = "SELECT COUNT(*) AS total FROM movies " +
                "WHERE LOWER(title) LIKE ?";

        try {
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setString(1, "%" + query.toLowerCase() + "%");

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                total = rs.getInt("total");
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar filmes: " + e.getMessage(), e);
        }

        System.out.println("Total de filmes encontrados: " + total);
        return total;
    }

    /**
     * Busca os filmes mais populares ordenados por popularidade
     * 
     * @param page  Página atual (começa em 1)
     * @param limit Número de filmes por página
     * @return Lista de filmes mais populares
     */
    public ArrayList<Movie> getMostPopularMovies(int page, int limit) {
        ArrayList<Movie> movies = new ArrayList<>();

        String sql = "SELECT id, title, poster_path, release_date, popularity FROM movies " +
                "ORDER BY popularity DESC " +
                "LIMIT ? OFFSET ?";

        try {
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, limit);
            st.setInt(2, (page - 1) * limit);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Movie movie = new Movie();
                movie.setId(rs.getInt("id"));
                movie.setTitle(rs.getString("title"));
                movie.setReleaseDate(rs.getString("release_date"));
                movie.setPopularity(rs.getDouble("popularity"));
                movie.setPosterPath(rs.getString("poster_path"));
                movies.add(movie);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar filmes populares: " + e.getMessage(), e);
        }

        return movies;
    }

    /**
     * Conta o total de filmes no banco de dados
     * 
     * @return Número total de filmes
     */
    public int getTotalMoviesCount() {
        int total = 0;

        String sql = "SELECT COUNT(*) AS total FROM movies";

        try {
            PreparedStatement st = conexao.prepareStatement(sql);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                total = rs.getInt("total");
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar filmes: " + e.getMessage(), e);
        }

        return total;
    }

    /**
     * Busca filmes com filtros avançados, incluindo gêneros, ano e ordenação.
     * 
     * @param query       A consulta de pesquisa
     * @param page        A página atual (começa em 1)
     * @param limit       O número de filmes por página
     * @param sortBy      O critério de ordenação (rating, release_date_desc, etc.)
     * @param genresParam Os IDs dos gêneros filtrados, separados por vírgula
     * @param yearFrom    O ano inicial do filtro
     * @param yearTo      O ano final do filtro
     * @return Uma lista de filmes que correspondem aos filtros
     */
    public ArrayList<Movie> searchWithFilters(String query, int page, int limit, String sortBy, String genresParam,
            String yearFrom, String yearTo) {
        ArrayList<Movie> movies = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(
                "SELECT DISTINCT m.id, m.title, m.poster_path, m.release_date, m.popularity, m.rating FROM movies m ");

        if (genresParam != null && !genresParam.trim().isEmpty()) {
            sql.append("INNER JOIN movie_genres mg ON m.id = mg.movie_id ");
        }

        sql.append("WHERE 1=1 ");

        ArrayList<Object> params = new ArrayList<>();
        int paramIndex = 1;

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND LOWER(m.title) LIKE ? ");
            params.add("%" + query.toLowerCase() + "%");
        }

        if (genresParam != null && !genresParam.trim().isEmpty()) {
            String[] genreIds = genresParam.split(",");
            sql.append("AND mg.genre_id IN (");
            for (int i = 0; i < genreIds.length; i++) {
                sql.append("?");
                if (i < genreIds.length - 1)
                    sql.append(",");
                params.add(Integer.parseInt(genreIds[i].trim()));
            }
            sql.append(") ");
        }

        if (yearFrom != null && !yearFrom.trim().isEmpty()) {
            sql.append("AND EXTRACT(YEAR FROM m.release_date::date) >= ? ");
            params.add(Integer.parseInt(yearFrom));
        }

        if (yearTo != null && !yearTo.trim().isEmpty()) {
            sql.append("AND EXTRACT(YEAR FROM m.release_date::date) <= ? ");
            params.add(Integer.parseInt(yearTo));
        }

        sql.append("ORDER BY ");
        switch (sortBy) {
            case "rating":
                sql.append("m.rating DESC ");
                break;
            case "release_date_desc":
                sql.append("m.release_date DESC ");
                break;
            case "release_date_asc":
                sql.append("m.release_date ASC ");
                break;
            case "title":
                sql.append("m.title ASC ");
                break;
            case "popularity":
            default:
                sql.append("m.popularity DESC ");
                break;
        }

        sql.append("LIMIT ? OFFSET ?");
        params.add(limit);
        params.add((page - 1) * limit);

        try {
            PreparedStatement st = conexao.prepareStatement(sql.toString());

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    st.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    st.setInt(i + 1, (Integer) param);
                }
            }

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Movie movie = new Movie();
                movie.setId(rs.getInt("id"));
                movie.setTitle(rs.getString("title"));
                movie.setReleaseDate(rs.getString("release_date"));
                movie.setPopularity(rs.getDouble("popularity"));
                movie.setPosterPath(rs.getString("poster_path"));
                if (sortBy.equals("rating")) {
                    movie.setRating(rs.getDouble("rating"));
                }
                movies.add(movie);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar filmes com filtros: " + e.getMessage(), e);
        }

        System.out.println("Filmes encontrados com filtros: " + movies.size());
        return movies;
    }

    /**
     * Conta o número total de resultados de busca com filtros avançados.
     * 
     * @param query       A consulta de pesquisa
     * @param sortBy      O critério de ordenação
     * @param genresParam Os IDs dos gêneros filtrados, separados por vírgula
     * @param yearFrom    O ano inicial do filtro
     * @param yearTo      O ano final do filtro
     * @return O número total de filmes que correspondem aos filtros
     */
    public int countSearchResultsWithFilters(String query, String sortBy, String genresParam, String yearFrom,
            String yearTo) {
        int total = 0;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT m.id) AS total FROM movies m ");

        if (genresParam != null && !genresParam.trim().isEmpty()) {
            sql.append("INNER JOIN movie_genres mg ON m.id = mg.movie_id ");
        }

        sql.append("WHERE 1=1 ");

        ArrayList<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND LOWER(m.title) LIKE ? ");
            params.add("%" + query.toLowerCase() + "%");
        }

        if (genresParam != null && !genresParam.trim().isEmpty()) {
            String[] genreIds = genresParam.split(",");
            sql.append("AND mg.genre_id IN (");
            for (int i = 0; i < genreIds.length; i++) {
                sql.append("?");
                if (i < genreIds.length - 1)
                    sql.append(",");
                params.add(Integer.parseInt(genreIds[i].trim()));
            }
            sql.append(") ");
        }

        if (yearFrom != null && !yearFrom.trim().isEmpty()) {
            sql.append("AND EXTRACT(YEAR FROM m.release_date::date) >= ? ");
            params.add(Integer.parseInt(yearFrom));
        }

        if (yearTo != null && !yearTo.trim().isEmpty()) {
            sql.append("AND EXTRACT(YEAR FROM m.release_date::date) <= ? ");
            params.add(Integer.parseInt(yearTo));
        }

        try {
            PreparedStatement st = conexao.prepareStatement(sql.toString());

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    st.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    st.setInt(i + 1, (Integer) param);
                }
            }

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                total = rs.getInt("total");
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar filmes com filtros: " + e.getMessage(), e);
        }

        System.out.println("Total de filmes encontrados com filtros: " + total);
        return total;
    }

}