package service;

import java.util.ArrayList;

import com.google.gson.JsonObject;

import dao.MovieDAO;
import model.Movie;
import util.TMDBUtil;

public class MovieService {
    private MovieDAO movieDAO;
    private MovieGenreService movieGenreService;
    private TMDBUtil tmdbUtil;

    // Construtor com dependências
    public MovieService(MovieDAO movieDAO, MovieGenreService movieGenreService, TMDBUtil tmdbUtil) {
        this.movieDAO = movieDAO;
        this.movieGenreService = movieGenreService;
        this.tmdbUtil = tmdbUtil;
    }

    /**
     * Busca um filme do TMDB e o salva no banco de dados junto com seus gêneros
     * 
     * @param movieId O ID do filme no TMDB
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    public boolean storeMovie(JsonObject movieObj) {
        try {
            int movieId = movieObj.get("id").getAsInt();
            String title = movieObj.get("title").getAsString();
            String overview = movieObj.has("overview") ? movieObj.get("overview").getAsString() : null;
            double rating = movieObj.has("vote_average") ? movieObj.get("vote_average").getAsDouble() : 0.0;
            String releaseDate = movieObj.has("release_date") ? movieObj.get("release_date").getAsString() : null;
            String originalLanguage = movieObj.has("original_language")
                    ? movieObj.get("original_language").getAsString()
                    : null;
            double popularity = movieObj.has("popularity") ? movieObj.get("popularity").getAsDouble() : 0.0;
            String posterPath = movieObj.has("poster_path") ? movieObj.get("poster_path").getAsString() : null;
            String backdropPath = movieObj.has("backdrop_path") ? movieObj.get("backdrop_path").getAsString() : null;

            if (movieDAO.exists(movieId)) {
                System.out.println("Filme já existe no banco: " + movieId + " - " + title);
                return true;
            }

            Movie movie = new Movie(movieId, title, overview, rating, releaseDate, originalLanguage, popularity,
                    posterPath, backdropPath);
            if (!movieDAO.insert(movie)) {
                System.err.println("Falha ao inserir filme no banco: " + movieId + " - " + title);
                return false;
            }

            System.out.println("[🎬:🟢] MOVIE CREATE SUCCESS: [movieId: " + movieId + ", title: " + title + "]");
            return true;

        } catch (Exception e) {
            System.err.println("Erro ao processar filme: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca filmes com filtros avançados
     */
    public ArrayList<Movie> searchWithFilters(String query, int page, int limit, String sortBy, String genresParam,
            String yearFrom, String yearTo) throws Exception {
        return movieDAO.searchWithFilters(query, page, limit, sortBy, genresParam, yearFrom, yearTo);
    }

    /**
     * Conta resultados com filtros avançados
     */
    public int countSearchResultsWithFilters(String query, String sortBy, String genresParam, String yearFrom,
            String yearTo) {
        return movieDAO.countSearchResultsWithFilters(query, sortBy, genresParam, yearFrom, yearTo);
    }

    /**
     * Busca um filme pelo ID
     * 
     * @param movieId O ID do filme
     * @return O filme encontrado ou null se não encontrado
     */
    public Movie getMovieById(int movieId) {
        return movieDAO.getMovieById(movieId);
    }

    // getMovieDetails
    public JsonObject getMovieDetails(int movieId) {
        return tmdbUtil.getMovieDetails(movieId);
    }

    public ArrayList<Integer> getAllMoviesIds() {
        return movieDAO.getAllMoviesIds();
    }

    public ArrayList<Integer> getUnratedMovieIds(int userId, int limit, int offset) {
        return movieDAO.getUnratedMovieIds(userId, limit, offset);
    }

    /**
     * Verifica se um filme existe no banco de dados
     * 
     * @param movieId O ID do filme a ser verificado
     * @return true se o filme existe, false caso contrário
     */
    public boolean movieExists(int movieId) {
        return movieDAO.exists(movieId);
    }

    /**
     * Busca filmes com base em uma consulta de pesquisa
     * 
     * @param query A consulta de pesquisa
     * @param page  O número da página para paginação
     * @param limit O número máximo de resultados por página
     * @return Uma lista de filmes correspondentes à consulta
     * @throws Exception Se ocorrer um erro durante a busca
     */
    public ArrayList<Movie> search(String query, int page, int limit) throws Exception {
        return movieDAO.search(query, page, limit);
    }

    /**
     * Conta o número de resultados de pesquisa com base em uma consulta
     * 
     * @param query A consulta de pesquisa
     * @return O número de resultados correspondentes à consulta
     */
    public int countSearchResults(String query) {
        return movieDAO.countSearchResults(query);
    }

    /**
     * Busca os filmes mais populares do banco local
     * Ordena por popularidade (campo popularity) em ordem decrescente
     */
    public ArrayList<Movie> getMostPopularMovies(int page, int limit) {
        return movieDAO.getMostPopularMovies(page, limit);
    }

    /**
     * Conta o total de filmes no banco
     */
    public int getTotalMoviesCount() {
        return movieDAO.getTotalMoviesCount();
    }
}