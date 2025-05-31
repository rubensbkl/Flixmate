package service;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import dao.MovieDAO;
import model.Movie;

public class MovieService {
    private MovieDAO movieDAO;
    private MovieGenreService movieGenreService;
    private TMDBService tmdbService;
    
    // Construtor com dependências
    public MovieService(MovieDAO movieDAO, MovieGenreService movieGenreService, TMDBService tmdbService) {
        this.movieDAO = movieDAO;
        this.movieGenreService = movieGenreService;
        this.tmdbService = tmdbService;
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
            String originalLanguage = movieObj.has("original_language") ? movieObj.get("original_language").getAsString() : null;
            double popularity = movieObj.has("popularity") ? movieObj.get("popularity").getAsDouble() : 0.0;
            String posterPath = movieObj.has("poster_path") ? movieObj.get("poster_path").getAsString() : null;
            String backdropPath = movieObj.has("backdrop_path") ? movieObj.get("backdrop_path").getAsString() : null;

            if (movieDAO.exists(movieId)) {
                System.out.println("Filme já existe no banco: " + movieId + " - " + title);
                return true;
            }

            Movie movie = new Movie(movieId, title, overview, rating, releaseDate, originalLanguage, popularity, posterPath, backdropPath);
            if (!movieDAO.insert(movie)) {
                System.err.println("Falha ao inserir filme no banco: " + movieId + " - " + title);
                return false;
            }
            
            System.out.println("Filme armazenado com sucesso: " + movieId + " - " + title);
            return true;
        
        } catch (Exception e) {
            System.err.println("Erro ao processar filme: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
        return tmdbService.getMovieDetails(movieId);
    }

    public ArrayList<Integer> getAllMoviesIds() {
        return movieDAO.getAllMoviesIds();
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

    public ArrayList<Movie> search(String query, int page, int limit) throws Exception {
        return movieDAO.search(query, page, limit);
    }

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