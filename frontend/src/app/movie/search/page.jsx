"use client";

import Header from '@/components/Header';
import Navbar from '@/components/Navbar';
import Searchbar from '@/components/Searchbar';
import MovieCard from "@/components/MovieCard";
import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import ProtectedRoute from '@/components/ProtectedRoute';

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

    // Função unificada para buscar filmes (populares ou por query)
    const fetchAllMovies = async (searchQuery = '', currentPage = 1) => {
        setLoading(true);
        setError(null);
        try {
            // Montar URL com parâmetros
            const params = new URLSearchParams();
            if (searchQuery.trim()) {
                params.set('query', searchQuery);
            }
            params.set('page', currentPage);
            params.set('limit', '25');

            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/movies/search?${params.toString()}`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(searchQuery.trim() ? 'Erro ao buscar filmes' : 'Erro ao carregar filmes populares');
            }

            const data = await response.json();
            setMovies(data.results);
            setTotalPages(data.total_pages);
            setTotalResults(data.total_results);
        } catch (err) {
            setError(err.message || 'Erro ao carregar filmes');
            console.error('Erro na busca:', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const paramQuery = searchParams.get('query') || '';
        const paramPage = parseInt(searchParams.get('page')) || 1;

        setQuery(paramQuery);
        setPage(paramPage);
    }, [searchParams]);

    useEffect(() => {
        // Usar a função unificada para buscar filmes
        fetchAllMovies(debouncedQuery, page);
    }, [debouncedQuery, page]);

    const handleSearch = (newQuery) => {
        const trimmed = newQuery.trim();
        const params = new URLSearchParams();

        if (trimmed) {
            params.set('query', trimmed);
            params.set('page', '1');
            router.push(`?${params.toString()}`);
        } else {
            // Se limpar a busca, remove os parâmetros para mostrar populares
            router.push(`/movie/search`);
        }
    };

    const handlePageChange = (newPage) => {
        const params = new URLSearchParams();

        if (query.trim()) {
            params.set('query', query);
        }
        params.set('page', newPage);

        const paramString = params.toString();
        router.push(paramString ? `?${paramString}` : `/movie/search?page=${newPage}`);
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
                        <Searchbar
                            onSearch={handleSearch}
                            initialValue={query}
                            placeholder="Buscar filme..."
                        />
                    </section>

                    <section className="flex-1 p-4 overflow-auto">
                        <div className="max-w-4xl mx-auto">
                            {/* Título da seção */}
                            <div className="mb-4">
                                <h2 className="text-xl font-semibold text-primary">
                                    {query.trim() ? `Resultados para "${query}"` : 'Filmes Populares'}
                                </h2>
                                {totalResults > 0 && (
                                    <p className="text-secondary text-sm">
                                        {totalResults} {totalResults === 1 ? 'resultado encontrado' : 'resultados encontrados'}
                                    </p>
                                )}
                            </div>

                            {loading && page === 1 ? (
                                <div className="flex justify-center items-center h-full">
                                    <div className="w-12 h-12 border-t-4 border-accent border-solid rounded-full animate-spin"></div>
                                </div>
                            ) : error ? (
                                <div className="text-red-400 text-center bg-red-500/10 border border-red-500/50 p-4 rounded-lg">
                                    {error}
                                </div>
                            ) : movies.length === 0 ? (
                                <div className="text-center text-secondary p-8">
                                    {query.trim() ? 'Nenhum filme encontrado para sua busca' : 'Nenhum filme disponível'}
                                </div>
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
                                        className={`p-2 rounded-full transition-colors ${page === 1
                                            ? 'text-secondary cursor-not-allowed'
                                            : 'text-primary hover:bg-foreground'
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
                                                className={`px-3 py-1 rounded transition-colors ${pageNumber === page
                                                    ? 'bg-accent text-background'
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
                                        className={`p-2 rounded-full transition-colors ${page === totalPages
                                            ? 'text-secondary cursor-not-allowed'
                                            : 'text-primary hover:bg-foreground'
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