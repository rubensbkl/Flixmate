package model;

import java.util.ArrayList;

public class Movie {
    private int id;
    private String title;
    private String release_date;
    private String original_language;
    private double popularity;
    private Boolean adult;
    private ArrayList<Integer> genreIds;

    public Movie() {
        this.id = 0;
        this.title = "";
        this.release_date = "";
        this.original_language = "";
        this.popularity = 0.0;
        this.adult = false;
        this.genreIds = new ArrayList<>();
    }
    
    public Movie(int id, String title, String release_date, String original_language, double popularity, Boolean adult) {
        this.id = id;
        this.title = title; 
        this.release_date = release_date;
        this.original_language = original_language;
        this.popularity = popularity;
        this.adult = adult;
    }

    public Movie(int id, String title, String release_date, String original_language, double popularity, Boolean adult, ArrayList<Integer> genreIds) {
        this.id = id;
        this.title = title; 
        this.release_date = release_date;
        this.original_language = original_language;
        this.popularity = popularity;
        this.adult = adult;
        this.genreIds = genreIds;
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

    public String getReleaseDate() {
        return release_date;
    }

    public void setReleaseDate(String release_date) {
        this.release_date = release_date;
    }

    public String getOriginalLanguage() {
        return original_language;
    }

    public void setOriginalLanguage(String original_language) {
        this.original_language = original_language;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public Boolean getAdult() {
        return adult;
    }

    public void setAdult(Boolean adult) {
        this.adult = adult;
    }

    public ArrayList<Integer> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(ArrayList<Integer> genreIds) {
        this.genreIds = genreIds;
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
