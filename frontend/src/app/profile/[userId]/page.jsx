"use client";

import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import MovieGrid from "@/components/MovieGrid";
import {
    fetchUserFavorites,
    fetchUserProfile,
    fetchUserWatchList,
    fetchRecommendations,
} from "@/lib/api";
import {
    UserIcon,
    FilmIcon,
    BookmarkIcon,
    StarIcon
} from "@heroicons/react/24/outline";
import {
    FilmIcon as FilmIconSolid,
    BookmarkIcon as BookmarkIconSolid,
    StarIcon as StarIconSolid
} from "@heroicons/react/24/solid";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function UserProfilePage() {
    const { userId } = useParams();

    // Estados dos dados
    const [userInfo, setUserInfo] = useState(null);
    const [favoriteMovies, setFavoriteMovies] = useState([]);
    const [watchLaterMovies, setWatchLaterMovies] = useState([]);
    const [recommendedMovies, setRecommendedMovies] = useState([]);

    // Estados de loading
    const [loadingUser, setLoadingUser] = useState(true);
    const [loadingFavorites, setLoadingFavorites] = useState(false);
    const [loadingWatchLater, setLoadingWatchLater] = useState(false);
    const [loadingRecommended, setLoadingRecommended] = useState(false);

    // Estados da UI
    const [activeTab, setActiveTab] = useState('recommended'); // 'recommended', 'watchlater', 'favorites'
    const [error, setError] = useState(null);

    // Carregar informações do usuário
    useEffect(() => {
        const loadUserInfo = async () => {
            try {
                setLoadingUser(true);
                const data = await fetchUserProfile(userId);
                setUserInfo(data);
            } catch (err) {
                console.error("Erro ao buscar informações do usuário:", err);
                setError("Não foi possível carregar as informações do usuário.");
            } finally {
                setLoadingUser(false);
            }
        };

        if (userId) {
            loadUserInfo();
        }
    }, [userId]);

    // Carregar dados da aba ativa
    useEffect(() => {
        if (!userInfo || !userId) return;

        const loadTabData = async () => {
            try {
                switch (activeTab) {
                    case 'recommended':
                        if (recommendedMovies.length === 0) {
                            setLoadingRecommended(true);
                            const movies = await fetchRecommendations(userId);
                            setRecommendedMovies(movies);
                        }
                        break;
                    case 'watchlater':
                        if (watchLaterMovies.length === 0) {
                            setLoadingWatchLater(true);
                            const movies = await fetchUserWatchList(userId);
                            setWatchLaterMovies(movies);
                        }
                        break;
                    case 'favorites':
                        if (favoriteMovies.length === 0) {
                            setLoadingFavorites(true);
                            const movies = await fetchUserFavorites(userId);
                            setFavoriteMovies(movies);
                        }
                        break;
                }
            } catch (err) {
                console.error(`Erro ao buscar ${activeTab}:`, err);
            } finally {
                setLoadingRecommended(false);
                setLoadingWatchLater(false);
                setLoadingFavorites(false);
            }
        };

        loadTabData();
    }, [activeTab, userInfo, userId]);

    // Função para obter dados e loading da aba ativa
    const getActiveTabData = () => {
        switch (activeTab) {
            case 'recommended':
                return { movies: recommendedMovies, loading: loadingRecommended };
            case 'watchlater':
                return { movies: watchLaterMovies, loading: loadingWatchLater };
            case 'favorites':
                return { movies: favoriteMovies, loading: loadingFavorites };
            default:
                return { movies: [], loading: false };
        }
    };

    const { movies: activeMovies, loading: activeLoading } = getActiveTabData();

    // Função para obter estatísticas
    const getStats = () => ({
        recommended: recommendedMovies.length,
        watchlater: watchLaterMovies.length,
        favorites: favoriteMovies.length,
    });

    if (loadingUser) {
        return (
            <div className="flex md:flex-row flex-col min-h-screen bg-background">
                <div className="md:w-64 md:min-h-screen">
                    <Navbar />
                </div>
                <main className="flex-1 flex items-center justify-center">
                    <div className="text-center">
                        <div className="w-16 h-16 border-t-4 border-accent border-solid rounded-full animate-spin mx-auto mb-4"></div>
                        <p className="text-xl font-medium text-primary">Carregando perfil...</p>
                    </div>
                </main>
            </div>
        );
    }

    if (error || !userInfo) {
        return (
            <div className="flex md:flex-row flex-col min-h-screen bg-background">
                <div className="md:w-64 md:min-h-screen">
                    <Navbar />
                </div>
                <main className="flex-1 flex items-center justify-center">
                    <div className="text-center">
                        <div className="bg-red-500/10 border border-red-500/50 p-4 rounded-lg mb-4">
                            <p className="text-red-400 font-medium">
                                {error || "Usuário não encontrado"}
                            </p>
                        </div>
                    </div>
                </main>
            </div>
        );
    }

    const stats = getStats();

    return (
        <ProtectedRoute>
            <div className="flex md:flex-row min-h-screen bg-background">
                <div className="md:w-64 md:min-h-screen">
                    <Navbar />
                </div>

                <main className="flex-1 overflow-x-hidden">
                    <div className="max-w-4xl mx-auto px-4 md:px-6 py-6">

                        {/* Header do Perfil - Estilo Instagram */}
                        <div className="md:flex mb-8 justify-around items-center">
                            <div className="flex items-center">

                                {/* Foto de Perfil */}
                                <div className="relative h-24 w-24">
                                    <div className="h-24 w-24 rounded-full bg-foreground flex items-center justify-center border-2 border-accent/20 overflow-hidden">
                                        {userInfo.profileImage ? (
                                            <img
                                                src={userInfo.profileImage}
                                                alt="Foto de perfil"
                                                className="h-full w-full object-cover rounded-full"
                                                style={{
                                                    borderRadius: '50%',
                                                    clipPath: 'circle(50%)'
                                                }}
                                            />
                                        ) : (
                                            <UserIcon className="h-12 w-12 text-secondary" />
                                        )}
                                    </div>
                                </div>

                                {/* Informações do Usuário */}
                                <div>
                                    <h1 className="text-2xl font-semibold text-primary mb-1">
                                        {userInfo.firstName} {userInfo.lastName}
                                    </h1>
                                    <p className="text-secondary">{userInfo.email}</p>
                                </div>
                            </div>



                            {/* Estatísticas */}
                            <div className="flex gap-8 justify-around">
                                <div className="text-center">
                                    <span className="block text-xl font-semibold text-primary">
                                        {stats.recommended}
                                    </span>
                                    <span className="text-sm text-secondary">recomendados</span>
                                </div>
                                <div className="text-center">
                                    <span className="block text-xl font-semibold text-primary">
                                        {stats.favorites}
                                    </span>
                                    <span className="text-sm text-secondary">favoritos</span>
                                </div>
                                <div className="text-center">
                                    <span className="block text-xl font-semibold text-primary">
                                        {stats.watchlater}
                                    </span>
                                    <span className="text-sm text-secondary">assistir depois</span>
                                </div>
                            </div>
                        </div>


                        {/* Separador */}
                        <div className="border-t border-foreground "></div>

                        {/* Abas de Navegação - Estilo Instagram */}
                        <div className="flex justify-center border-b border-foreground mb-6 justify-around">

                            {/* Aba Recomendados */}
                            <button
                                onClick={() => setActiveTab('recommended')}
                                className={`flex items-center md:gap-2 py-3 transition-colors ${activeTab === 'recommended'
                                    ? 'text-accent'
                                    : 'text-secondary hover:text-primary'
                                    }`}
                            >
                                {activeTab === 'recommended' ? (
                                    <FilmIconSolid className="h-6 w-6" />
                                ) : (
                                    <FilmIcon className="h-6 w-6" />
                                )}
                                <span className="font-medium text-sm uppercase tracking-wide hidden md:block">
                                    Recomendados
                                </span>
                            </button>

                            {/* Aba Assistir Depois */}
                            <button
                                onClick={() => setActiveTab('watchlater')}
                                className={`flex items-center md:gap-2 py-3  transition-colors ${activeTab === 'watchlater'
                                    ? ' text-accent'
                                    : 'text-secondary hover:text-primary'
                                    }`}
                            >
                                {activeTab === 'watchlater' ? (
                                    <BookmarkIconSolid className="h-6 w-6" />
                                ) : (
                                    <BookmarkIcon className="h-6 w-6" />
                                )}
                                <span className="font-medium text-sm uppercase tracking-wide hidden md:block">
                                    Assistir Depois
                                </span>
                            </button>

                            {/* Aba Favoritos */}
                            <button
                                onClick={() => setActiveTab('favorites')}
                                className={`flex items-center md:gap-2 py-3  transition-colors ${activeTab === 'favorites'
                                    ? ' text-accent'
                                    : ' text-secondary hover:text-primary'
                                    }`}
                            >
                                {activeTab === 'favorites' ? (
                                    <StarIconSolid className="h-6 w-6" />
                                ) : (
                                    <StarIcon className="h-6 w-6" />
                                )}
                                <span className="font-medium text-sm uppercase tracking-wide hidden md:block">
                                    Favoritos
                                </span>
                            </button>
                        </div>

                        {/* Conteúdo da Aba Ativa */}
                        <div className="min-h-[400px]">
                            <MovieGrid
                                movies={activeMovies}
                                loading={activeLoading}
                                emptyMessage={
                                    activeTab === 'recommended' ? 'Nenhum filme recomendado' :
                                        activeTab === 'watchlater' ? 'Nenhum filme para assistir depois' :
                                            'Nenhum filme favorito'
                                }
                                emptyIcon={
                                    activeTab === 'recommended' ? FilmIcon :
                                        activeTab === 'watchlater' ? BookmarkIcon :
                                            StarIcon
                                }
                            />
                        </div>
                    </div>
                </main>
            </div>
        </ProtectedRoute>
    );
}