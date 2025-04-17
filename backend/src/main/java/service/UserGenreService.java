package service;

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

    // Create a method to get the preferred genres of a user
    public List<Genre> getPreferredGenres(int userId) {
        return userGenreDAO.getPreferredGenres(userId);
    }

    // Validate if genreId is valid
    public boolean isValidGenreId(int genreId) {
        return userGenreDAO.genreExists(genreId);
    }

    // Create a method to add a preferred genre for a user
    public boolean addPreferredGenre(int userId, int genreId) {
        // Check if the genreId is valid
        if (!isValidGenreId(genreId)) {
            System.err.println("Invalid genre ID: " + genreId);
            return false;
        }
        // Check if the userId is valid
        if (userId <= 0) {
            System.err.println("Invalid user ID: " + userId);
            return false;
        }
        // Check if the genre already exists for the user
        if (userGenreDAO.getPreferredGenres(userId).stream().anyMatch(g -> g.getId() == genreId)) {
            System.err.println("Genre already exists for user " + userId);
            return false;
        }
        // Get the genre object from the genreId
        Genre genre = userGenreDAO.getGenreById(genreId);
        if (genre == null) {
            System.err.println("Genre not found for ID: " + genreId);
            return false;
        }
        // Insert the new user genre
        UserGenre userGenre = new UserGenre(userId, genreId);
        boolean result = userGenreDAO.insert(userGenre);
        if (result) {
            System.out.println("Genre " + genre.getName() + " added for user " + userId);
        } else {
            System.err.println("Failed to add genre " + genre.getName() + " for user " + userId);
        }
        return result;

    }

}
