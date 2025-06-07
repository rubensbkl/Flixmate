'use client';
import "@/styles/tinder-card.css";
import Image from 'next/image';
import { memo, useEffect, useState } from 'react';

const ImprovedMovieCard = memo(({ 
  movie, 
  isActive = true, 
  isAnimating = false, 
  swipeDirection = null,
  className = ""
}) => {
  const [showFullDescription, setShowFullDescription] = useState(false);
  const [imageLoaded, setImageLoaded] = useState(false);
  const [imageError, setImageError] = useState(false);

  useEffect(() => {
    setShowFullDescription(false);
    setImageLoaded(false);
    setImageError(false);
  }, [movie?.id]);

  const posterPath = movie?.image || 'https://via.placeholder.com/500x750?text=No+Image';

  const preventDefaultHandler = (e) => {
    e.preventDefault();
    e.stopPropagation();
    return false;
  };

  if (!movie) {
    return null;
  }

  return (
    <div
      className={`relative rounded-3xl overflow-hidden shadow-xl transition-all duration-300 ${className}`}
      style={{
        touchAction: 'none',
        userSelect: 'none', 
        transform: isActive ? 'scale(1)' : 'scale(0.95)',
        transition: 'all 0.3s ease',
        minHeight: '400px',
        width: '100%',
        display: 'block',
        backgroundColor: '#f0f0f0',
      }}
      onDragStart={preventDefaultHandler}
      onContextMenu={preventDefaultHandler}
    >
      {/* Loading skeleton */}
      {!imageLoaded && !imageError && (
        <div className="absolute inset-0 bg-foreground animate-pulse" />
      )}
      
      {/* Image com lazy loading usando Next.js Image */}
      <Image
        src={posterPath}
        alt={movie.title}
        fill
        className={`object-cover transition-opacity duration-500 ${
          imageLoaded ? 'opacity-100' : 'opacity-0'
        }`}
        onLoad={() => setImageLoaded(true)}
        onError={() => setImageError(true)}
        priority={isActive} // Priorizar imagem ativa
        loading={isActive ? "eager" : "lazy"} // Lazy loading para cards não ativos
        draggable="false"
      />
      
      {/* Fallback para erro de imagem */}
      {imageError && (
        <div className="absolute inset-0 bg-gray-300 flex items-center justify-center">
          <span className="text-gray-600">Sem imagem</span>
        </div>
      )}

      {/* Indicadores de swipe */}
      {swipeDirection === 'left' && (
        <div className="absolute top-8 left-8 bg-red-500 text-white py-1 px-4 rounded-full transform -rotate-12 font-bold z-10">
          DISLIKE
        </div>
      )}
      {swipeDirection === 'right' && (
        <div className="absolute top-8 right-8 bg-green-500 text-white py-1 px-4 rounded-full transform rotate-12 font-bold z-10">
          LIKE
        </div>
      )}

      {/* Overlay gradiente para legibilidade do texto */}
      <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent pointer-events-none" />
      
      {/* Informações do filme */}
      <div className="absolute bottom-0 left-0 right-0 p-4 text-white">
        <h2 className="text-xl font-bold truncate">{movie.title}</h2>
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
                e.stopPropagation();
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
});

ImprovedMovieCard.displayName = 'ImprovedMovieCard';

export default ImprovedMovieCard;