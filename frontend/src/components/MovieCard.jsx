import Link from 'next/link';

import Image from "next/image";
import { useState } from 'react';

export default function MovieCard({ movie, isMobile }) {
    // Configuração de tamanhos baseada no dispositivo
    const posterSize = isMobile ? "w-12 h-18" : "w-16 h-24";
    const padding = isMobile ? "p-2" : "p-4";
    const titleSize = isMobile ? "text-xs" : "text-lg";

    const [isLoading, setIsLoading] = useState(true);

    return (
        <div
            className={`flex items-center bg-foreground shadow-sm md:shadow-md rounded-lg ${padding} movie-card transition-all hover:bg-accent/10 hover:shadow-lg`}
        >
            <Link className="flex items-center flex-1 min-w-0 overflow-hidden group" href={`/movie/${movie.id}`}>
                {/* Poster do filme */}
                <div className={`${posterSize} relative rounded-md bg-background overflow-hidden`}>
                    {isLoading && (
                        <div className="absolute inset-0 bg-foreground animate-pulse rounded-md" />
                    )}
                    <Image
                        src={movie.poster_path ? `https://image.tmdb.org/t/p/original${movie.poster_path}` : "/placeholder.jpg"}
                        alt={movie.title}
                        fill
                        className={`object-cover rounded-md transition-opacity duration-500 ${isLoading ? 'opacity-0' : 'opacity-100'}`}
                        onLoadingComplete={() => setIsLoading(false)}
                        loading="lazy"
                    />
                </div>


                {/* Conteúdo do filme */}
                <div className="mx-2 md:mx-4 overflow-hidden">
                    <h2
                        className={`${titleSize} font-bold line-clamp-1 md:line-clamp-2 movie-title text-primary`}
                        title={movie.title}
                    >
                        {movie.title}
                    </h2>
                    <p className="text-xs text-secondary">
                        {movie.release_date
                            ? new Date(movie.release_date).getFullYear()
                            : "N/A"}
                    </p>
                    <p className="text-xs text-accent line-clamp-1 genre-list">
                        {movie.genres && movie.genres.length > 0
                            ? Array.isArray(movie.genres[0])
                                ? movie.genres.map((g) => g.name).join(", ")
                                : movie.genres.join(", ")
                            : movie.genre_ids?.length > 0
                                ? "Carregando gêneros..."
                                : "Sem gêneros"}
                    </p>
                </div>
            </Link>
        </div>
    );
}
