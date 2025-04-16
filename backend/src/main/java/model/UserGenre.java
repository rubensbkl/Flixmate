package model;

public class UserGenre {
    private int userId;
    private int genreId;

    public UserGenre() {
        this.userId = -1;
        this.genreId = -1;
    }

    public UserGenre(int userId, int genreId) {
        this.userId = userId;
        this.genreId = genreId;
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

    @Override
    public String toString() {
        return "UserGenre{userId=" + userId + ", genreId=" + genreId + "}";
    }

}