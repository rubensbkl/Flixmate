package model;

public class UserPreferredGenre {
    private int id;
    private int userId;
    private int genreId;
    
    // Objeto Genre associado (opcional, para uso em consultas)
    private Genre genre;

    public UserPreferredGenre() {
        this.id = -1;
        this.userId = -1;
        this.genreId = -1;
        this.genre = null;
    }

    public UserPreferredGenre(int userId, int genreId) {
        this.userId = userId;
        this.genreId = genreId;
        this.genre = null;
    }

    public UserPreferredGenre(int id, int userId, int genreId) {
        this.id = id;
        this.userId = userId;
        this.genreId = genreId;
        this.genre = null;
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

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
        if (genre != null) {
            this.genreId = genre.getId();
        }
    }

    @Override
    public String toString() {
        return "UserPreferredGenre [id=" + id + ", userId=" + userId + ", genreId=" + genreId + "]";
    }
}