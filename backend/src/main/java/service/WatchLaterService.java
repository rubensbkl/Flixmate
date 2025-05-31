package service;

import dao.FavoriteDAO;
import dao.WatchLaterDAO;   
import java.util.ArrayList;

/**
 * Service class for managing the "Watch Later" functionality.
 * This class interacts with the WatchLaterDAO to retrieve movie IDs
 * that a user has marked to watch later.
 */
public class WatchLaterService {
    private WatchLaterDAO watchLaterDAO;
    
    public WatchLaterService(WatchLaterDAO watchLaterDAO) {
        this.watchLaterDAO = watchLaterDAO;
    }
    
    public ArrayList<Integer> getWatchLaterMovies(int userId) {
        return watchLaterDAO.getWatchLaterMovieIds(userId);
    }
}