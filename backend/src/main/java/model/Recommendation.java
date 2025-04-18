package model;

public class Recommendation {
    private int userId;
    private int movieId;
    private boolean watched;
    private boolean favorite;

    public Recommendation() {
        this.userId = -1;
        this.movieId = -1;
        this.watched = false;
        this.favorite = false;
    }

    public Recommendation(int userId, int movieId) {
        this.userId = userId;
        this.movieId = movieId;
        this.watched = false;
        this.favorite = false;
    }

    public int getUserId() {
        return userId;
    }

    public int setUserId(int userId) {
        this.userId = userId;
        return userId;
    }

    public int getMovieId() {
        return movieId;
    }

    public int setMovieId(int movieId) {
        this.movieId = movieId;
        return movieId;
    }

    public boolean isWatched() {
        return watched;
    }
    
    public void setWatched(boolean watched) {
        this.watched = watched;
    }
    
    public boolean isFavorite() {
        return favorite;
    }
    
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public String toString() {
        return "Recommendation{" +
                "userId=" + userId +
                ", movieId=" + movieId +
                ", watched=" + watched +
                ", favorite=" + favorite +
                '}';
    }

}
