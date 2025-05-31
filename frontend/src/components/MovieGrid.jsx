
import Link from 'next/link';
import Image from "next/image";
import { useState } from 'react';

function MovieGridItem({ movie }) {
    const [isLoading, setIsLoading] = useState(true);

    return (
        <Link href={`/movie/${movie.id}`} className="group">
            <div className="aspect-[2/3] relative rounded-lg overflow-hidden bg-foreground shadow-md hover:shadow-lg transition-all duration-300 group-hover:scale-105">
                {/* Loading placeholder */}
                {isLoading && (
                    <div className="absolute inset-0 bg-foreground animate-pulse" />
                )}
                
                {/* Poster do filme */}
                <Image
                    src={movie.poster_path ? `https://image.tmdb.org/t/p/original${movie.poster_path}` : "/placeholder.jpg"}
                    alt={movie.title}
                    fill
                    className={`object-cover transition-all duration-500 ${
                        isLoading ? 'opacity-0' : 'opacity-100'
                    } group-hover:opacity-90`}
                    onLoadingComplete={() => setIsLoading(false)}
                />
                
                {/* Overlay com informações */}
                <div className="absolute inset-0 bg-gradient-to-t from-background/80 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                    <div className="absolute bottom-0 left-0 right-0 p-3">
                        <h3 className="text-primary font-semibold text-sm line-clamp-2 mb-1">
                            {movie.title}
                        </h3>
                        <p className="text-secondary text-xs">
                            {movie.release_date ? new Date(movie.release_date).getFullYear() : "N/A"}
                        </p>
                        {movie.genres && movie.genres.length > 0 && (
                            <p className="text-accent text-xs line-clamp-1 mt-1">
                                {Array.isArray(movie.genres[0])
                                    ? movie.genres.map((g) => g.name).join(", ")
                                    : movie.genres.join(", ")
                                }
                            </p>
                        )}
                    </div>
                </div>
            </div>
        </Link>
    );
}

export default function MovieGrid({ movies, loading, emptyMessage, emptyIcon: EmptyIcon }) {
    if (loading) {
        return (
            <div className="grid grid-cols-3 md:grid-cols-4 gap-4">
                {/* Skeleton loading */}
                {Array.from({ length: 6 }).map((_, index) => (
                    <div key={index} className="aspect-[2/3] bg-foreground animate-pulse rounded-lg" />
                ))}
            </div>
        );
    }

    if (!movies || movies.length === 0) {
        return (
            <div className="text-center py-20">
                {EmptyIcon && (
                    <EmptyIcon className="h-16 w-16 text-secondary mx-auto mb-4" />
                )}
                <p className="text-primary text-lg font-medium mb-2">
                    {emptyMessage || 'Nenhum filme encontrado'}
                </p>
                <p className="text-secondary">
                    Que tal descobrir alguns filmes incríveis?
                </p>
            </div>
        );
    }

    return (
        <div className="grid grid-cols-3 md:grid-cols-4 gap-4">
            {movies.map((movie) => (
                <MovieGridItem key={movie.id} movie={movie} />
            ))}
        </div>
    );
}