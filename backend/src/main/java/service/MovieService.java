package service;

import java.util.ArrayList;
import java.util.List;

import dao.MovieDAO;
import model.Movie;

public class MovieService {
    private MovieDAO movieDAO;
    private MovieGenreService movieGenreService;
    private TMDBService tmdbService;
    
    // Construtor com dependências
    public MovieService(MovieDAO movieDAO, MovieGenreService movieGenreService, TMDBService tmdbService) {
        this.movieDAO = movieDAO;
        this.movieGenreService = movieGenreService;
        this.tmdbService = tmdbService;
    }
    
    /**
     * Busca um filme do TMDB e o salva no banco de dados junto com seus gêneros
     * 
     * @param movieId O ID do filme no TMDB
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    public boolean buscarESalvarFilme(int movieId) {
        try {
            // Obtém os detalhes do filme do TMDB, independente se já existe no banco ou não
            Movie movie = tmdbService.getMovieDetails(movieId);
            if (movie == null) {
                System.err.println("Filme não encontrado no TMDB: " + movieId);
                return false;
            }
            
            // Verifica se o filme já existe no banco
            boolean filmeExiste = movieDAO.exists(movieId);
            
            // Se não existir, tenta salvar
            if (!filmeExiste && !salvarFilme(movie)) {
                return false;
            }
            
            // Em ambos os casos (filme existente ou recém-salvo), adiciona apenas os gêneros faltantes
            return adicionarGenerosFaltantes(movie);
            
        } catch (Exception e) {
            System.err.println("Erro em buscarESalvarFilme: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tenta salvar um filme no banco de dados, lidando com possíveis erros de chave duplicada
     */
    private boolean salvarFilme(Movie movie) {
        try {
            System.out.println("Salvando filme no banco: " + movie.getTitle());
            boolean filmeSalvo = movieDAO.insert(movie);
            if (!filmeSalvo) {
                System.err.println("Falha ao inserir filme no banco: " + movie.getId());
                return false;
            }
            return true;
        } catch (Exception e) {
            // Se for erro de chave duplicada, considera como sucesso
            if (e.getMessage().contains("duplicate key") || e.getMessage().contains("already exists")) {
                System.out.println("Filme foi inserido por outro processo, continuando com os gêneros...");
                return true;
            }
            throw e; // Relança qualquer outro tipo de exceção
        }
    }

    /**
     * Adiciona apenas os gêneros que ainda não existem para o filme
     */
    private boolean adicionarGenerosFaltantes(Movie movie) {
        ArrayList<Integer> genreIds = movie.getGenreIds();
        
        if (genreIds == null || genreIds.isEmpty()) {
            return true; // Nada a fazer se não houver gêneros
        }
        
        System.out.println("Verificando gêneros faltantes para o filme: " + movie.getTitle());
        
        // Obtém os gêneros que já estão associados ao filme
        List<Integer> generosExistentes = movieGenreService.getGenreIdsForMovie(movie.getId());
        
        // Filtra apenas os gêneros que ainda não estão associados
        List<Integer> generosFaltantes = genreIds.stream()
                .filter(genreId -> !generosExistentes.contains(genreId))
                .collect(java.util.stream.Collectors.toList());
        
        if (generosFaltantes.isEmpty()) {
            System.out.println("Nenhum gênero novo para adicionar ao filme: " + movie.getId());
            return true;
        }
        
        System.out.println("Adicionando " + generosFaltantes.size() + " gêneros faltantes ao filme: " + movie.getId());
        boolean generosRelacionadosSalvos = movieGenreService.registrarGenerosDoFilme(movie.getId(), generosFaltantes);
        
        if (!generosRelacionadosSalvos) {
            System.err.println("Aviso: Falha ao salvar alguns ou todos os relacionamentos de gênero para o filme ID " + movie.getId());
        }
        
        return true; // Retorna true mesmo se a associação de gêneros falhar
    }
    
    /**
     * Atualiza os gêneros de um filme
     * 
     * @param movieId O ID do filme
     * @param genreIds A nova lista de IDs de gêneros
     * @return true se bem-sucedido, false caso contrário
     */
    public boolean atualizarGenerosDoFilme(int movieId, List<Integer> genreIds) {
        return movieGenreService.atualizarGenerosDoFilme(movieId, genreIds);
    }

    /**
     * Busca um filme pelo ID
     * 
     * @param movieId O ID do filme
     * @return O filme encontrado ou null se não encontrado
     */
    public Movie buscarFilmePorId(int movieId) {
        return movieDAO.getMovieById(movieId);
    }
}