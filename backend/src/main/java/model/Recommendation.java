package model;

public class Recommendation {
    private int id;
    private int userId;
    private int movieId;
    private boolean watched;
    private boolean favorite;

    public Recommendation() {
        this.id = -1;
        this.userId = -1;
        this.movieId = -1;
        this.watched = false;
        this.favorite = false;
    }

    public Recommendation(int userId, int movieId, boolean watched, boolean favorite) {
        this.userId = userId;
        this.movieId = movieId;
        this.watched = watched;
        this.favorite = favorite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "Recommendation{" +
                "id=" + id +
                ", userId=" + userId +
                ", movieId=" + movieId +
                '}';
    }

}
