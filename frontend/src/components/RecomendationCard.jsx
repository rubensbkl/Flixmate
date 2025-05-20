// components/MovieCard.jsx
import {
    ClockIcon as ClockOutline,
    StarIcon as StarOutline,
    TrashIcon as TrashOutline,
} from "@heroicons/react/24/outline";
import React from 'react';
import Link from "next/link";

import {
    ClockIcon as ClockSolid,
    StarIcon as StarSolid
} from "@heroicons/react/24/solid";

export default function MovieCard({ movie, activeIcons, toggleIcon, isMobile }) {
    // Configuração de tamanhos baseada no dispositivo
    const posterSize = isMobile ? "w-12 h-18" : "w-16 h-24";
    const iconSize = isMobile ? "w-5 h-5" : "w-5 h-5";
    const spacing = isMobile ? "space-x-1" : "space-x-2";
    const padding = isMobile ? "p-2" : "p-4";
    const titleSize = isMobile ? "text-xs" : "text-lg";

    return (
        <Link href={`/movie/${movie.id}`}
            className={`flex items-center bg-foreground shadow-sm md:shadow-md rounded-lg ${padding} movie-card`}
        >
            {/* Poster do filme */}
            <img
                src={movie.poster_path
                    ? `https://image.tmdb.org/t/p/w200${movie.poster_path}`
                    : '/placeholder.jpg'}
                alt={movie.title}
                className={`${posterSize} rounded-md object-cover flex-shrink-0 movie-poster`}
                loading="lazy"
            />

            {/* Conteúdo do filme */}
            <div className="flex-1 min-w-0 mx-2 md:mx-4 overflow-hidden">
                <h2 className={`${titleSize} font-bold line-clamp-1 md:line-clamp-2 movie-title text-primary`} title={movie.title}>
                    {movie.title}
                </h2>
                <p className="text-xs text-secondary">
                    {movie.release_date ? new Date(movie.release_date).getFullYear() : 'N/A'}
                </p>
                <p className="text-xs text-accent line-clamp-1 genre-list">
                    {movie.genres && movie.genres.length > 0
                        ? Array.isArray(movie.genres[0])
                            ? movie.genres.map(g => g.name).join(', ')
                            : movie.genres.join(', ')
                        : movie.genre_ids?.length > 0
                            ? 'Carregando gêneros...'
                            : 'Sem gêneros'}
                </p>
            </div>

            {/* Botões de ação */}
            <div className={`flex items-center ${spacing} flex-shrink-0 action-buttons`}>
                {/* Botão Watchlist (Clock) */}
                <button
                    className={`p-1 rounded-full action-button ${activeIcons[movie.id]?.clock
                        ? "text-blue-600 bg-blue-100 action-button-active"
                        : "text-gray-500 hover:bg-gray-200"
                        }`}
                    onClick={() => toggleIcon(movie.id, "clock")}
                    aria-label={activeIcons[movie.id]?.clock ? "Remover da watchlist" : "Adicionar à watchlist"}
                >
                    {activeIcons[movie.id]?.clock ? (
                        <ClockSolid className={iconSize} />
                    ) : (
                        <ClockOutline className={iconSize} />
                    )}
                </button>

                {/* Botão Favorito (Star) */}
                <button
                    className={`p-1 rounded-full action-button ${activeIcons[movie.id]?.star
                        ? "text-yellow-400 bg-yellow-100 action-button-active"
                        : "text-gray-500 hover:bg-gray-200"
                        }`}
                    onClick={() => toggleIcon(movie.id, "star")}
                    aria-label={activeIcons[movie.id]?.star ? "Remover dos favoritos" : "Adicionar aos favoritos"}
                >
                    {activeIcons[movie.id]?.star ? (
                        <StarSolid className={iconSize} />
                    ) : (
                        <StarOutline className={iconSize} />
                    )}
                </button>

                {/* Botão Deletar (Trash) */}
                <button
                    className={`p-1 rounded-full action-button ${activeIcons[movie.id]?.trash
                        ? "text-red-600 bg-red-100 action-button-active"
                        : "text-gray-500 hover:bg-gray-200"
                        }`}
                    onClick={() => toggleIcon(movie.id, "trash")}
                    aria-label="Deletar recomendação"
                >
                    <TrashOutline className={iconSize} />
                </button>
            </div>
        </Link>
    );
}