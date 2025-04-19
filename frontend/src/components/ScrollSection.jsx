// src/components/ScrollSection.jsx

import { ChevronLeftIcon, ChevronRightIcon } from '@heroicons/react/24/outline';
import React, { useRef } from 'react';

const ScrollSection = ({ title, movies, loading, emptyMessage }) => {
  const scrollContainerRef = useRef(null);

  const scrollLeft = () => {
    if (scrollContainerRef.current) {
      scrollContainerRef.current.scrollBy({
        left: -200,
        behavior: 'smooth'
      });
    }
  };

  const scrollRight = () => {
    if (scrollContainerRef.current) {
      scrollContainerRef.current.scrollBy({
        left: 200,
        behavior: 'smooth'
      });
    }
  };

  return (
    <section className="mb-6 last:mb-20">
      <h2 className="text-lg font-semibold text-gray-800 mb-4">{title}</h2>
      {loading ? (
        <div className="flex items-center justify-center py-6">
          <div className="w-8 h-8 border-t-3 border-blue-500 border-solid rounded-full animate-spin"></div>
        </div>
      ) : movies.length === 0 ? (
        <p className="text-gray-500 py-2">{emptyMessage}</p>
      ) : (
        <div className="relative group">
          {/* Botão de rolagem para esquerda - posicionado no centro da IMAGEM */}
          <button 
            onClick={scrollLeft}
            className="absolute left-0 top-[30%] z-10 bg-white/80 rounded-full p-1 shadow-md opacity-0 group-hover:opacity-100 transition-opacity"
            aria-label="Rolar para esquerda"
          >
            <ChevronLeftIcon className="h-6 w-6 text-gray-700" />
          </button>
          
          {/* Container de rolagem sem botões visíveis por padrão */}
          <div 
            ref={scrollContainerRef}
            className="scrollbar-hide overflow-x-auto pb-4"
          >
            <div className="inline-flex gap-4">
              {movies.map(movie => (
                <div key={movie.id} className="w-28 md:w-32 flex-none">
                  <div className="bg-white rounded-lg overflow-hidden">
                    <div className="relative pb-[142%]">
                      <img
                        src={`https://image.tmdb.org/t/p/w500${movie.poster_path}`}
                        alt={movie.title}
                        className="absolute h-full w-full object-cover rounded-lg"
                      />
                    </div>
                  </div>
                  <div className="mt-1">
                    <h3 className="text-xs font-medium leading-tight line-clamp-2">{movie.title}</h3>
                  </div>
                </div>
              ))}
            </div>
          </div>
          
          {/* Botão de rolagem para direita - posicionado no centro da IMAGEM */}
          <button 
            onClick={scrollRight}
            className="absolute right-0 top-[30%] z-10 bg-white/80 rounded-full p-1 shadow-md opacity-0 group-hover:opacity-100 transition-opacity"
            aria-label="Rolar para direita"
          >
            <ChevronRightIcon className="h-6 w-6 text-gray-700" />
          </button>
        </div>
      )}
    </section>
  );
};

export default ScrollSection;