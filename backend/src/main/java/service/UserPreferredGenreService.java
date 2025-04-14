package service;

import com.google.gson.JsonObject;
import java.util.List;

public class UserPreferredGenreService {

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
    
}
