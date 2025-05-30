"use client";

import Header from '@/components/Header';
import Navbar from '@/components/Navbar';
import ProtectedRoute from '@/components/ProtectedRoute';
import Searchbar from '@/components/Searchbar';
import MovieCard from "@/components/MovieCard";
import { fetchMovies } from '@/lib/api';
import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';

// Debounce hook
function useDebounce(value, delay) {
    const [debouncedValue, setDebouncedValue] = useState(value);
    useEffect(() => {
        const handler = setTimeout(() => setDebouncedValue(value), delay);
        return () => clearTimeout(handler);
    }, [value, delay]);
    return debouncedValue;
}

export default function SearchPage() {
    const router = useRouter();
    const searchParams = useSearchParams();

    const [query, setQuery] = useState('');
    const [movies, setMovies] = useState([]);
    const [page, setPage] = useState(1);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [hasMore, setHasMore] = useState(false);

    const [totalPages, setTotalPages] = useState(1);
    const [totalResults, setTotalResults] = useState(0);

    const debouncedQuery = useDebounce(query, 500);

    const generatePageNumbers = () => {
        const pages = [];

        // Sempre mostra a primeira página
        pages.push(1);

        // Define um range dinâmico ao redor da página atual
        const start = Math.max(page - 1, 2);
        const end = Math.min(page + 1, totalPages - 1);

        for (let i = start; i <= end; i++) {
            pages.push(i);
        }

        // Sempre mostra a última página, se não for já exibida
        if (totalPages > 1) {
            pages.push(totalPages);
        }

        return [...new Set(pages)];
    };

    useEffect(() => {
        const paramQuery = searchParams.get('query') || '';
        const paramPage = parseInt(searchParams.get('page')) || 1;

        setQuery(paramQuery);
        setPage(paramPage);
    }, [searchParams]);

    useEffect(() => {
        if (!debouncedQuery.trim()) {
            setMovies([]);
            setTotalPages(1);
            setTotalResults(0);
            return;
        }

        const loadMovies = async () => {
            setLoading(true);
            setError(null);
            try {
                const data = await fetchMovies(debouncedQuery, page, 25);
                console.log(data);
                setMovies(data.results);
                setTotalPages(data.total_pages);
                setTotalResults(data.total_results);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        loadMovies();
    }, [debouncedQuery, page]);


    const handleSearch = (newQuery) => {
        const trimmed = newQuery.trim();
        const params = new URLSearchParams();

        if (trimmed) {
            params.set('query', trimmed);
            params.set('page', '1');
            router.push(`?${params.toString()}`);
        } else {
            router.push(`/movie/search`);
        }
    };

    const handlePageChange = (newPage) => {
        const params = new URLSearchParams(searchParams.toString());
        params.set('query', query);
        params.set('page', newPage);
        router.push(`?${params.toString()}`);
    };

    return (
        <ProtectedRoute>
            <div className="md:flex">
                <div className="md:w-64 md:min-h-screen">
                    <Navbar />
                </div>

                <main className="flex-1 overflow-auto flex flex-col h-[calc(100vh-4rem)] md:h-screen">
                    <Header />

                    <section className="flex justify-center items-center p-4">
                        <Searchbar onSearch={handleSearch} initialValue={query} placeholder="Buscar filme..."/>
                    </section>

                    <section className="flex-1 p-4 overflow-auto">
                        <div className="max-w-4xl mx-auto">
                            {loading && page === 1 ? (
                                <div className="flex justify-center items-center h-full">
                                    <div className="w-12 h-12 border-t-4 border-accent border-solid rounded-full animate-spin"></div>
                                </div>
                            ) : error ? (
                                <div className="text-red-500 text-center">{error}</div>
                            ) : movies.length === 0 ? (
                                <div className="text-center text-gray-500">Nenhum filme encontrado</div>
                            ) : (
                                <div className="flex flex-col gap-4">
                                    {movies.map(movie => (
                                        <MovieCard
                                            key={movie.id}
                                            movie={movie}
                                            isMobile={false}
                                        />
                                    ))}
                                </div>
                            )}

                            {movies.length > 0 && totalPages > 1 && (
                                <div className="flex justify-center mt-6 items-center gap-4">
                                    {/* Botão Página Anterior */}
                                    <button
                                        disabled={page === 1}
                                        onClick={() => handlePageChange(page - 1)}
                                        className={`p-2 rounded-full ${page === 1 ? 'text-secondary cursor-not-allowed' : 'text-primary hover:bg-primary-dark'
                                            }`}
                                    >
                                        &#8592;
                                    </button>

                                    {/* Lista de páginas */}
                                    <div className="flex gap-2">
                                        {generatePageNumbers().map((pageNumber, index) => (
                                            <button
                                                key={index}
                                                onClick={() => handlePageChange(pageNumber)}
                                                className={`px-3 py-1 rounded ${pageNumber === page
                                                        ? 'text-accent hover:bg-foreground'
                                                        : 'text-primary hover:bg-foreground'
                                                    }`}
                                            >
                                                {pageNumber}
                                            </button>
                                        ))}
                                    </div>

                                    {/* Botão Próxima Página */}
                                    <button
                                        disabled={page === totalPages}
                                        onClick={() => handlePageChange(page + 1)}
                                        className={`p-2 rounded-full ${page === totalPages ? 'text-secondary cursor-not-allowed' : 'text-primary hover:bg-primary-dark'
                                            }`}
                                    >
                                        &#8594;
                                    </button>
                                </div>
                            )}
                        </div>
                    </section>
                </main>
            </div>
        </ProtectedRoute>
    );
}
