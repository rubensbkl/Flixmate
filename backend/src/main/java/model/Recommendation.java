package model;

public class Recommendation {
    private int id;
    private int userId;
    private int movieId;

    public Recommendation() {
        this.id = -1;
        this.userId = -1;
        this.movieId = -1;
    }

    public Recommendation(int userId, int movieId) {
        this.userId = userId;
        this.movieId = movieId;
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
