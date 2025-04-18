"use client";

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import { UserIcon } from "@heroicons/react/24/outline";
import { 
    fetchUserProfile, 
    fetchUserFavorites, 
    fetchUserRecents, 
    fetchUserRecommended 
} from '@/lib/api';

export default function UserProfilePage() {
    const { userId } = useParams();
    
    // Estados separados para cada tipo de dados
    const [userInfo, setUserInfo] = useState(null);
    const [favoriteMovies, setFavoriteMovies] = useState([]);
    const [recentMovies, setRecentMovies] = useState([]);
    const [recommendedMovies, setRecommendedMovies] = useState([]);
    
    // Estados de carregamento para cada seção
    const [loadingUser, setLoadingUser] = useState(true);
    const [loadingFavorites, setLoadingFavorites] = useState(true);
    const [loadingRecents, setLoadingRecents] = useState(true);
    const [loadingRecommended, setLoadingRecommended] = useState(true);
    
    // Estado de erro geral
    const [error, setError] = useState(null);

    // Carregar informações básicas do usuário
    useEffect(() => {
        const loadUserInfo = async () => {
            try {
                setLoadingUser(true);
                const data = await fetchUserProfile(userId);
                console.log(data);
                setUserInfo(data);
            } catch (err) {
                console.error('Erro ao buscar informações do usuário:', err);
                setError('Não foi possível carregar as informações do usuário.');
            } finally {
                setLoadingUser(false);
            }
        };
        
        if (userId) {
            loadUserInfo();
        }
    }, [userId]);

    // Carregar filmes favoritos
    useEffect(() => {
        const loadFavorites = async () => {
            try {
                setLoadingFavorites(true);
                const movies = await fetchUserFavorites(userId);
                setFavoriteMovies(movies);
            } catch (err) {
                console.error('Erro ao buscar filmes favoritos:', err);
                // Não definimos erro geral aqui para não bloquear toda a página
            } finally {
                setLoadingFavorites(false);
            }
        };
        
        if (userId && userInfo) {
            loadFavorites();
        }
    }, [userId, userInfo]);

    // Carregar filmes recentes
    useEffect(() => {
        const loadRecents = async () => {
            try {
                setLoadingRecents(true);
                const movies = await fetchUserRecents(userId);
                setRecentMovies(movies);
            } catch (err) {
                console.error('Erro ao buscar filmes recentes:', err);
            } finally {
                setLoadingRecents(false);
            }
        };
        
        if (userId && userInfo) {
            loadRecents();
        }
    }, [userId, userInfo]);

    // Carregar filmes recomendados
    useEffect(() => {
        const loadRecommended = async () => {
            try {
                setLoadingRecommended(true);
                const movies = await fetchUserRecommended(userId);
                setRecommendedMovies(movies);
            } catch (err) {
                console.error('Erro ao buscar filmes recomendados:', err);
            } finally {
                setLoadingRecommended(false);
            }
        };
        
        if (userId && userInfo) {
            loadRecommended();
        }
    }, [userId, userInfo]);

    // Renderização durante o carregamento inicial do usuário
    if (loadingUser) {
        return (
            <div className="bg-gray-100 md:flex h-screen">
                <Navbar />
                <main className="flex-1 flex flex-col items-center justify-center h-full">
                    <div className="text-xl font-semibold mb-4">Carregando perfil...</div>
                    <div className="w-16 h-16 border-t-4 border-blue-500 border-solid rounded-full animate-spin"></div>
                </main>
            </div>
        );
    }

    // Erro ao carregar informações básicas do usuário
    if (error || !userInfo) {
        return (
            <div className="bg-gray-100 md:flex h-screen">
                <Navbar />
                <main className="flex-1 flex flex-col items-center justify-center h-full text-center p-4">
                    <div className="text-red-500 text-xl">{error || 'Usuário não encontrado'}</div>
                </main>
            </div>
        );
    }

    // Componente para exibir filmes com indicador de carregamento
    const MovieGrid = ({ movies, loading, emptyMessage }) => (
        <div>
            {loading ? (
                <div className="flex justify-center items-center py-8">
                    <div className="w-10 h-10 border-t-4 border-blue-500 border-solid rounded-full animate-spin"></div>
                </div>
            ) : movies.length === 0 ? (
                <p className="text-gray-500">{emptyMessage}</p>
            ) : (
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
                    {movies.map(movie => (
                        <div key={movie.id} className="rounded overflow-hidden shadow-md">
                            <img
                                src={`https://image.tmdb.org/t/p/w500${movie.poster_path}`}
                                alt={movie.title}
                                className="w-full h-40 object-cover"
                            />
                            <div className="p-2">
                                <p className="text-sm font-medium truncate">{movie.title}</p>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );

    // Renderização principal do perfil com carregamento progressivo
    return (
        <ProtectedRoute>
            <div className="bg-gray-100 md:flex h-screen">
                <Navbar />

                <main className="flex-1 overflow-y-auto h-full">
                    <div className="max-w-4xl mx-auto p-4 md:p-6">
                        {/* User Info Section */}
                        <section className="bg-white shadow-md rounded-lg p-6 mb-6">
                            <div className="flex items-center space-x-4">
                                <div className="h-20 w-20 bg-gray-200 rounded-full flex items-center justify-center overflow-hidden">
                                    {userInfo.profileImage ? (
                                        <img
                                            src={userInfo.profileImage}
                                            alt="Foto de perfil"
                                            className="h-full w-full object-cover"
                                        />
                                    ) : (
                                        <UserIcon className="h-10 w-10 text-gray-600" />
                                    )}
                                </div>
                                <div>
                                    <h1 className="text-2xl font-bold">
                                        {userInfo.firstName} {userInfo.lastName}
                                    </h1>
                                    <p className="text-gray-600">{userInfo.email}</p>
                                </div>
                            </div>
                        </section>

                        {/* Favorite Movies Section */}
                        <section className="bg-white shadow-md rounded-lg p-6 mb-6">
                            <h2 className="text-xl font-semibold mb-4">Filmes Favoritos</h2>
                            <MovieGrid 
                                movies={favoriteMovies} 
                                loading={loadingFavorites} 
                                emptyMessage="Este usuário ainda não tem filmes favoritos."
                            />
                        </section>

                        {/* Recent Movies Section */}
                        <section className="bg-white shadow-md rounded-lg p-6 mb-6">
                            <h2 className="text-xl font-semibold mb-4">Filmes Recentes</h2>
                            <MovieGrid 
                                movies={recentMovies} 
                                loading={loadingRecents} 
                                emptyMessage="Este usuário ainda não assistiu nenhum filme."
                            />
                        </section>
                        
                        {/* Recommended Movies Section */}
                        <section className="bg-white shadow-md rounded-lg p-6">
                            <h2 className="text-xl font-semibold mb-4">Filmes Recomendados</h2>
                            <MovieGrid 
                                movies={recommendedMovies} 
                                loading={loadingRecommended} 
                                emptyMessage="Ainda não há recomendações para este usuário."
                            />
                        </section>
                    </div>
                </main>
            </div>
        </ProtectedRoute>
    );
}