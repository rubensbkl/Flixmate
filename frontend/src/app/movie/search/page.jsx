"use client";

import AdvancedFilters from '@/components/AdvancedFilters'; // Novo componente
import Header from '@/components/Header';
import MovieCard from "@/components/MovieCard";
import Navbar from '@/components/Navbar';
import Searchbar from '@/components/Searchbar';
import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';

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
    
    // Estados básicos
    const [query, setQuery] = useState('');
    const [movies, setMovies] = useState([]);
    const [page, setPage] = useState(1);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [isMounted, setIsMounted] = useState(false);
    
    // Estados para filtros avançados
    const [filters, setFilters] = useState({
        sortBy: 'popularity',
        genres: [],
        yearFrom: '',
        yearTo: ''
    });

    const [totalPages, setTotalPages] = useState(1);
    const [totalResults, setTotalResults] = useState(0);

    const debouncedQuery = useDebounce(query, 500);

    // Fix para SSR
    useEffect(() => {
        setIsMounted(true);
    }, []);

    const generatePageNumbers = () => {
        const pages = [];
        pages.push(1);

        const start = Math.max(page - 1, 2);
        const end = Math.min(page + 1, totalPages - 1);

        for (let i = start; i <= end; i++) {
            pages.push(i);
        }

        if (totalPages > 1) {
            pages.push(totalPages);
        }

        return [...new Set(pages)];
    };

    // Função unificada para buscar filmes (populares, por query ou com filtros)
    const fetchAllMovies = async (searchQuery = '', currentPage = 1, appliedFilters = filters) => {
        setLoading(true);
        setError(null);
        try {
            const params = new URLSearchParams();
            
            if (searchQuery.trim()) {
                params.set('query', searchQuery);
            }
            
            params.set('page', currentPage);
            params.set('limit', '25');
            
            // Adicionar filtros avançados
            if (appliedFilters.sortBy && appliedFilters.sortBy !== 'popularity') {
                params.set('sortBy', appliedFilters.sortBy);
            }
            
            if (appliedFilters.genres && appliedFilters.genres.length > 0) {
                params.set('genres', appliedFilters.genres.join(','));
            }
            
            if (appliedFilters.yearFrom) {
                params.set('yearFrom', appliedFilters.yearFrom);
            }
            
            if (appliedFilters.yearTo) {
                params.set('yearTo', appliedFilters.yearTo);
            }

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
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        // Só executar após hidratação
        if (!isMounted) return;
        
        const paramQuery = searchParams.get('query') || '';
        const paramPage = parseInt(searchParams.get('page')) || 1;
        
        // Recuperar filtros da URL
        const urlFilters = {
            sortBy: searchParams.get('sortBy') || 'popularity',
            genres: searchParams.get('genres') ? searchParams.get('genres').split(',').map(Number) : [],
            yearFrom: searchParams.get('yearFrom') || '',
            yearTo: searchParams.get('yearTo') || ''
        };

        setQuery(paramQuery);
        setPage(paramPage);
        setFilters(urlFilters);
    }, [searchParams, isMounted]);

    useEffect(() => {
        // Só executar após hidratação
        if (!isMounted) return;
        
        fetchAllMovies(debouncedQuery, page, filters);
    }, [debouncedQuery, page, filters, isMounted]);

    const handleSearch = (newQuery) => {
        const trimmed = newQuery.trim();
        updateURL(trimmed, 1, filters);
    };

    const handleApplyFilters = (newFilters) => {
        setFilters(newFilters);
        updateURL(query, 1, newFilters);
    };

    const updateURL = (searchQuery, newPage, appliedFilters) => {
        const params = new URLSearchParams();

        if (searchQuery.trim()) {
            params.set('query', searchQuery);
        }
        
        if (newPage > 1) {
            params.set('page', newPage);
        }
        
        if (appliedFilters.sortBy && appliedFilters.sortBy !== 'popularity') {
            params.set('sortBy', appliedFilters.sortBy);
        }
        
        if (appliedFilters.genres && appliedFilters.genres.length > 0) {
            params.set('genres', appliedFilters.genres.join(','));
        }
        
        if (appliedFilters.yearFrom) {
            params.set('yearFrom', appliedFilters.yearFrom);
        }
        
        if (appliedFilters.yearTo) {
            params.set('yearTo', appliedFilters.yearTo);
        }

        const paramString = params.toString();
        router.push(paramString ? `?${paramString}` : '/movie/search');
    };

    const handlePageChange = (newPage) => {
        updateURL(query, newPage, filters);
    };

    return (
        <div className="md:flex">
            <div className="md:w-64 md:min-h-screen">
                <Navbar />
            </div>

            <main className="flex-1 overflow-auto flex flex-col h-[calc(100vh-4rem)] md:h-screen">
                <Header />

                <section className="flex justify-center items-center p-4">
                    <div className="flex flex-col gap-4 items-center w-full max-w-4xl">
                        <div className="w-full">
                            <Searchbar
                                onSearch={handleSearch}
                                initialValue={query}
                                placeholder="Buscar filme..."
                            />
                        </div>
                        {isMounted && (
                            <AdvancedFilters
                                onApplyFilters={handleApplyFilters}
                                initialFilters={filters}
                            />
                        )}
                    </div>
                </section>

                <section className="flex-1 p-4 overflow-auto">
                    <div className="max-w-4xl mx-auto">
                        {/* Título da seção */}
                        <div className="mb-4">
                            <h2 className="text-xl font-semibold text-primary">
                                {query.trim() ? `Resultados para "${query}"` : 'Filmes'}
                            </h2>
                            {totalResults > 0 && (
                                <p className="text-secondary text-sm">
                                    {totalResults} {totalResults === 1 ? 'resultado encontrado' : 'resultados encontrados'}
                                </p>
                            )}
                            
                            {/* Mostrar filtros ativos */}
                            {(filters.genres.length > 0 || filters.yearFrom || filters.yearTo || filters.sortBy !== 'popularity') && (
                                <div className="flex flex-wrap gap-2 mt-2">
                                    {filters.sortBy !== 'popularity' && (
                                        <span className="px-2 py-1 bg-accent/20 text-accent rounded-full text-xs">
                                            Ordenado por: {filters.sortBy === 'rating' ? 'Avaliação' : 
                                                         filters.sortBy === 'release_date_desc' ? 'Mais recentes' :
                                                         filters.sortBy === 'release_date_asc' ? 'Mais antigos' :
                                                         filters.sortBy === 'title' ? 'Título' : filters.sortBy}
                                        </span>
                                    )}
                                    {filters.yearFrom && (
                                        <span className="px-2 py-1 bg-accent/20 text-accent rounded-full text-xs">
                                            A partir de: {filters.yearFrom}
                                        </span>
                                    )}
                                    {filters.yearTo && (
                                        <span className="px-2 py-1 bg-accent/20 text-accent rounded-full text-xs">
                                            Até: {filters.yearTo}
                                        </span>
                                    )}
                                    {filters.genres.length > 0 && (
                                        <span className="px-2 py-1 bg-accent/20 text-accent rounded-full text-xs">
                                            {filters.genres.length} gênero(s) selecionado(s)
                                        </span>
                                    )}
                                </div>
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
    );
}