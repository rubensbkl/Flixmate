package model;
public class Movie {
    private int id;
    private String title;
    private String overview;
    private double rating;
    private String releaseDate;
    private String originalLanguage;
    private double popularity;
    private boolean adult;
    private String poster_path;
    
    public Movie() {
        this.id = 0;
        this.title = "";
        this.overview = "";
        this.rating = 0.0;
        this.releaseDate = "";
        this.originalLanguage = "";
        this.popularity = 0.0;
        this.adult = false;
        this.poster_path = "";
    }

    public Movie(int id, String title, String releaseDate, String originalLanguage, double popularity, boolean adult) {
        this.id = id;
        this.title = title;
        this.overview = "";
        this.rating = 0.0;
        this.releaseDate = releaseDate;
        this.originalLanguage = originalLanguage;
        this.popularity = popularity;
        this.adult = adult;
        this.poster_path = "";
    }
    
    public Movie(int id, String title, String overview, double rating, String releaseDate, String originalLanguage, double popularity, boolean adult, String poster_path) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.originalLanguage = originalLanguage;
        this.popularity = popularity;
        this.adult = adult;
        this.poster_path = poster_path;
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
    
    public boolean isAdult() {
        return adult;
    }
    
    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public String getPosterPath() {
        return poster_path;
    }

    public void setPosterPath(String poster_path) {
        this.poster_path = poster_path;
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
                ", adult=" + adult +
                ", poster_path='" + poster_path + '\'' +
                '}';
    }

}