package model;

public class Movie {
    private int id;
    private String title;
    private String overview;
    private double rating;
    private String releaseDate;
    private String originalLanguage;
    private double popularity;
    private String posterPath;
    private String backdropPath;
    
    public Movie() {
        this.id = 0;
        this.title = "";
        this.overview = "";
        this.rating = 0.0;
        this.releaseDate = "";
        this.originalLanguage = "";
        this.popularity = 0.0;
        this.posterPath = "";
        this.backdropPath = "";
    }
    
    public Movie(int id, String title, String overview, double rating, String releaseDate, String originalLanguage, double popularity, String posterPath, String backdropPath) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.originalLanguage = originalLanguage;
        this.popularity = popularity;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
    
    public String getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
    
    public String getOriginalLanguage() {
        return originalLanguage;
    }
    
    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }
    
    public double getPopularity() {
        return popularity;
    }
    
    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }
    
    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", overview='" + overview + '\'' +
                ", rating=" + rating +
                ", releaseDate='" + releaseDate + '\'' +
                ", originalLanguage='" + originalLanguage + '\'' +
                ", popularity=" + popularity +
                ", poster_path='" + posterPath + '\'' +
                ", backdrop_path='" + backdropPath + '\'' +
                '}';
    }

}