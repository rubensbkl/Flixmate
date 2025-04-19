"use client";

import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import ScrollSection from "@/components/ScrollSection";
import {
    fetchMe,
    fetchUserFavorites,
    fetchUserRecents,
    fetchUserRecommended,
} from "@/lib/api";
import { PencilSquareIcon, UserIcon } from "@heroicons/react/24/outline";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function MyProfilePage() {
    // Estados para os dados do usuário
    const [userInfo, setUserInfo] = useState(null);
    const [favoriteMovies, setFavoriteMovies] = useState([]);
    const [recentMovies, setRecentMovies] = useState([]);
    const [recommendedMovies, setRecommendedMovies] = useState([]);

    // Estados de carregamento
    const [loadingUser, setLoadingUser] = useState(true);
    const [loadingFavorites, setLoadingFavorites] = useState(true);
    const [loadingRecents, setLoadingRecents] = useState(true);
    const [loadingRecommended, setLoadingRecommended] = useState(true);

    // Estado de erro
    const [error, setError] = useState(null);

    const router = useRouter();

    // Carregar informações do usuário
    useEffect(() => {
        const loadUserInfo = async () => {
            try {
                setLoadingUser(true);
                const data = await fetchMe();
                setUserInfo(data);
            } catch (err) {
                console.error("Erro ao buscar informações do usuário:", err);
                setError(
                    "Não foi possível carregar as informações do usuário."
                );
            } finally {
                setLoadingUser(false);
            }
        };

        loadUserInfo();
    }, []);

    // Carregar filmes favoritos
    useEffect(() => {
        const loadFavorites = async () => {
            try {
                setLoadingFavorites(true);
                const movies = await fetchUserFavorites(userInfo.id);
                setFavoriteMovies(movies);
            } catch (err) {
                console.error("Erro ao buscar filmes favoritos:", err);
            } finally {
                setLoadingFavorites(false);
            }
        };

        if (userInfo) {
            loadFavorites();
        }
    }, [userInfo]);

    // Carregar filmes recentes
    useEffect(() => {
        const loadRecents = async () => {
            try {
                setLoadingRecents(true);
                const movies = await fetchUserRecents(userInfo.id);
                setRecentMovies(movies);
            } catch (err) {
                console.error("Erro ao buscar filmes recentes:", err);
            } finally {
                setLoadingRecents(false);
            }
        };

        if (userInfo) {
            loadRecents();
        }
    }, [userInfo]);

    // Carregar filmes recomendados
    useEffect(() => {
        const loadRecommended = async () => {
            try {
                setLoadingRecommended(true);
                const movies = await fetchUserRecommended(userInfo.id);
                setRecommendedMovies(movies);
            } catch (err) {
                console.error("Erro ao buscar filmes recomendados:", err);
            } finally {
                setLoadingRecommended(false);
            }
        };

        if (userInfo) {
            loadRecommended();
        }
    }, [userInfo]);

    // Componente para exibir o loading do perfil
    if (loadingUser) {
        return (
            <div className="flex md:flex-row flex-col min-h-screen">
                <div className="md:w-64 md:min-h-screen">
                    <Navbar />
                </div>
                <main className="flex-1 flex items-center justify-center">
                    <div className="text-center">
                        <div className="w-16 h-16 border-t-4 border-blue-500 border-solid rounded-full animate-spin mx-auto mb-4"></div>
                        <p className="text-xl font-medium text-gray-700">
                            Carregando perfil...
                        </p>
                    </div>
                </main>
            </div>
        );
    }

    // Erro ao carregar informações do usuário
    if (error || !userInfo) {
        return (
            <div className="flex md:flex-row flex-col min-h-screen">
                <div className="md:w-64 md:min-h-screen">
                    <Navbar />
                </div>
                <main className="flex-1 flex items-center justify-center">
                    <div className="text-center">
                        <div className="bg-red-100 p-4 rounded-lg mb-4">
                            <p className="text-red-600 font-medium">
                                {error || "Usuário não encontrado"}
                            </p>
                        </div>
                    </div>
                </main>
            </div>
        );
    }

    // Renderização principal do perfil
    return (
        <ProtectedRoute>
            <div className="flex md:flex-row min-h-screen">
                {/* Navbar fixa na lateral esquerda (igual às suas outras páginas) */}
                <div className="md:w-64 md:min-h-screen">
                    <Navbar />
                </div>

                {/* Conteúdo principal */}
                <main className="flex-1 bg-white overflow-x-hidden">
                    {/* Cabeçalho do perfil - estilo mobile/desktop */}
                    <div className="max-w-4xl mx-auto px-4 md:px-8 py-6">
                        <div className="bg-white mb-8 pt-2">
                            <div className="flex items-center gap-4">
                                <div className="h-16 w-16 bg-gray-100 rounded-full flex items-center justify-center overflow-hidden">
                                    {userInfo.profileImage ? (
                                        <img
                                            src={userInfo.profileImage}
                                            alt="Foto de perfil"
                                            className="h-full w-full object-cover"
                                        />
                                    ) : (
                                        <UserIcon className="h-8 w-8 text-gray-400" />
                                    )}
                                </div>
                                <div className="flex-1">
                                    <h1 className="text-xl font-bold text-gray-800">
                                        {userInfo.firstName} {userInfo.lastName}
                                    </h1>
                                    <p className="text-gray-500 text-sm">
                                        {userInfo.email}
                                    </p>
                                </div>
                                <button
                                    onClick={() => router.push("/profile/edit")}
                                    className="flex items-center gap-2 px-3 py-1.5 md:px-4 md:py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition text-sm md:text-base"
                                >
                                    <PencilSquareIcon className="h-4 w-4 md:h-5 md:w-5" />
                                    {/* Keep text visible on mobile but adjust overall size */}
                                    <span className="hidden sm:inline">
                                        Editar Perfil
                                    </span>{" "}
                                    {/* Optionally hide text on very small screens */}
                                    <span className="sm:hidden">Editar</span>{" "}
                                    {/* Shorter text for smallest screens */}
                                </button>
                            </div>
                        </div>

                        {/* Seções de filmes */}
                        <div className="">
                            {/* Seção de filmes recentes */}
                            <ScrollSection
                                title="Filmes Recentes"
                                movies={recentMovies}
                                loading={loadingRecents}
                                emptyMessage="Este usuário ainda não assistiu nenhum filme."
                            />

                            {/* Seção de filmes favoritos */}
                            <ScrollSection
                                title="Filmes Favoritos"
                                movies={favoriteMovies}
                                loading={loadingFavorites}
                                emptyMessage="Este usuário ainda não tem filmes favoritos."
                            />

                            {/* Seção de filmes recomendados */}
                            <ScrollSection
                                title="Filmes Recomendados"
                                movies={recommendedMovies}
                                loading={loadingRecommended}
                                emptyMessage="Ainda não há recomendações para este usuário."
                            />
                        </div>
                    </div>
                </main>
            </div>
        </ProtectedRoute>
    );
}
