-- Script para inicializar o banco de dados

-- Criar tabela de usuários
CREATE TABLE IF NOT EXISTS "user" (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    gender CHAR(1) NOT NULL,
    content_filter BOOLEAN DEFAULT TRUE,  -- Filtro para conteúdo adulto
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Criar tabela de gêneros (do TMDB)
CREATE TABLE IF NOT EXISTS genres (
    id INTEGER PRIMARY KEY,  -- Usando o mesmo ID que o TMDB usa
    name VARCHAR(50) NOT NULL
);

-- Criar tabela de gêneros preferidos dos usuários (relação muitos-para-muitos)
CREATE TABLE IF NOT EXISTS user_preferred_genres (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES "user"(id) ON DELETE CASCADE,
    genre_id INTEGER REFERENCES genres(id) ON DELETE CASCADE,
    UNIQUE(user_id, genre_id)  -- Evita duplicatas
);

-- Criar tabela de interações
CREATE TABLE IF NOT EXISTS interactions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES "user"(id),
    movie_id INTEGER NOT NULL,
    interaction BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Criar tabela de recomendações
CREATE TABLE IF NOT EXISTS recommendations (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES "user"(id),
    movie_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Criar índices para melhorar performance
CREATE INDEX IF NOT EXISTS idx_interactions_user_id ON interactions(user_id);
CREATE INDEX IF NOT EXISTS idx_recommendations_user_id ON recommendations(user_id);
CREATE INDEX IF NOT EXISTS idx_user_preferred_genres_user_id ON user_preferred_genres(user_id);
CREATE INDEX IF NOT EXISTS idx_user_preferred_genres_genre_id ON user_preferred_genres(genre_id);

-- Inserir um usuário de teste (opcional)
INSERT INTO "user" (first_name, last_name, email, password, gender)
VALUES ('admin', 'adm', 'admin@admin.com', 'senha123', 'M')
ON CONFLICT (email) DO NOTHING;

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

-- Adicionar alguns gêneros preferidos para o usuário de teste
INSERT INTO user_preferred_genres (user_id, genre_id)
VALUES 
((SELECT id FROM "user" WHERE email = 'admin@admin.com'), 28),  -- Ação
((SELECT id FROM "user" WHERE email = 'admin@admin.com'), 12),  -- Aventura
((SELECT id FROM "user" WHERE email = 'admin@admin.com'), 878)  -- Ficção científica
ON CONFLICT (user_id, genre_id) DO NOTHING;