package service;

import java.util.List;

import dao.GenreDAO;
import model.Genre;

public class GenreService {
    private GenreDAO genreDAO;


    public GenreService(GenreDAO genreDAO) {
        this.genreDAO = genreDAO;
    }

    public boolean insertGenre(int id, String name) {
        return genreDAO.insert(new Genre(id, name));
    }

    public Genre getGenreById(int id) {
        return genreDAO.getById(id);
    }

    public List<Genre> getAllGenres() {
        return genreDAO.getAll();
    }

    

    
}
