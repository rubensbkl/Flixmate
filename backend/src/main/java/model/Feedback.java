package model;

public class Feedback {
    private int userId;
    private int movieId;
    private Boolean feedback;
    private String movieTitle;
    private String movieGenres;
    private String releaseDate;
    private double popularity;
    private boolean adult;
    private String originalLanguage;

    public Feedback() {
        this.userId = -1;
        this.movieId = -1;
        this.feedback = false;
        this.movieTitle = "";
        this.movieGenres = "";
        this.releaseDate = "";
        this.popularity = 0.0;
        this.adult = false;
        this.originalLanguage = "";
    }

    public Feedback(int userId, int movieId, Boolean feedback) {
        this.userId = userId;
        this.movieId = movieId;
        this.feedback = feedback;
        this.movieTitle = "";
        this.movieGenres = "";
        this.releaseDate = "";
        this.popularity = 0.0;
        this.adult = false;
        this.originalLanguage = "";
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

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMovieGenres() {
        return movieGenres;
    }

    public void setMovieGenres(String movieGenres) {
        this.movieGenres = movieGenres;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public boolean getAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "userId=" + userId +
                ", movieId=" + movieId +
                ", feedback=" + feedback +
                ", movieTitle='" + movieTitle + '\'' +
                ", movieGenres='" + movieGenres + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", popularity=" + popularity +
                ", adult=" + adult +
                ", originalLanguage='" + originalLanguage + '\'' +
                '}';
    }
    
}
