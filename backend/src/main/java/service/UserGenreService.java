package service;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import dao.UserGenreDAO;
import model.Genre;
import model.UserGenre;

public class UserGenreService {

    private UserGenreDAO userGenreDAO;

    public UserGenreService(UserGenreDAO userGenreDAO) {
        this.userGenreDAO = userGenreDAO;
    }

    /**
     * Calcula a pontuação de um filme com base nos gêneros preferidos do usuário.
     * 
     * @param movie            O objeto JsonObject representando o filme
     * @param preferredGenres  Lista de IDs dos gêneros preferidos do usuário
     * @return A pontuação do filme com base nos gêneros preferidos
     */
    public int calculateGenreScore(JsonObject movie, List<Integer> preferredGenres) {
        if (!movie.has("genre_ids")) {
            return 0;
        }

        var movieGenres = movie.getAsJsonArray("genre_ids");
        int score = 0;

        for (var genreElement : movieGenres) {
            int genreId = genreElement.getAsInt();
            if (preferredGenres.contains(genreId)) {
                score++;
            }
        }
        return score;
    }

    /**
     * Obtém os gêneros preferidos de um usuário.
     * 
     * @param userId O ID do usuário
     * @return Lista de gêneros preferidos do usuário
     */
    public ArrayList<Genre> getPreferredGenres(int userId) {
        return userGenreDAO.getPreferredGenres(userId);
    }

    /**
     * Verifica se um gênero é válido com base no ID.
     * 
     * @param genreId O ID do gênero
     * @return true se o gênero existir, false caso contrário
     */
    public boolean isValidGenreId(int genreId) {
        return userGenreDAO.genreExists(genreId);
    }

    /**
     * Adiciona um gênero preferido para um usuário.
     * 
     * @param userId   O ID do usuário
     * @param genreId  O ID do gênero a ser adicionado
     * @return true se o gênero foi adicionado com sucesso, false caso contrário
     */
    public boolean addPreferredGenre(int userId, int genreId) {
        if (!isValidGenreId(genreId)) {
            System.err.println("Invalid genre ID: " + genreId);
            return false;
        }
        if (userId <= 0) {
            System.err.println("Invalid user ID: " + userId);
            return false;
        }
        if (userGenreDAO.getPreferredGenres(userId).stream().anyMatch(g -> g.getId() == genreId)) {
            System.err.println("Genre already exists for user " + userId);
            return false;
        }
        Genre genre = userGenreDAO.getGenreById(genreId);
        if (genre == null) {
            System.err.println("Genre not found for ID: " + genreId);
            return false;
        }
        UserGenre userGenre = new UserGenre(userId, genreId);
        boolean result = userGenreDAO.insert(userGenre);
        if (result) {
            System.out.println("Genre " + genre.getName() + " added for user " + userId);
        } else {
            System.err.println("Failed to add genre " + genre.getName() + " for user " + userId);
        }
        return result;

    }
    
    /**
     * Remove um gênero preferido de um usuário.
     * 
     * @param userId   O ID do usuário
     * @param genreId  O ID do gênero a ser removido
     * @return true se o gênero foi removido com sucesso, false caso contrário
     */
    public boolean removeAllPreferredGenres(int userId) {
        return userGenreDAO.removeAllByUserId(userId);
    }

}
