import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";

const MovieDetail = () => {
  const { id } = useParams();
  const [movie, setMovie] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchMovie = async () => {
      try {
        const response = await fetch(`/api/movies/${id}`);
        if (response.ok) {
          const data = await response.json();
          setMovie(data);
        } else {
          setError("Filme não encontrado.");
        }
      } catch (error) {
        setError("Erro ao buscar os detalhes do filme.");
      } finally {
        setLoading(false);
      }
    };

    fetchMovie();
  }, [id]);

  if (loading) return <div>Carregando...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="movie-detail">
      <h2>{movie.title}</h2>
      <img src={movie.poster} alt={movie.title} />
      <p>{movie.description}</p>
      <p><strong>Gênero:</strong> {movie.genre}</p>
      <p><strong>Ano:</strong> {movie.year}</p>
    </div>
  );
};

export default MovieDetail;