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
}