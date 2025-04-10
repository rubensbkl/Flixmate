'use client';

import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useSwipeable } from 'react-swipeable';
import TinderCard from 'react-tinder-card';

// Dados simulados de filmes
const initialMovies = [
  {
    id: 1,
    title: 'Cars',
    studio: 'Disney/Pixar',
    description: 'Um carro de corrida que aprende que na vida não é só sobre chegar em primeiro lugar.',
    posterUrl: 'https://image.tmdb.org/t/p/w500/abW5AzHDaIK1n9C36VdAeOwORRA.jpg',
    backdropImage: 'https://image.tmdb.org/t/p/w500/abW5AzHDaIK1n9C36VdAeOwORRA.jpg'
  },
  {
    id: 2,
    title: 'Toy Story',
    studio: 'Disney/Pixar',
    description: 'Os brinquedos ganham vida quando os humanos não estão olhando.',
    posterUrl: 'https://image.tmdb.org/t/p/w500/7G9915LfUQ2lVfwMEEhDsn3kT4B.jpg',
    backdropImage: 'https://image.tmdb.org/t/p/w500/7G9915LfUQ2lVfwMEEhDsn3kT4B.jpg'
  },
  {
    id: 3,
    title: 'Up: Altas Aventuras',
    studio: 'Disney/Pixar',
    description: 'Um idoso embarca em uma aventura para cumprir uma promessa à sua falecida esposa.',
    posterUrl: 'https://image.tmdb.org/t/p/w500/tloHMqUlGfkHGkXhg7b1nSH46HC.jpg',
    backdropImage: 'https://image.tmdb.org/t/p/w500/tloHMqUlGfkHGkXhg7b1nSH46HC.jpg'
  },
  {
    id: 4,
    title: 'Ratatouille',
    studio: 'Disney/Pixar',
    description: 'Um rato que sonha em se tornar um chef de cozinha em Paris.',
    posterUrl: 'https://image.tmdb.org/t/p/w500/lpyqGZXYc4lTRbf7KE474KPkIxV.jpg',
    backdropImage: 'https://image.tmdb.org/t/p/w500/lpyqGZXYc4lTRbf7KE474KPkIxV.jpg'
  },
  {
    id: 5,
    title: 'WALL-E',
    studio: 'Disney/Pixar',
    description: 'Um robô solitário na Terra encontra um novo propósito quando conhece outro robô.',
    posterUrl: 'https://image.tmdb.org/t/p/w500/hbhFnRzzg6ZDmm8YAmxBnQpQIPh.jpg',
    backdropImage: 'https://image.tmdb.org/t/p/w500/hbhFnRzzg6ZDmm8YAmxBnQpQIPh.jpg'
  }
];

export default function MovieSwipeApp() {
  const [movies, setMovies] = useState(initialMovies);
  const [currentIndex, setCurrentIndex] = useState(movies.length - 1);
  const [ratedCount, setRatedCount] = useState(0);
  const [likedMovies, setLikedMovies] = useState([]);
  const [dislikedMovies, setDislikedMovies] = useState([]);
  const [isPreloading, setIsPreloading] = useState(true);
  const currentIndexRef = useRef(currentIndex);
  const canSwipe = currentIndex >= 0;

  // Pré-carregamento de imagens para evitar atrasos
  useEffect(() => {
    const preloadImages = () => {
      const imagePromises = movies.map((movie) => {
        return new Promise((resolve, reject) => {
          const img = new Image();
          img.src = movie.posterUrl;
          img.onload = resolve;
          img.onerror = reject;
        });
      });
      
      Promise.all(imagePromises)
        .then(() => setIsPreloading(false))
        .catch(() => setIsPreloading(false));
    };
    
    preloadImages();
  }, [movies]);

  // Criar referências para os cartões
  const childRefs = useMemo(
    () =>
      Array(movies.length)
        .fill(0)
        .map(() => React.createRef()),
    [movies.length]
  );

  const updateCurrentIndex = (val) => {
    setCurrentIndex(val);
    currentIndexRef.current = val;
  };

  const canGoBack = currentIndex < movies.length - 1;

  // Função para quando um cartão é deslizado
  const swiped = (direction, movieId, index) => {
    const movie = movies.find(m => m.id === movieId);
    
    if (direction === 'right') {
      setLikedMovies(prev => [...prev, movie]);
    } else if (direction === 'left') {
      setDislikedMovies(prev => [...prev, movie]);
    }
    
    setRatedCount(prev => prev + 1);
    updateCurrentIndex(index - 1);
  };

  const outOfFrame = (idx) => {
    // Função chamada quando o cartão sai da tela
  };

  // Função para deslizar o cartão programaticamente
  const swipe = async (dir) => {
    if (canSwipe && currentIndex >= 0) {
      await childRefs[currentIndex].current.swipe(dir);
    }
  };

  // Função para voltar ao cartão anterior
  const goBack = async () => {
    if (!canGoBack) return;
    
    const newIndex = currentIndex + 1;
    updateCurrentIndex(newIndex);
    
    // Ajustar contagem e listas
    setRatedCount(prev => prev - 1);
    
    const prevMovie = movies[newIndex];
    if (likedMovies.some(m => m.id === prevMovie.id)) {
      setLikedMovies(prev => prev.filter(m => m.id !== prevMovie.id));
    } else if (dislikedMovies.some(m => m.id === prevMovie.id)) {
      setDislikedMovies(prev => prev.filter(m => m.id !== prevMovie.id));
    }
  };

  // Configuração para detecção de deslize via mouse/touch
  const handleSwipe = useSwipeable({
    onSwipedLeft: () => swipe('left'),
    onSwipedRight: () => swipe('right'),
    preventDefaultTouchmoveEvent: true,
    trackMouse: true
  });

  // Calcular progresso
  const progress = (ratedCount / movies.length) * 100;

  if (isPreloading) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-900">
        <div className="text-white text-xl">Carregando filmes...</div>
      </div>
    );
  }

  return (
    <div className="h-screen bg-gray-900 overflow-hidden flex flex-col pb-16 md:pb-0">
      {/* Barra de progresso */}
      <div className="w-full h-2 bg-gray-700">
        <div 
          className="h-full bg-red-500 transition-all duration-300 ease-out"
          style={{ width: `${progress}%` }}
        />
      </div>
      
      <div className="flex-1 relative overflow-hidden" {...handleSwipe}>
        {movies.map((movie, index) => (
          <TinderCard
            ref={childRefs[index]}
            className="absolute w-full h-full flex items-center justify-center"
            key={movie.id}
            onSwipe={(dir) => swiped(dir, movie.id, index)}
            onCardLeftScreen={() => outOfFrame(index)}
            preventSwipe={['up', 'down']}
          >
            <div 
              className="relative w-full max-w-md h-5/6 rounded-xl overflow-hidden shadow-xl bg-white cursor-grab active:cursor-grabbing"
              style={{ touchAction: 'none' }}
            >
              <img 
                src={movie.posterUrl} 
                alt={movie.title} 
                className="w-full h-full object-cover"
                draggable="false"
              />
              <div className="absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-black to-transparent">
                <h2 className="text-xl font-bold text-white">{movie.title}</h2>
                <h3 className="text-sm text-gray-300">{movie.studio}</h3>
                <p className="text-white text-sm mt-2">{movie.description}</p>
              </div>
            </div>
          </TinderCard>
        ))}
        
        {/* Estado: sem mais filmes */}
        {currentIndex < 0 && (
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="bg-white p-6 rounded-lg shadow-xl text-center max-w-sm">
              <h2 className="text-2xl font-bold mb-4">Sem mais filmes!</h2>
              <p className="mb-4">Você avaliou todos os filmes disponíveis.</p>
              <p className="mb-6">
                <span className="font-bold">Gostou:</span> {likedMovies.length} filmes<br />
                <span className="font-bold">Não gostou:</span> {dislikedMovies.length} filmes
              </p>
              <button 
                onClick={() => {
                  setMovies(initialMovies);
                  setCurrentIndex(initialMovies.length - 1);
                  setRatedCount(0);
                  setLikedMovies([]);
                  setDislikedMovies([]);
                }}
                className="bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition-colors"
              >
                Recomeçar
              </button>
            </div>
          </div>
        )}
      </div>
      
      {/* Botões de ação */}
      <div className="p-4 flex justify-center space-x-6 bg-gray-800">
        <button 
          onClick={() => swipe('left')}
          className="w-16 h-16 rounded-full bg-white flex items-center justify-center shadow-lg hover:bg-gray-200 transition-colors disabled:opacity-50"
          disabled={!canSwipe}
          aria-label="Não gostei"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
        
        <button 
          onClick={goBack}
          className="w-16 h-16 rounded-full bg-white flex items-center justify-center shadow-lg hover:bg-gray-200 transition-colors disabled:opacity-50"
          disabled={!canGoBack}
          aria-label="Voltar"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-yellow-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
        </button>
        
        <button 
          onClick={() => swipe('right')}
          className="w-16 h-16 rounded-full bg-white flex items-center justify-center shadow-lg hover:bg-gray-200 transition-colors disabled:opacity-50"
          disabled={!canSwipe}
          aria-label="Gostei"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        </button>
      </div>
    </div>
  );

}