
package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import model.Feedback;
import model.Genre;
import model.Movie;
import service.AIService;
import service.FeedbackService;
import service.GenreService;
import service.MovieGenreService;
import service.MovieService;
import service.RecommendationService;
import service.TMDBService;
import spark.Response;
import util.RecommendationHelper;

public class Controller {
    private TMDBService tmdb;
    private MovieService movieService;
    private MovieGenreService movieGenreService;
    private RecommendationService recommendationService;
    private FeedbackService feedbackService;
    private AIService ai;
    private GenreService genreService;
    private Gson gson;

    public Controller(TMDBService tmdb, MovieService movieService, MovieGenreService movieGenreService,
            RecommendationService recommendationService, AIService ai, GenreService genreService,
            FeedbackService feedbackService) {
        this.tmdb = tmdb;
        this.movieService = movieService;
        this.movieGenreService = movieGenreService;
        this.recommendationService = recommendationService;
        this.ai = ai;
        this.genreService = genreService;
        this.feedbackService = feedbackService;
        // Inicializa o Gson para manipulação de JSON
        this.gson = new Gson();
        
    }

    // Método para gerar recomendação aleatória
    public String gerarRecomendacaoAleatoria(int userId, List<Genre> generosFavoritos, Response res) {
        try {
            System.out.println("Usuário sem interações. Gerando recomendação aleatória...");

            // Obter um filme aleatório
            JsonObject movie = tmdb.getARandomMovie();

            if (movie == null || !movie.has("id")) {
                res.status(400);
                return gson.toJson(Map.of("error", "Não foi possível obter um filme aleatório"));
            }

            // Extrair o ID do filme
            int recommendation = movie.get("id").getAsInt();
            System.out.println("ID do filme recomendado: " + recommendation);

            // Procurar o filme no banco de dados
            Movie existingMovie = movieService.buscarFilmePorId(recommendation);
            JsonObject movieObj;
            if (existingMovie != null) {
                System.out.println("Filme já existe no banco de dados: " + existingMovie);
                movieObj = gson.toJsonTree(existingMovie).getAsJsonObject();
            } else {
                System.out.println("Filme não encontrado no banco de dados, buscando na API...");
                movieObj = tmdb.getMovieDetails(recommendation);

                // Armazenar o filme no banco de dados
                boolean storedMovie = movieService.storeMovie(movieObj);
                // Armazenar os gêneros do filme no banco de dados
                boolean storedGenres = movieGenreService.storeMovieGenres(movieObj);
                // Armazenar a recomendação no banco de dados
                System.out.println("Filme armazenado: " + storedMovie);

            }

            // Checar se o filme já foi recomendado para o usuário
            if (recommendationService.checkIfMovieAlreadyRecommended(userId, recommendation)) {
                System.out.println("Filme já recomendado para o usuário: " + recommendation);
                res.status(400);
                return gson.toJson(Map.of("error", "Filme já recomendado para o usuário"));
            }

            boolean storedRecommendation = recommendationService.storeRecommendation(userId, recommendation);

            return gson.toJson(Map.of("status", "ok", "recomendacao", movieObj, "tipo", "aleatoria"));
        } catch (Exception e) {
            System.out.println("Erro ao gerar recomendação aleatória:");
            e.printStackTrace();
            res.status(500);
            return gson.toJson(Map.of("error", "Erro ao gerar recomendação aleatória"));
        }
    }

    // Método para gerar recomendação personalizada
    public JsonObject gerarRecomendacaoPersonalizada(int userId, List<Feedback> interacoes,
            List<Genre> generosFavoritos,
            Response res) {
        try {
            System.out.println("Gerando recomendação personalizada com base em interações...");
            RecommendationHelper helper = new RecommendationHelper();

            // Adicionar filmes com interações ao helper
            for (Feedback feedback : interacoes) {
                int movieId = feedback.getMovieId();
                Movie movie = movieService.buscarFilmePorId(movieId);
                List<Genre> generos = movieGenreService.buscarGenerosDoFilme(movieId);

                helper.addMovieWithGenres(movie, generos);
            }

            // Obter filmes candidatos para recomendação
            JsonArray candidateMoviesJson = recommendationService.getCandidateMoviesJSON(interacoes);

            if (candidateMoviesJson == null || candidateMoviesJson.size() == 0) {
                res.status(400);
                // create an JSonObject with the error message
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Nenhum filme candidato encontrado");
                return errorResponse;
            }

            // Adicionar filmes candidatos ao helper
            for (int i = 0; i < candidateMoviesJson.size(); i++) {
                JsonObject movieJson = candidateMoviesJson.get(i).getAsJsonObject();

                // Extrair os dados básicos do filme a partir do JSON
                int movieId = movieJson.get("id").getAsInt();
                String title = movieJson.get("title").getAsString();
                String releaseDate = movieJson.has("release_date") ? movieJson.get("release_date").getAsString() : "";
                String originalLanguage = movieJson.get("original_language").getAsString();
                double popularity = movieJson.get("popularity").getAsDouble();
                boolean adult = movieJson.get("adult").getAsBoolean();

                Movie movie = new Movie(movieId, title, releaseDate, originalLanguage, popularity, adult);
                helper.addCandidateMovieId(movieId);

                // Processar gêneros do JSON
                List<Genre> genres = new ArrayList<>();
                if (movieJson.has("genre_ids") && movieJson.get("genre_ids").isJsonArray()) {
                    JsonArray genreIdsJson = movieJson.getAsJsonArray("genre_ids");
                    for (int j = 0; j < genreIdsJson.size(); j++) {
                        int genreId = genreIdsJson.get(j).getAsInt();
                        Genre genre = genreService.getGenreById(genreId);
                        if (genre != null) {
                            genres.add(genre);
                        }
                    }
                }

                // Adicionar o filme com seus gêneros ao helper
                helper.addMovieWithGenres(movie, genres);
            }

            // Verificar se temos dados suficientes para gerar recomendação
            if (!helper.hasEnoughDataForRecommendation()) {
                res.status(400);
                // create an JSonObject with the error message
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Dados insuficientes para gerar recomendação personalizada");
                return errorResponse;
            }

            System.out.println("Chamando IA para gerar recomendação...");
            int recommendedMovieId = ai.gerarRecomendacao(interacoes, generosFavoritos, helper);
            System.out.println("ID do filme recomendado: " + recommendedMovieId);

            // Limpar interações após gerar recomendação
            System.out.println("Limpando interações do usuário...");
            boolean clearFeedback = feedbackService.clearAllById(userId);
            System.out.println("Resultado da limpeza: " + clearFeedback);

            if (!clearFeedback) {
                res.status(500);
                // create an JSonObject with the error message
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Erro ao limpar interações");
                // return the error response
                return errorResponse;
            }

            // Limpar o helper depois de usar
            helper.clear();

            // Checar se o filme já existe no banco de dados
            Movie existingMovie = movieService.buscarFilmePorId(recommendedMovieId);
            JsonObject movieObj;
            if (existingMovie != null) {
                System.out.println("Filme já existe no banco de dados: " + existingMovie);
                movieObj = gson.toJsonTree(existingMovie).getAsJsonObject();
            } else {
                System.out.println("Filme não encontrado no banco de dados, buscando na API...");
                movieObj = tmdb.getMovieDetails(recommendedMovieId);
                // Armazenar o filme no banco de dados
                boolean storedMovie = movieService.storeMovie(movieObj);
                System.out.println("Filme armazenado: " + storedMovie);
                boolean storedGenres = movieGenreService.storeMovieGenres(movieObj);
            }

            // Armazenar a recomendação no banco de dados
            boolean storedRecommendation = recommendationService.storeRecommendation(userId, recommendedMovieId);
            System.out.println("Recomendação armazenada: " + storedRecommendation);

            return movieObj;
        } catch (Exception e) {
            System.out.println("Erro ao gerar recomendação personalizada:");
            e.printStackTrace();
            res.status(500);
            // create an JSonObject with the error message
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Erro ao gerar recomendação personalizada");
            // return the error response
            return errorResponse;
            
        }
    }

}
