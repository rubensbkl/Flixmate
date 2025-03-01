import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";

const Recommendations = () => {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchRecommendations = async () => {
      try {
        const response = await fetch("/api/recommendations", { credentials: "include" });
        if (response.ok) {
          const data = await response.json();
          setMovies(data);
        } else {
          setError("Não foi possível obter recomendações.");
        }
      } catch (error) {
        setError("Erro ao buscar recomendações.");
      } finally {
        setLoading(false);
      }
    };

    fetchRecommendations();
  }, []);

  if (loading) return <div>Carregando recomendações...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="recommendations">
      <h2>Recomendações de Filmes</h2>
      <ul>
        {movies.map((movie) => (
          <li key={movie.id}>
            <Link to={`/movie/${movie.id}`}>
              <img src={movie.poster} alt={movie.title} />
              <p>{movie.title}</p>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Recommendations;