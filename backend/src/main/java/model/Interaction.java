package model;

public class Interaction {
    private int id;
    private int userId;
    private int movieId;
    private boolean interaction;

    public Interaction() {}

    public Interaction(int userId, int movieId, boolean interaction) {
        this.userId = userId;
        this.movieId = movieId;
        this.interaction = interaction;
    }

    // Getters e Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public Boolean getInteraction() {
        return interaction;
    }

    public void setInteraction(boolean interaction) {
        this.interaction = interaction;
    }
}
