package service;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import dao.MovieGenreDAO;
import model.Genre;
import model.MovieGenre;

public class MovieGenreService {
    private final MovieGenreDAO movieGenreDAO;

    public MovieGenreService(MovieGenreDAO movieGenreDAO) {
        this.movieGenreDAO = movieGenreDAO;
    }



    /**
     * Registra múltiplos gêneros para um filme
     * 
     * @param movieId  O ID do filme
     * @param genreIds Lista de IDs dos gêneros
     * @return true se todos foram registrados com sucesso, false caso contrário
     */
    public boolean storeMovieGenres(JsonObject movieObj) {
        int movieId = movieObj.get("id").getAsInt();

        // Verifica se o filme já tem os gêneros associados
        if (movieGenreDAO.checkIfMovieHasGenres(movieId)) {
            System.out.println("Filme já tem gêneros associados: " + movieId);
            return true;
        }

        List<Integer> genreIds = new ArrayList<>();
        for (int i = 0; i < movieObj.get("genres").getAsJsonArray().size(); i++) {
            genreIds.add(movieObj.get("genres").getAsJsonArray().get(i).getAsJsonObject().get("id").getAsInt());
        }
        boolean todosComSucesso = true;

        for (int genreId : genreIds) {
            MovieGenre movieGenre = new MovieGenre(movieId, genreId);
            boolean sucesso = movieGenreDAO.insert(movieGenre);
            if (!sucesso) {
                System.err.println("Erro ao registrar gênero " + genreId + " para o filme " + movieId);
                todosComSucesso = false;
            }
        }

        return todosComSucesso;
    }

    /**
     * Obtém os IDs dos gêneros associados a um filme
     * 
     * @param movieId O ID do filme
     * @return Lista de IDs dos gêneros associados ao filme
     */
    public List<Integer> getGenreIdsForMovie(int movieId) {
        List<Integer> genreIds = new ArrayList<>();
        ArrayList<Genre> genres = movieGenreDAO.getGenresByMovieId(movieId);
        for (Genre genre : genres) {
            genreIds.add(genre.getId());
        }

        return genreIds;
    }

    /**
     * Remove todos os gêneros associados a um filme
     * 
     * @param movieId O ID do filme
     * @return true se a remoção foi bem-sucedida, false caso contrário
     */
    public boolean removerGenerosDoFilme(int movieId) {
        return movieGenreDAO.deleteByMovieId(movieId);
    }

    /**
     * Busca todos os gêneros associados a um filme
     * 
     * @param movieId O ID do filme
     * @return Lista de IDs dos gêneros associados ao filme
     */
    public ArrayList<Genre> buscarGenerosDoFilme(int movieId) {
        return movieGenreDAO.getGenresByMovieId(movieId);
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
            boolean status = movieGenreDAO.insert(movieGenre);
            if (!status) {
                allSuccessful = false;
            }
        }
        
        return allSuccessful;
    }

}