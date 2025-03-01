// src/main/frontend/src/components/Recommendations.js
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

function Recommendations() {
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchRecommendations = async () => {
      try {
        setLoading(true);
        const response = await fetch('/api/movies/recommendations');

        if (!response.ok) {
          throw new Error('Falha ao carregar recomendações');
        }

        const data = await response.json();
        setRecommendations(data.movies || []);
      } catch (error) {
        console.error("Error fetching recommendations:", error);
        setError(error.message);
      } finally {
        setLoading(false);
      }
    };

    fetchRecommendations();
  }, []);

  if (loading) {
    return (
      <div className="recommendations-loading">
        <div className="loading-spinner"></div>
        <p>Gerando recomendações personalizadas para você...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="recommendations-error">
        <h2>Ops! Algo deu errado</h2>
        <p>{error}</p>
        <button onClick={() => window.location.reload()}>Tentar novamente</button>
      </div>
    );
  }

  if (recommendations.length === 0) {
    return (
      <div className="no-recommendations">
        <h2>Ainda não temos recomendações para você</h2>
        <p>Continue curtindo filmes para melhorarmos nossas sugestões!</p>
        <Link to="/" className="back-button">Voltar para filmes</Link>
      </div>
    );
  }

  return (
    <div className="recommendations-container">
      <h1>Filmes Recomendados para Você</h1>
      <p className="ai-note">Baseado em seus gostos e analisado por nossa Inteligência Artificial</p>

      <div className="recommendation-grid">
        {recommendations.map((movie) => (
          <div key={movie.id} className="recommendation-card">
            <Link to={`/movie/${movie.id}`}>
              <img src={movie.poster} alt={movie.title} className="movie-poster" />
              <div className="movie-info">
                <h3>{movie.title}</h3>
                <div className="match-percentage">{movie.matchPercentage}% de match</div>
                <p className="match-reason">{movie.matchReason}</p>
              </div>
            </Link>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Recommendations;