package model;

public class MovieGenre {
    private int movieId;
    private int genreId;

    public MovieGenre() {
        this.movieId = -1;
        this.genreId = -1;
    }

    public MovieGenre(int movieId, int genreId) {
        this.movieId = movieId;
        this.genreId = genreId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    @Override
    public String toString() {
        return "MovieGenre{movieId=" + movieId + ", genreId=" + genreId + "}";
    }

}