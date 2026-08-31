package app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dao.*;
import service.*;
import util.*;

@Configuration
public class AppConfig {

    @Value("${DB_HOST:localhost}")
    private String dbHost;

    @Value("${DB_NAME:cinematch}")
    private String dbName;

    @Value("${DB_PORT:5432}")
    private int dbPort;

    @Value("${DB_USER:cinematch}")
    private String dbUser;

    @Value("${DB_PASSWORD:cinematch}")
    private String dbPassword;

    @Value("${TMDB_API_KEY:fake}")
    private String tmdbApiKey;

    @Value("${JWT_SECRET:secret}")
    private String jwtSecret;

    // DAOs
    @Bean
    public UserDAO userDAO() { return new UserDAO(dbHost, dbName, dbPort, dbUser, dbPassword); }
    @Bean
    public MovieDAO movieDAO() { return new MovieDAO(dbHost, dbName, dbPort, dbUser, dbPassword); }
    @Bean
    public GenreDAO genreDAO() { return new GenreDAO(dbHost, dbName, dbPort, dbUser, dbPassword); }
    @Bean
    public MovieGenreDAO movieGenreDAO() { return new MovieGenreDAO(dbHost, dbName, dbPort, dbUser, dbPassword); }
    @Bean
    public FeedbackDAO feedbackDAO() { return new FeedbackDAO(dbHost, dbName, dbPort, dbUser, dbPassword); }
    @Bean
    public FavoriteDAO favoriteDAO() { return new FavoriteDAO(dbHost, dbName, dbPort, dbUser, dbPassword); }
    @Bean
    public WatchLaterDAO watchLaterDAO() { return new WatchLaterDAO(dbHost, dbName, dbPort, dbUser, dbPassword); }
    @Bean
    public RecommendationDAO recommendationDAO() { return new RecommendationDAO(dbHost, dbName, dbPort, dbUser, dbPassword); }
    @Bean
    public UserGenreDAO userGenreDAO() { return new UserGenreDAO(dbHost, dbName, dbPort, dbUser, dbPassword); }
    @Bean
    public PasswordResetDAO passwordResetDAO() { return new PasswordResetDAO(dbHost, dbName, dbPort, dbUser, dbPassword); }

    // Utils
    @Bean
    public TMDBUtil tmdbUtil() { return new TMDBUtil(tmdbApiKey); }
    @Bean
    public JWTUtil jwtUtil() { return new JWTUtil(jwtSecret); }
    
    // Services
    @Bean
    public MovieGenreService movieGenreService(MovieGenreDAO dao) { return new MovieGenreService(dao); }
    @Bean
    public MovieService movieService(MovieDAO dao, MovieGenreService mg, TMDBUtil tmdb) { return new MovieService(dao, mg, tmdb); }
    @Bean
    public FeedbackService feedbackService(FeedbackDAO dao, MovieService ms) { return new FeedbackService(dao, ms); }
    @Bean
    public UserService userService(UserDAO dao) { return new UserService(dao); }
    @Bean
    public UserGenreService userGenreService(UserGenreDAO dao) { return new UserGenreService(dao); }
    @Bean
    public WatchLaterService watchLaterService(WatchLaterDAO dao) { return new WatchLaterService(dao); }
    @Bean
    public FavoriteService favoriteService(FavoriteDAO dao) { return new FavoriteService(dao); }
    @Bean
    public GenreService genreService(GenreDAO dao) { return new GenreService(dao); }
    @Bean
    public RecommendationService recommendationService(RecommendationDAO dao, TMDBUtil tmdb) { return new RecommendationService(dao, tmdb); }
}
