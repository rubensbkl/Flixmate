package model;

public class Feedback {
    private int userId;
    private int movieId;
    private Boolean feedback;

    public Feedback() {
        this.userId = -1;
        this.movieId = -1;
        this.feedback = false;
    }

    public Feedback(int userId, int movieId, Boolean feedback) {
        this.userId = userId;
        this.movieId = movieId;
        this.feedback = feedback;
    }

    // Getters e Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public Boolean getFeedback() {
        return feedback;
    }

    public void setFeedback(boolean feedback) {
        this.feedback = feedback;
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "userId=" + userId +
                ", movieId=" + movieId +
                ", feedback=" + feedback +
                '}';
    }
    
}
