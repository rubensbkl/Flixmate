package service;

import java.util.ArrayList;

import dao.WatchLaterDAO;
import model.WatchLater;

public class WatchLaterService {
    private WatchLaterDAO watchLaterDAO;

    public WatchLaterService(WatchLaterDAO watchLaterDAO) {
        this.watchLaterDAO = watchLaterDAO;
    }

    /**
     * Retorna uma lista de IDs de filmes que o usuário deseja assistir mais tarde.
     *
     * @param userId o ID do usuário
     * @return uma lista de IDs de filmes
     */
    public ArrayList<Integer> getWatchLaterMovies(int userId) {
        return watchLaterDAO.getWatchLaterMovieIds(userId);
    }

    /**
     * Adiciona ou remove um filme da lista de "assistir mais tarde" do usuário.
     *
     * @param watchLater o objeto WatchLater contendo o ID do usuário e do filme
     * @param watched    true para adicionar o filme à lista, false para removê-lo
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    public boolean toggleWatchLater(WatchLater watchLater, boolean watched) {
        if (watched) {
            return watchLaterDAO.addToWatchLater(watchLater);
        } else {
            return watchLaterDAO.removeFromWatchLater(watchLater);
        }
    }

    /**
     * Verifica se um filme está na lista de "assistir mais tarde" do usuário.
     *
     * @param watchLater o objeto WatchLater contendo o ID do usuário e do filme
     * @return true se o filme estiver na lista, false caso contrário
     */
    public boolean isInWatchLater(WatchLater watchLater) {
        return watchLaterDAO.isInWatchLater(watchLater);
    }

}