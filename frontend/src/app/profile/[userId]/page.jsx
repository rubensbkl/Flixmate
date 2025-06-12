"use client";

import MovieGrid from "@/components/MovieGrid";
import Navbar from "@/components/Navbar";
import {
    fetchRecommendations,
    fetchUserFavorites,
    fetchUserProfile,
    fetchUserWatchList,
    verifyUser,
} from "@/lib/api";
import {
    BookmarkIcon,
    FilmIcon,
    StarIcon,
    UserIcon
} from "@heroicons/react/24/outline";
import {
    BookmarkIcon as BookmarkIconSolid,
    FilmIcon as FilmIconSolid,
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
    const [loadingCounts, setLoadingCounts] = useState(true); // Novo estado para loading dos contadores
    const [loadingContent, setLoadingContent] = useState(false); // Loading do conteúdo da aba ativa

    // Estados da UI
    const [activeTab, setActiveTab] = useState('recommended');
    const [error, setError] = useState(null);
    const [isMobile, setIsMobile] = useState(false);
    const [isCurrentUser, setIsCurrentUser] = useState(false); // State to track if the profile belongs to the logged-in user

    useEffect(() => {
        const checkMobile = () => {
            setIsMobile(window.innerWidth < 768);
        };

        checkMobile();
        window.addEventListener("resize", checkMobile);
        return () => window.removeEventListener("resize", checkMobile);
    }, []);

    // Carregar informações do usuário e CONTADORES
    useEffect(() => {
        const loadUserInfoAndCounts = async () => {
            try {
                setLoadingUser(true);
                setLoadingCounts(true);

                // Carregar perfil do usuário
                const userProfile = await fetchUserProfile(userId);
                setUserInfo(userProfile);

                // Carregar TODOS os dados em paralelo para os contadores
                const [recommendations, watchlist, favorites] = await Promise.all([
                    fetchRecommendations(userId).catch(err => {
                        console.error("Erro ao carregar recomendações:", err);
                        return [];
                    }),
                    fetchUserWatchList(userId).catch(err => {
                        console.error("Erro ao carregar watchlist:", err);
                        return [];
                    }),
                    fetchUserFavorites(userId).catch(err => {
                        console.error("Erro ao carregar favoritos:", err);
                        return [];
                    })
                ]);

                // Definir todos os dados de uma vez
                setRecommendedMovies(recommendations);
                setWatchLaterMovies(watchlist);
                setFavoriteMovies(favorites);

            } catch (err) {
                console.error("Erro ao buscar informações do usuário:", err);
                setError("Não foi possível carregar as informações do usuário.");
            } finally {
                setLoadingUser(false);
                setLoadingCounts(false);
            }
        };

        if (userId) {
            loadUserInfoAndCounts();
        }
    }, [userId]);

    // Verificar se o perfil pertence ao usuário logado
    useEffect(() => {
        const checkCurrentUser = async () => {
            try {
                const verifiedUser = await verifyUser();
                if (verifiedUser.valid && verifiedUser.user.id === parseInt(userId)) {
                    setIsCurrentUser(true);
                }
            } catch (err) {
                console.error("Erro ao verificar usuário:", err);
            }
        };

        checkCurrentUser();
    }, [userId]);

    // Agora não precisamos mais carregar dados quando a aba muda, apenas mostrar loading do conteúdo
    const handleTabChange = (newTab) => {
        if (newTab !== activeTab) {
            setLoadingContent(true);
            setActiveTab(newTab);

            // Simular um pequeno delay para smooth transition
            setTimeout(() => {
                setLoadingContent(false);
            }, 200);
        }
    };

    // Função para obter dados da aba ativa
    const getActiveTabData = () => {
        switch (activeTab) {
            case 'recommended':
                return recommendedMovies;
            case 'watchlater':
                return watchLaterMovies;
            case 'favorites':
                return favoriteMovies;
            default:
                return [];
        }
    };

    // Função para obter estatísticas (agora sempre atualizada)
    const getStats = () => ({
        recommended: recommendedMovies.length,
        watchlater: watchLaterMovies.length,
        favorites: favoriteMovies.length,
    });

    // Função para obter título da aba vazia
    const getEmptyMessage = () => {
        switch (activeTab) {
            case 'recommended':
                return 'Nenhum filme recomendado';
            case 'watchlater':
                return 'Nenhum filme para assistir depois';
            case 'favorites':
                return 'Nenhum filme favorito';
            default:
                return 'Nenhum filme encontrado';
        }
    };

    // Função para obter ícone da aba vazia
    const getEmptyIcon = () => {
        switch (activeTab) {
            case 'recommended':
                return FilmIcon;
            case 'watchlater':
                return BookmarkIcon;
            case 'favorites':
                return StarIcon;
            default:
                return FilmIcon;
        }
    };

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
    const activeMovies = getActiveTabData();

    return (
        <div className={`flex md:flex-row min-h-screen bg-background ${isMobile ? 'pb-20' : ''}`}>
            <div className="md:w-64 md:min-h-screen">
                <Navbar />
            </div>

            <main className="flex-1 overflow-y-auto overflow-x-hidden">
                <div className="max-w-4xl mx-auto px-4 md:px-6 py-6">
                    {/* Header do Perfil - Estilo Instagram */}
                    <div className="md:flex mb-4 justify-around items-center">
                        <div className="flex items-center">
                            {/* Foto de Perfil */}
                            <div className="h-20 w-20 ml-3 md:h-24 md:w-24 rounded-full bg-foreground flex items-center justify-center border-2 border-accent/20 overflow-hidden">
                                <UserIcon className="h-8 w-8 md:h-12 md:w-12 text-secondary" />
                            </div>

                            {/* Informações do Usuário */}
                            <div className="ml-6">
                                <h1 className="text-xl font-semibold text-primary mb-1">
                                    {userInfo.firstName} {userInfo.lastName}
                                </h1>
                                <p className="text-secondary text-sm">{userInfo.email}</p>
                            </div>
                        </div>

                        {/* Estatísticas */}
                        <div className="flex gap-8 justify-around mt-6 md:mt-0">
                            <div className="text-center">
                                <span className="block text-xl font-semibold text-primary">
                                    {loadingCounts ? (
                                        <div className="w-6 h-6 bg-foreground animate-pulse rounded mx-auto"></div>
                                    ) : (
                                        stats.recommended
                                    )}
                                </span>
                                <span className="text-sm text-secondary">recomendados</span>
                            </div>
                            <div className="text-center">
                                <span className="block text-xl font-semibold text-primary">
                                    {loadingCounts ? (
                                        <div className="w-6 h-6 bg-foreground animate-pulse rounded mx-auto"></div>
                                    ) : (
                                        stats.favorites
                                    )}
                                </span>
                                <span className="text-sm text-secondary">favoritos</span>
                            </div>
                            <div className="text-center">
                                <span className="block text-xl font-semibold text-primary">
                                    {loadingCounts ? (
                                        <div className="w-6 h-6 bg-foreground animate-pulse rounded mx-auto"></div>
                                    ) : (
                                        stats.watchlater
                                    )}
                                </span>
                                <span className="text-sm text-secondary">assistir depois</span>
                            </div>
                        </div>
                    </div>

                    {/* Botão de Editar Perfil */}
                    {isCurrentUser && (
                        <div className="text-center mb-6">
                            <button
                                onClick={() => window.location.href = "/profile/edit"}
                                className="px-4 py-2 md:px-6 md:py-3 bg-accent text-background text-sm md:text-base font-semibold rounded-lg md:rounded-full hover:bg-accent-dark transition-shadow shadow-sm md:shadow-md hover:shadow-lg"
                            >
                                Editar Perfil
                            </button>
                        </div>
                    )}

                    {/* Separador */}
                    <div className="border-t border-foreground"></div>

                    {/* Abas de Navegação - Estilo Instagram */}
                    <div className="flex justify-center border-b border-foreground mb-6 justify-around">
                        {/* Aba Recomendados */}
                        <button
                            onClick={() => handleTabChange('recommended')}
                            className={`flex items-center md:gap-2 py-3 transition-colors ${activeTab === 'recommended'
                                    ? 'text-accent border-b-2 border-accent'
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
                            onClick={() => handleTabChange('watchlater')}
                            className={`flex items-center md:gap-2 py-3 transition-colors ${activeTab === 'watchlater'
                                    ? 'text-accent border-b-2 border-accent'
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
                            onClick={() => handleTabChange('favorites')}
                            className={`flex items-center md:gap-2 py-3 transition-colors ${activeTab === 'favorites'
                                    ? 'text-accent border-b-2 border-accent'
                                    : 'text-secondary hover:text-primary'
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
                            loading={loadingContent || loadingCounts}
                            emptyMessage={getEmptyMessage()}
                            emptyIcon={getEmptyIcon()}
                        />
                    </div>
                </div>
            </main>
        </div>
    );
}