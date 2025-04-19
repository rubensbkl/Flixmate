// components/MovieMatchModal.jsx
"use client";

import { HeartIcon, XMarkIcon } from "@heroicons/react/24/solid";
import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useState } from "react";

const MovieMatchModal = ({ isOpen, onClose, movie, onNavigate }) => {
  const [animationComplete, setAnimationComplete] = useState(false);
  const [posterUrl, setPosterUrl] = useState('');

  useEffect(() => {
    if (isOpen && movie) {
      // Reset animation state when modal opens
      setAnimationComplete(false);

      // Handle poster path - check different possible property names
      if (movie.posterPath) {
        setPosterUrl(`https://image.tmdb.org/t/p/w500${movie.posterPath}`);
      } else if (movie.poster_path) {
        setPosterUrl(`https://image.tmdb.org/t/p/w500${movie.poster_path}`);
      } else {
        setPosterUrl('');
      }

      // Auto-transition to movie details after animation
      const timer = setTimeout(() => {
        setAnimationComplete(true);
      }, 2000);

      return () => clearTimeout(timer);
    }
  }, [isOpen, movie]);

  if (!movie) return null;

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black bg-opacity-80"
        >
          <motion.div
            initial={{ scale: 0.8, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.8, opacity: 0 }}
            className="relative w-full max-w-sm md:max-w-lg lg:max-w-xl rounded-xl overflow-hidden bg-black"
          >
            <button
              onClick={onClose}
              className="absolute right-2 top-2 z-10 w-8 h-8 flex items-center justify-center rounded-full bg-black bg-opacity-50 text-white hover:bg-opacity-70"
            >
              <XMarkIcon className="w-5 h-5" />
            </button>

            <AnimatePresence mode="wait">
              {!animationComplete ? (
                // Match animation screen
                <motion.div
                  key="match-animation"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                  className="p-8 flex flex-col items-center justify-center h-[500px] md:h-[600px]"
                >
                  <motion.div
                    initial={{ scale: 0.5, rotate: -10 }}
                    animate={{ scale: 1.2, rotate: 0, y: -20 }}
                    transition={{ duration: 0.5, type: "spring" }}
                  >
                    <HeartIcon className="w-24 h-24 text-yellow-400" />
                  </motion.div>

                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.3 }}
                    className="text-center"
                  >
                    <h2 className="text-yellow-400 font-bold text-4xl mb-2 tracking-tight">IT'S A MATCH!</h2>
                    <p className="text-white text-xl">We found the perfect movie for you!</p>
                  </motion.div>
                </motion.div>
              ) : (
                // Movie details screen
                <motion.div
                  key="movie-details"
                  initial={{ opacity: 0, x: 100 }}
                  animate={{ opacity: 1, x: 0 }}
                  className="flex flex-col"
                >
                  {/* Movie poster - taller on desktop */}
                  <div className="relative w-full h-72 md:h-96 lg:h-[450px]">
                    {posterUrl ? (
                      <div
                        className="w-full h-full bg-cover bg-center"
                        style={{
                          backgroundImage: `url(${posterUrl})`
                        }}
                      >
                        <div className="absolute inset-0 bg-gradient-to-t from-black to-transparent" />
                      </div>
                    ) : (
                      <div className="w-full h-full bg-gray-800 flex items-center justify-center">
                        <p className="text-gray-400">No poster available</p>
                        <div className="absolute inset-0 bg-gradient-to-t from-black to-transparent" />
                      </div>
                    )}
                  </div>

                  {/* Movie info */}
                  <div className="p-6 bg-black text-white">
                    <h2 className="text-2xl font-bold mb-2">{movie.title || movie.name || 'Filme Recomendado'}</h2>

                    <div className="flex items-center mb-4">
                      <div className="mr-4 flex items-center">
                        <span className="text-yellow-400 mr-1">★</span>
                        <span>{
                          (movie.voteAverage || movie.vote_average)
                            ? Number(movie.voteAverage || movie.vote_average).toFixed(1)
                            : "N/A"
                        }</span>
                      </div>
                      <div className="text-gray-400">{
                        (movie.releaseDate || movie.release_date)
                          ? (movie.releaseDate || movie.release_date).substring(0, 4)
                          : "N/A"
                      }</div>
                    </div>

                    <p className="text-gray-300 mb-4 md:text-base line-clamp-3 md:line-clamp-4">
                      {movie.overview || movie.sinopse || movie.description || 'Sem descrição disponível.'}
                    </p>

                    <div className="flex items-center justify-between mt-4">
                      <button
                        onClick={onNavigate}
                        className="w-full py-3 bg-black border border-yellow-400 text-white hover:bg-gray-900 font-bold rounded-lg transition"
                      >
                        Ir para as recomendações
                      </button>
                    </div>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default MovieMatchModal;