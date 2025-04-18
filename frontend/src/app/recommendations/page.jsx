"use client";

import Header from "@/components/Header";
import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import { useState, useEffect } from "react";
import { fetchRecommendations } from "@/lib/api";

import {
    ClockIcon as ClockOutline,
    StarIcon as StarOutline,
    TrashIcon as TrashOutline,
} from "@heroicons/react/24/outline";

import {
    ClockIcon as ClockSolid,
    StarIcon as StarSolid,
    TrashIcon as TrashSolid,
} from "@heroicons/react/24/solid";

export default function HistoricoPage() {
    const [activeIcons, setActiveIcons] = useState({});
    const [recomendacoes, setRecomendacoes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const toggleIcon = (movieId, iconType) => {
        setActiveIcons((prev) => ({
            ...prev,
            [movieId]: {
                ...prev[movieId],
                [iconType]: !prev[movieId]?.[iconType],
            },
        }));
    };

    useEffect(() => {
        async function carregarRecomendacoes() {
            try {
                setLoading(true);
                const dados = await fetchRecommendations();
                setRecomendacoes(dados);
                setError(null);
            } catch (err) {
                console.error("Erro ao carregar recomendações:", err);
                setError("Não foi possível carregar o histórico de recomendações.");
            } finally {
                setLoading(false);
            }
        }

        carregarRecomendacoes();
    }, []);

    return (
        <ProtectedRoute>
            <div className="bg-gray-100 md:flex">
                {/* Navbar */}
                <Navbar />

                <main className="flex-1 overflow-hidden flex flex-col h-[calc(100vh-4rem)] md:h-screen">
                    <Header />

                    <h1 className="text-2xl font-bold p-4">Histórico de Recomendações</h1>

                    {loading ? (
                        <div className="flex justify-center items-center p-8">
                            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
                        </div>
                    ) : error ? (
                        <div className="text-red-500 p-4 text-center">
                            {error}
                        </div>
                    ) : recomendacoes.length === 0 ? (
                        <div className="text-center p-8 text-gray-500">
                            Você ainda não possui recomendações.
                        </div>
                    ) : (
                        <section className="w-full max-w-md space-y-4 p-4 overflow-y-auto">
                            {recomendacoes.map((movie) => (
                                <div
                                    key={movie.id}
                                    className="flex items-center bg-white shadow-md rounded-lg p-4 space-x-4"
                                >
                                    <img
                                        src={movie.poster_path
                                            ? `https://image.tmdb.org/t/p/w200${movie.poster_path}`
                                            : '/placeholder.jpg'}
                                        alt={movie.title}
                                        className="w-12 h-12 rounded-md object-cover"
                                    />
                                    <div className="flex-1">
                                        <h2 className="text-base font-bold">
                                            {movie.title}
                                        </h2>
                                        <p className="text-sm text-gray-500">
                                            {movie.release_date ? new Date(movie.release_date).getFullYear() : 'N/A'}
                                        </p>
                                        <p className="text-xs text-gray-400">
                                            {movie.genres
                                                ? movie.genres.map(g => g.name).join(', ')
                                                : movie.genre_ids?.length > 0
                                                    ? 'Carregando gêneros...'
                                                    : 'Sem gêneros'}
                                        </p>
                                    </div>
                                    <div className="flex space-x-2">
                                        <button
                                            className={`text-base font-bold ${activeIcons[movie.id]?.clock
                                                    ? "text-blue-600"
                                                    : "text-base font-bold"
                                                }`}
                                            onClick={() =>
                                                toggleIcon(movie.id, "clock")
                                            }
                                        >
                                            {activeIcons[movie.id]?.clock ? (
                                                <ClockSolid className="w-5 h-5" />
                                            ) : (
                                                <ClockOutline className="w-5 h-5" />
                                            )}
                                        </button>
                                        <button
                                            className={`text-base font-bold ${activeIcons[movie.id]?.star
                                                    ? "text-yellow-400"
                                                    : "text-base font-bold"
                                                }`}
                                            onClick={() =>
                                                toggleIcon(movie.id, "star")
                                            }
                                        >
                                            {activeIcons[movie.id]?.star ? (
                                                <StarSolid className="w-5 h-5" />
                                            ) : (
                                                <StarOutline className="w-5 h-5" />
                                            )}
                                        </button>
                                        <button
                                            className={`text-base font-bold ${activeIcons[movie.id]?.hoverTrash ||
                                                    activeIcons[movie.id]?.trash
                                                    ? "text-red-600"
                                                    : "text-base font-bold"
                                                }`}
                                            onClick={() =>
                                                toggleIcon(movie.id, "trash")
                                            }
                                            onMouseEnter={() =>
                                                setActiveIcons((prev) => ({
                                                    ...prev,
                                                    [movie.id]: {
                                                        ...prev[movie.id],
                                                        hoverTrash: true,
                                                    },
                                                }))
                                            }
                                            onMouseLeave={() =>
                                                setActiveIcons((prev) => ({
                                                    ...prev,
                                                    [movie.id]: {
                                                        ...prev[movie.id],
                                                        hoverTrash: false,
                                                    },
                                                }))
                                            }
                                        >
                                            {activeIcons[movie.id]?.hoverTrash ||
                                                activeIcons[movie.id]?.trash ? (
                                                <TrashSolid className="w-5 h-5" />
                                            ) : (
                                                <TrashOutline className="w-5 h-5" />
                                            )}
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </section>
                    )}
                </main>
            </div>
        </ProtectedRoute>
    );
}