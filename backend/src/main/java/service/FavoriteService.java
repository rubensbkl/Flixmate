package service;

import dao.FavoriteDAO;
import java.util.ArrayList;


public class FavoriteService {
    private FavoriteDAO favoriteDAO;
    
    public FavoriteService(FavoriteDAO favoriteDAO) {
        this.favoriteDAO = favoriteDAO;
    }
    
    public ArrayList<Integer> getFavoriteMovies(int userId) {
        return favoriteDAO.getFavoriteMovieIds(userId);
    }

    public boolean toggleFavorite(int userId, int movieId, boolean favorite) {
    if (favorite) {
        return favoriteDAO.addToFavorites(userId, movieId);
    } else {
        return favoriteDAO.removeFromFavorites(userId, movieId);
    }
}

public boolean isInFavorites(int userId, int movieId) {
    return favoriteDAO.isInFavorites(userId, movieId);
}
}