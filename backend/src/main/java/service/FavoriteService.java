package service;

import java.util.ArrayList;

import dao.FavoriteDAO;
import model.Favorite;

public class FavoriteService {
    private FavoriteDAO favoriteDAO;

    public FavoriteService(FavoriteDAO favoriteDAO) {
        this.favoriteDAO = favoriteDAO;
    }

    /**
     * Retorna uma lista de IDs de filmes favoritos do usuário.
     *
     * @param userId o ID do usuário
     * @return uma lista de IDs de filmes favoritos
     */
    public ArrayList<Integer> getFavoriteMovies(int userId) {
        return favoriteDAO.getFavoriteMovieIds(userId);
    }

    /**
     * Adiciona ou remove um filme dos favoritos do usuário.
     *
     * @param favorite o objeto Favorite contendo o ID do usuário e do filme
     * @param status   true para adicionar o filme aos favoritos, false para removê-lo
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    public boolean toggleFavorite(Favorite favorite, boolean status) {
        if (status) {
            return favoriteDAO.addToFavorites(favorite);
        } else {
            return favoriteDAO.removeFromFavorites(favorite);
        }
    }
    
    /**
     * Verifica se um filme está nos favoritos do usuário.
     *
     * @param favorite o objeto Favorite contendo o ID do usuário e do filme
     * @return true se o filme estiver nos favoritos, false caso contrário
     */
    public boolean isInFavorites(Favorite favorite) {
        return favoriteDAO.isInFavorites(favorite);
    }

}
