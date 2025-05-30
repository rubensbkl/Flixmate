package model;

public class Recommendation {
    private int userId;
    private int movieId;
    private double score;

    public Recommendation() {
        this.userId = 0;
        this.movieId = 0;
        this.score = 0.0;
    }

    public Recommendation(int userId, int movieId, double score) {
        this.userId = userId;
        this.movieId = movieId;
        this.score = score;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMovieId() {
        return this.movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public double getScore() {
        return this.score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Recommendation{" +
                "userId=" + userId +
                ", movieId=" + movieId +
                ", score=" + score +
                '}';
    }

}
