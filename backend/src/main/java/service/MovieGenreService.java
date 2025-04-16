package service;

import java.util.ArrayList;
import java.util.List;

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
     * @param movieId O ID do filme
     * @param genreIds Lista de IDs dos gêneros
     * @return true se todos foram registrados com sucesso, false caso contrário
     */
    public boolean registrarGenerosDoFilme(int movieId, List<Integer> genreIds) {
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
     * Atualiza os gêneros de um filme
     * 
     * @param movieId O ID do filme
     * @param genreIds Nova lista de IDs dos gêneros
     * @return true se a atualização foi bem-sucedida, false caso contrário
     */
    public boolean atualizarGenerosDoFilme(int movieId, List<Integer> genreIds) {
        try {
            // Primeiro remove as relações existentes
            movieGenreDAO.deleteByMovieId(movieId);
            
            // Depois adiciona as novas
            return registrarGenerosDoFilme(movieId, genreIds);
        } catch (Exception e) {
            System.err.println("Erro ao atualizar gêneros do filme: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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

}