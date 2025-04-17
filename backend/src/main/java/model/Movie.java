package model;

import interfaces.IMovie;
public class Movie implements IMovie {
    private int id;
    private String title;
    private String release_date;
    private String original_language;
    private double popularity;
    private boolean adult;
    
    public Movie() {
        this.id = 0;
        this.title = "";
        this.release_date = "";
        this.original_language = "";
        this.popularity = 0.0;
        this.adult = false;
    }
    
    public Movie(int id, String title, String release_date, String original_language, double popularity, boolean adult) {
        this.id = id;
        this.title = title;
        this.release_date = release_date;
        this.original_language = original_language;
        this.popularity = popularity;
        this.adult = adult;
    }
    
    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public String getTitle() {
        return title;
    }
    
    @Override
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Override
    public String getReleaseDate() {
        return release_date;
    }
    
    @Override
    public void setReleaseDate(String release_date) {
        this.release_date = release_date;
    }
    
    @Override
    public String getOriginalLanguage() {
        return original_language;
    }
    
    @Override
    public void setOriginalLanguage(String original_language) {
        this.original_language = original_language;
    }
    
    @Override
    public double getPopularity() {
        return popularity;
    }
    
    @Override
    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }
    
    @Override
    public boolean getAdult() {
        return adult;
    }
    
    @Override
    public void setAdult(boolean adult) {
        this.adult = adult;
    }
    
    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", release_date='" + release_date + '\'' +
                ", original_language='" + original_language + '\'' +
                ", popularity=" + popularity +
                ", adult=" + adult +
                '}';
    }
}