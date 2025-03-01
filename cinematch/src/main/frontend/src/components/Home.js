// src/main/frontend/src/components/Home.js
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

function Home() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchMovies = async () => {
      try {
        const response = await fetch('/api/movies/trending');
        if (response.ok) {
          const data = await response.json();
          setMovies(data);
        } else {
          console.error("Failed to fetch movies");
        }
      } catch (error) {
        console.error("Error:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchMovies();
  }, []);

  const handleLike = async (movieId) => {
    try {
      const response = await fetch('/api/movies/like', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ movieId }),
      });

      if (response.ok) {
        // Atualizar a UI para mostrar que o filme foi curtido
        setMovies(movies.map(movie =>
          movie.id === movieId
            ? { ...movie, liked: true }
            : movie
        ));
      }
    } catch (error) {
      console.error("Error liking movie:", error);
    }
  };

  if (loading) {
    return <div className="loading">Carregando filmes...</div>;
  }

  return (
    <div className="home-container">
      <h1>Descubra Novos Filmes</h1>

      <div className="movie-grid">
        {movies.map(movie => (
          <div key={movie.id} className="movie-card">
            <Link to={`/movie/${movie.id}`}>
              <img
                src={movie.poster}
                alt={movie.title}
                className="movie-poster"
              />
              <h3>{movie.title}</h3>
              <div className="movie-rating">⭐ {movie.rating}</div>
            </Link>
            <button
              className={`like-button ${movie.liked ? 'liked' : ''}`}
              onClick={() => handleLike(movie.id)}
            >
              {movie.liked ? '❤️ Curtido' : '🤍 Curtir'}
            </button>
          </div>
        ))}
      </div>

      <div className="recommendation-cta">
        <h2>Quer descobrir filmes personalizados para você?</h2>
        <Link to="/recommendations" className="recommendation-button">
          Ver Recomendações
        </Link>
      </div>
    </div>
  );
}

export default Home;