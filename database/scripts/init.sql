-- Script para inicializar o banco de dados

-- Criar tabela de usuários
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    gender CHAR(1) NOT NULL,
    content_filter BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Criar tabela de filmes
CREATE TABLE IF NOT EXISTS movies (
    id INTEGER PRIMARY KEY,
    title TEXT NOT NULL,
    overview TEXT,
    rating DOUBLE PRECISION NOT NULL,
    release_date VARCHAR(15) NOT NULL,
    original_language VARCHAR(10) NOT NULL,
    popularity DOUBLE PRECISION NOT NULL,
    adult BOOLEAN DEFAULT FALSE,
    poster_path TEXT
);

-- Criar tabela de gêneros (do TMDB)
CREATE TABLE IF NOT EXISTS genres (
    id INTEGER PRIMARY KEY, -- Usando o mesmo ID que o TMDB usa
    name VARCHAR(50) NOT NULL
);

-- Criar tabela de gêneros preferidos dos usuários (relação muitos-para-muitos)
CREATE TABLE IF NOT EXISTS user_genres (
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    genre_id INTEGER REFERENCES genres(id) ON DELETE CASCADE,
    UNIQUE(user_id, genre_id)
);

-- Criar tabela de gêneros dos filmes (relação muitos-para-muitos)
CREATE TABLE IF NOT EXISTS movie_genres (
    movie_id INTEGER REFERENCES movies(id) ON DELETE CASCADE,
    genre_id INTEGER REFERENCES genres(id) ON DELETE CASCADE,
    UNIQUE(movie_id, genre_id)
);

-- Criar tabela de interações
CREATE TABLE IF NOT EXISTS feedbacks (
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    movie_id INTEGER REFERENCES movies(id) ON DELETE CASCADE NOT NULL,
    feedback BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, movie_id)
);

-- Criar tabela de recomendações
CREATE TABLE IF NOT EXISTS recommendations (
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    movie_id INTEGER REFERENCES movies(id) ON DELETE CASCADE NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, movie_id)
);

-- Criar índices para melhorar performance
CREATE INDEX IF NOT EXISTS idx_feedbacks_user_id ON feedbacks(user_id);
CREATE INDEX IF NOT EXISTS idx_feedbacks_movie_id ON feedbacks(movie_id);
CREATE INDEX IF NOT EXISTS idx_recommendations_user_id ON recommendations(user_id);
CREATE INDEX IF NOT EXISTS idx_recommendations_movie_id ON recommendations(movie_id);
CREATE INDEX IF NOT EXISTS idx_user_genres_user_id ON user_genres(user_id);
CREATE INDEX IF NOT EXISTS idx_movie_genres_movie_id ON movie_genres(movie_id);
CREATE INDEX IF NOT EXISTS idx_user_genres_genre_id ON user_genres(genre_id);
CREATE INDEX IF NOT EXISTS idx_movie_genres_genre_id ON movie_genres(genre_id);

-- Inserir gêneros comuns do TMDB (IDs reais do TMDB)
INSERT INTO genres (id, name) VALUES
(28, 'Ação'),
(12, 'Aventura'),
(16, 'Animação'),
(35, 'Comédia'),
(80, 'Crime'),
(99, 'Documentário'),
(18, 'Drama'),
(10751, 'Família'),
(14, 'Fantasia'),
(36, 'História'),
(27, 'Terror'),
(10402, 'Música'),
(9648, 'Mistério'),
(10749, 'Romance'),
(878, 'Ficção científica'),
(10770, 'Cinema TV'),
(53, 'Thriller'),
(10752, 'Guerra'),
(37, 'Faroeste')
ON CONFLICT (id) DO NOTHING;

-- Inserir usuários de exemplo
INSERT INTO users (first_name, last_name, email, password, gender, content_filter) VALUES
('admin', 'admin', 'admin@admin.com', '$2a$12$DCtnqzWD9NIe1Gt2hfmUvOCe/YCH9gAhg2n9sD6jnKQ2V1qWDk.B.', 'M', false)
ON CONFLICT (email) DO NOTHING;