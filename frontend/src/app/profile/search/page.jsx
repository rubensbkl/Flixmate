"use client";

import Header from '@/components/Header';
import Navbar from '@/components/Navbar';
import ProtectedRoute from '@/components/ProtectedRoute';
import Searchbar from '@/components/Searchbar';
import UserCard from '@/components/UserCard';
import { fetchUsers } from '@/lib/api';
import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';

// Debounce hook (pode mover para um arquivo utils futuramente)
function useDebounce(value, delay) {
    const [debouncedValue, setDebouncedValue] = useState(value);
    useEffect(() => {
        const handler = setTimeout(() => setDebouncedValue(value), delay);
        return () => clearTimeout(handler);
    }, [value, delay]);
    return debouncedValue;
}

export default function ProfileSearch() {
    const router = useRouter();
    const searchParams = useSearchParams();

    const [query, setQuery] = useState('');
    const [users, setUsers] = useState([]);
    const [filteredUsers, setFilteredUsers] = useState([]);
    const [page, setPage] = useState(1);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const [totalPages, setTotalPages] = useState(1);
    const itemsPerPage = 20;

    const debouncedQuery = useDebounce(query, 500);

    const generatePageNumbers = () => {
        const pages = [];
        pages.push(1);
        const start = Math.max(page - 1, 2);
        const end = Math.min(page + 1, totalPages - 1);
        for (let i = start; i <= end; i++) pages.push(i);
        if (totalPages > 1) pages.push(totalPages);
        return [...new Set(pages)];
    };

    useEffect(() => {
        const paramQuery = searchParams.get('query') || '';
        const paramPage = parseInt(searchParams.get('page')) || 1;
        setQuery(paramQuery);
        setPage(paramPage);
    }, [searchParams]);

    useEffect(() => {
        const loadUsers = async () => {
            setLoading(true);
            setError(null);
            try {
                const allUsers = await fetchUsers();
                setUsers(allUsers);

                // Filtro por nome, sobrenome ou email
                const filtered = debouncedQuery
                    ? allUsers.filter(user =>
                        `${user.firstName} ${user.lastName}`.toLowerCase().includes(debouncedQuery.toLowerCase()) ||
                        user.email.toLowerCase().includes(debouncedQuery.toLowerCase())
                    )
                    : allUsers;

                setFilteredUsers(filtered);

                const calculatedPages = Math.max(1, Math.ceil(filtered.length / itemsPerPage));
                setTotalPages(calculatedPages);
                if (page > calculatedPages) {
                    setPage(1);
                    const params = new URLSearchParams();
                    if (debouncedQuery) params.set('query', debouncedQuery);
                    params.set('page', '1');
                    router.push(`?${params.toString()}`);
                }
            } catch (err) {
                console.error(err);
                setError('Erro ao carregar usuários');
            } finally {
                setLoading(false);
            }
        };

        loadUsers();
    }, [debouncedQuery, page]);

    const handleSearch = (newQuery) => {
        const trimmed = newQuery.trim();
        const params = new URLSearchParams();
        if (trimmed) {
            params.set('query', trimmed);
            params.set('page', '1');
            router.push(`?${params.toString()}`);
        } else {
            router.push(`/profile/search`);
        }
    };

    const handlePageChange = (newPage) => {
        const params = new URLSearchParams(searchParams.toString());
        params.set('query', query);
        params.set('page', newPage);
        router.push(`?${params.toString()}`);
    };

    // Paginação dos usuários filtrados
    const startIndex = (page - 1) * itemsPerPage;
    const paginatedUsers = filteredUsers.slice(startIndex, startIndex + itemsPerPage);

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
                            placeholder="Buscar perfil..."
                        />
                    </section>

                    <section className="flex-1 p-4 overflow-auto">
                        <div className="max-w-4xl mx-auto">
                            {loading && page === 1 ? (
                                <div className="flex justify-center items-center h-full">
                                    <div className="w-12 h-12 border-t-4 border-accent border-solid rounded-full animate-spin"></div>
                                </div>
                            ) : error ? (
                                <div className="text-red-500 text-center">{error}</div>
                            ) : paginatedUsers.length === 0 ? (
                                <div className="text-center text-gray-500">Nenhum usuário encontrado</div>
                            ) : (
                                <div className="flex flex-col gap-4">
                                    {paginatedUsers.map(user => (
                                        <UserCard key={user.id} user={user} />
                                    ))}
                                </div>
                            )}

                            {filteredUsers.length > itemsPerPage && (
                                <div className="flex justify-center mt-6 items-center gap-4">
                                    <button
                                        disabled={page === 1}
                                        onClick={() => handlePageChange(page - 1)}
                                        className={`p-2 rounded-full ${page === 1 ? 'text-secondary cursor-not-allowed' : 'text-primary hover:bg-primary-dark'
                                            }`}
                                    >
                                        &#8592;
                                    </button>

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
