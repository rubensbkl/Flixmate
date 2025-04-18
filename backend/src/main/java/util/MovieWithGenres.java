package util;

import java.util.ArrayList;
import java.util.List;

import model.Genre;
import model.Movie;

/**
 * Classe de transferência de dados (DTO) que representa um filme com seus
 * gêneros
 * Usada apenas para processamento em memória, não para armazenamento no banco
 */
public class MovieWithGenres extends Movie {
    private List<Genre> genres;

    public MovieWithGenres(Movie movie) {
        super(movie.getId(), movie.getTitle(), movie.getReleaseDate(),
                movie.getOriginalLanguage(), movie.getPopularity(), movie.getAdult());
        this.genres = new ArrayList<>();
    }

    public MovieWithGenres(Movie movie, List<Genre> genres) {
        super(movie.getId(), movie.getTitle(), movie.getReleaseDate(),
                movie.getOriginalLanguage(), movie.getPopularity(), movie.getAdult());
        this.genres = genres != null ? new ArrayList<>(genres) : new ArrayList<>();
    }

    public List<Genre> getGenres() {
        return new ArrayList<>(genres);
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres != null ? new ArrayList<>(genres) : new ArrayList<>();
    }

    public void addGenre(Genre genre) {
        if (genre != null && !containsGenre(genre.getId())) {
            this.genres.add(genre);
        }
    }

    public boolean containsGenre(int genreId) {
        for (Genre genre : genres) {
            if (genre.getId() == genreId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Formata os gêneros como uma string para exibição
     */
    public String getGenresAsString() {
        if (genres == null || genres.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < genres.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Genre genre = genres.get(i);
            sb.append(genre.getName()).append(" (ID: ").append(genre.getId()).append(")");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "MovieWithGenres{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", release_date='" + getReleaseDate() + '\'' +
                ", original_language='" + getOriginalLanguage() + '\'' +
                ", popularity=" + getPopularity() +
                ", adult=" + getAdult() +
                ", genres=" + genres +
                '}';
    }
}