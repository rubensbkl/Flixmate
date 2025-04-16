package service;

import java.util.ArrayList;
import java.util.List;

import model.Feedback;
import model.Movie;
import service.TMDBService;

public class RecommendationService {

    private TMDBService tmdbService;

    public RecommendationService(TMDBService tmdbService) {
        this.tmdbService = tmdbService;
    }

    public List<Movie> getCandidateMovies(List<Feedback> interacoes) {
        List<Movie> candidatos = new ArrayList<>();
    
        for (Feedback interacao : interacoes) {
            try {
                // Pega o primeiro similar
                Movie similar = tmdbService.getRandomSimilarMovie(interacao.getMovieId());
    
                if (similar != null) {
                    candidatos.add(similar);
                }
    
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Erro ao buscar similares para o filme ID: " + interacao.getMovieId());
            }
        }
    
        return candidatos;
    }
}
