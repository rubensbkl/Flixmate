"use client";

import Header from '@/components/Header';
import Navbar from '@/components/Navbar';
import ProtectedRoute from '@/components/ProtectedRoute';
import Searchbar from '@/components/Searchbar';
import { useRouter } from 'next/navigation';
import { useEffect, useState, useCallback } from 'react';

// Debounce para evitar muitas requisições rápido
function useDebounce(value, delay) {
    const [debouncedValue, setDebouncedValue] = useState(value);
    useEffect(() => {
        const handler = setTimeout(() => setDebouncedValue(value), delay);
        return () => clearTimeout(handler);
    }, [value, delay]);
    return debouncedValue;
}

// Função de busca que você deve implementar para buscar da sua API/servidor
async function fetchMovies(query, page = 1, limit = 25) {
    // Exemplo: chame sua API passando query, page e limit para paginação
    // Retorne { results: [], total: number }
    const params = new URLSearchParams({ query, page, limit });
    const res = await fetch(`/api/movies/search?${params.toString()}`);
    if (!res.ok) throw new Error('Erro ao buscar filmes');
    return await res.json();
}

export default function SearchPage() {
    const [query, setQuery] = useState('');
    const [movies, setMovies] = useState([]);
    const [page, setPage] = useState(1);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [hasMore, setHasMore] = useState(false);

    // Debounce para search
    const debouncedQuery = useDebounce(query, 500);

    // Buscar filmes sempre que query ou página mudar
    useEffect(() => {
        if (!debouncedQuery.trim()) {
            setMovies([]);
            setHasMore(false);
            setPage(1);
            return;
        }

        const loadMovies = async () => {
            setLoading(true);
            setError(null);

            try {
                const data = await fetchMovies(debouncedQuery, page, 25);
                if (page === 1) {
                    setMovies(data.results);
                } else {
                    setMovies(prev => [...prev, ...data.results]);
                }
                setHasMore(data.results.length === 25); // se retornou 25, pode ter mais
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        loadMovies();
    }, [debouncedQuery, page]);

    // Resetar paginação ao mudar query
    useEffect(() => {
        setPage(1);
    }, [debouncedQuery]);

    // Quando muda o input do search bar
    const handleSearch = (newQuery) => {
        setQuery(newQuery);
    };

    // Carregar mais filmes
    const loadMore = () => {
        if (!loading && hasMore) setPage(prev => prev + 1);
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
                        <Searchbar onSearch={handleSearch} />
                    </section>

                    <section className="flex-1 p-4 overflow-auto">
                        {loading && page === 1 ? (
                            <div className="flex justify-center items-center h-full">
                                <div className="w-12 h-12 border-t-4 border-accent border-solid rounded-full animate-spin"></div>
                            </div>
                        ) : error ? (
                            <div className="text-red-500 text-center">{error}</div>
                        ) : movies.length === 0 ? (
                            <div className="text-center text-gray-500">Nenhum filme encontrado</div>
                        ) : (
                            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
                                {movies.map(movie => (
                                    <div key={movie.id} className="bg-foreground rounded-xl p-4 flex flex-col cursor-pointer hover:bg-background transition">
                                        {movie.poster_path ? (
                                            <img
                                                src={`https://image.tmdb.org/t/p/w300${movie.poster_path}`}
                                                alt={movie.title}
                                                className="rounded-md mb-2 object-cover"
                                            />
                                        ) : (
                                            <div className="w-full h-48 bg-gray-300 rounded-md mb-2 flex items-center justify-center text-gray-500">
                                                Sem imagem
                                            </div>
                                        )}
                                        <h3 className="font-medium">{movie.title}</h3>
                                        <p className="text-sm text-secondary">{movie.release_date?.slice(0,4)}</p>
                                    </div>
                                ))}
                            </div>
                        )}
                        {/* Botão carregar mais */}
                        {hasMore && !loading && (
                            <div className="flex justify-center mt-4">
                                <button
                                    onClick={loadMore}
                                    className="px-4 py-2 rounded bg-primary text-white hover:bg-primary-dark transition"
                                >
                                    Carregar mais
                                </button>
                            </div>
                        )}
                        {loading && page > 1 && (
                            <div className="flex justify-center mt-4">
                                <div className="w-8 h-8 border-t-4 border-accent border-solid rounded-full animate-spin"></div>
                            </div>
                        )}
                    </section>
                </main>
            </div>
        </ProtectedRoute>
    );
}
