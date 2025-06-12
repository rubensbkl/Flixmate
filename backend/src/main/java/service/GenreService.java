package service;

import java.util.List;

import dao.GenreDAO;
import model.Genre;

public class GenreService {
    private GenreDAO genreDAO;

    public GenreService(GenreDAO genreDAO) {
        this.genreDAO = genreDAO;
    }

    /**
     * Insere um novo gênero no banco de dados.
     *
     * @param id   ID do gênero
     * @param name Nome do gênero
     * @return true se a inserção for bem-sucedida, false caso contrário
     */
    public boolean insertGenre(int id, String name) {
        return genreDAO.insert(new Genre(id, name));
    }

    /**
     * Atualiza um gênero existente no banco de dados.
     *
     * @param genre O objeto Genre contendo os dados atualizados
     * @return true se a atualização for bem-sucedida, false caso contrário
     */
    public Genre getGenreById(int id) {
        return genreDAO.getById(id);
    }

    /**
     * Obtém todos os gêneros disponíveis no banco de dados.
     *
     * @return Lista de todos os gêneros
     */
    public List<Genre> getAllGenres() {
        return genreDAO.getAll();
    }

}
