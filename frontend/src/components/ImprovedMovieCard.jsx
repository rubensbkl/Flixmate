'use client';
import "@/styles/tinder-card.css";
import { useEffect, useState } from 'react';

const ImprovedMovieCard = ({ movie, onSwipe, isActive = true, isAnimating = false, swipeDirection = null }) => {
  // Estado para controlar se mostra a descrição completa
  const [showFullDescription, setShowFullDescription] = useState(false);

  // Função para prevenir o comportamento padrão de drag de imagens
  const preventDragHandler = (e) => {
    e.preventDefault();
    return false;
  };

  // Efeito para resetar descrição quando card muda
  useEffect(() => {
    setShowFullDescription(false);
  }, [movie]);

  // Pegando o caminho do poster ou usando um fallback
  const posterPath = movie.image;

  return (
    <div
      className="relative h-[70vh] w-[90vw] max-w-md md:h-[75vh] md:w-[95vw] md:max-w-lg rounded-3xl overflow-hidden shadow-xl transition-all duration-300"
      style={{
        touchAction: 'none',
        transform: isActive ? 'scale(1)' : 'scale(0.95)',
        transition: 'all 0.3s ease',
      }}
    >
      {/* Usando div com background-image ao invés de img para evitar problemas de drag */}
      <div
        className="absolute inset-0 bg-cover bg-center"
        style={{
          backgroundImage: `url(${posterPath})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
        }}
        onDragStart={preventDragHandler}
      />
      
      {/* Indicadores de swipe */}
      {swipeDirection === 'left' && (
        <div className="absolute top-8 left-8 bg-red-500 text-white py-1 px-4 rounded-full transform -rotate-12 font-bold">
          DISLIKE
        </div>
      )}
      {swipeDirection === 'right' && (
        <div className="absolute top-8 right-8 bg-green-500 text-white py-1 px-4 rounded-full transform rotate-12 font-bold">
          LIKE
        </div>
      )}

      {/* Overlay gradiente para legibilidade do texto */}
      <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent" />
      
      {/* Informações do filme */}
      <div className="absolute bottom-0 left-0 right-0 p-4 text-white">
        <h2 className="text-xl font-bold">{movie.title}</h2>
        <p className="text-sm opacity-90">
          {movie.release_date ? new Date(movie.release_date).getFullYear() : 'N/A'}
        </p>
        <div className="flex items-center mt-1">
          <span className="bg-yellow-500 text-black text-xs font-bold px-2 py-1 rounded mr-2">
            {movie.vote_average ? movie.vote_average.toFixed(1) : 'N/A'}
          </span>
        </div>
        
        {/* Descrição que expande ao clicar */}
        <div className={`mt-2 relative ${showFullDescription ? 'bg-black/30 p-2 rounded-lg' : ''}`}>
          <p className={`text-sm ${!showFullDescription ? 'line-clamp-3' : ''}`}>
            {movie.description || 'Nenhuma descrição disponível.'}
          </p>
          
          {/* Botão "Ver mais" / "Ver menos" */}
          {movie.description && movie.description.length > 150 && (
            <button
              className="text-xs text-blue-300 mt-1 font-medium"
              onClick={(e) => {
                e.stopPropagation(); // Evita que o clique afete o TinderCard
                setShowFullDescription(!showFullDescription);
              }}
            >
              {showFullDescription ? 'Ver menos' : 'Ver mais'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default ImprovedMovieCard;