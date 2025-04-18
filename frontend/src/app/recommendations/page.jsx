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

                    <div className="flex flex-col items-center justify-start w-full flex-1 overflow-y-auto">
                        <h1 className="text-2xl font-bold p-4">Histórico de Recomendações</h1>

                        {loading ? (
                            // Add flex-1 to make this div take available vertical space
                            // items-center and justify-center will center the spinner within this div
                            <div className="flex flex-1 justify-center items-center p-8">
                                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
                            </div>
                        ) : error ? (
                            // Also apply flex-1 to center error message vertically
                            <div className="flex flex-1 justify-center items-center text-red-500 p-4 text-center">
                                {error}
                            </div>
                        ) : recomendacoes.length === 0 ? (
                            // Also apply flex-1 to center empty state message vertically
                            <div className="flex flex-1 justify-center items-center text-center p-8 text-gray-500">
                                Você ainda não possui recomendações.
                            </div>
                        ) : (
                            <section className="w-full md:w-[60%] space-y-4 p-4 overflow-y-auto">
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
                                            className="w-16 h-24 rounded-md object-cover"
                                        />
                                        <div className="flex-1">
                                            <h2 className="text-base md:text-lg font-bold">
                                                {movie.title}
                                            </h2>
                                            <p className="text-xs md:text-sm text-gray-500">
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
                                                    <ClockSolid className="w-5 h-5 md:w-6 md:h-6" />
                                                ) : (
                                                    <ClockOutline className="w-5 h-5 md:w-6 md:h-6" />
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
                                                    <StarSolid className="w-5 h-5 md:w-6 md:h-6" />
                                                ) : (
                                                    <StarOutline className="w-5 h-5 md:w-6 md:h-6" />
                                                )}
                                            </button>
                                            <button
                                                className={`text-base font-bold ${activeIcons[movie.id]?.trash
                                                    ? "text-red-600"
                                                    : "text-base font-bold"
                                                    }`}
                                                onClick={() =>
                                                    toggleIcon(movie.id, "trash")
                                                }
                                            >
                                                {activeIcons[movie.id]?.trash ? (
                                                    <TrashSolid className="w-5 h-5 md:w-6 md:h-6" />
                                                ) : (
                                                    <TrashOutline className="w-5 h-5 md:w-6 md:h-6" />
                                                )}
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </section>
                        )}
                    </div>
                </main>
            </div>
        </ProtectedRoute>
    );
}