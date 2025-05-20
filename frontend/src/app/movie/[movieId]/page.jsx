"use client";

import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import { fetchMovieById } from "@/lib/api";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function MovieProfilePage() {
    const { movieId } = useParams();

    const [movieInfo, setMovieInfo] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadMovie = async () => {
            try {
                setLoading(true);
                const data = await fetchMovieById(movieId);
                setMovieInfo(data);
            } catch (err) {
                console.error("Erro ao buscar filme:", err);
                setError("Não foi possível carregar os dados do filme.");
            } finally {
                setLoading(false);
            }
        };

        if (movieId) {
            loadMovie();
        }
    }, [movieId]);

    if (loading) {
        return (
            <div className="flex min-h-screen">
                <div className="md:w-64">
                    <Navbar />
                </div>
                <main className="flex-1 flex items-center justify-center">
                    <div className="text-center">
                        <div className="w-16 h-16 border-t-4 border-accent border-solid rounded-full animate-spin mx-auto mb-4"></div>
                        <p className="text-xl font-medium text-gray-700">
                            Carregando informações do filme...
                        </p>
                    </div>
                </main>
            </div>
        );
    }

    if (error || !movieInfo) {
        return (
            <div className="flex min-h-screen">
                <div className="md:w-64">
                    <Navbar />
                </div>
                <main className="flex-1 flex items-center justify-center">
                    <div className="text-center">
                        <div className="bg-red-100 p-4 rounded-lg mb-4">
                            <p className="text-red-600 font-medium">
                                {error || "Filme não encontrado."}
                            </p>
                        </div>
                    </div>
                </main>
            </div>
        );
    }

    return (
        <ProtectedRoute>
            <div className="flex min-h-screen">
                <div className="md:w-64">
                    <Navbar />
                </div>
                <main className="flex-1 bg-white">
                    <div className="max-w-4xl mx-auto px-4 md:px-8 py-6">
                        <div className="flex gap-6 flex-col md:flex-row">
                            <div className="w-full md:w-1/3">
                                <img
                                    src={movieInfo.posterUrl}
                                    alt={movieInfo.title}
                                    className="rounded-2xl shadow-lg w-full"
                                />
                            </div>
                            <div className="flex-1">
                                <h1 className="text-3xl font-bold mb-2">
                                    {movieInfo.title}
                                </h1>
                                <p className="text-gray-500 mb-4 italic">
                                    {movieInfo.tagline}
                                </p>
                                <p className="text-gray-700 mb-4">
                                    {movieInfo.overview}
                                </p>
                                <p className="text-sm text-gray-600">
                                    <strong>Gêneros:</strong>{" "}
                                    {movieInfo.genres?.join(", ")}
                                </p>
                                <p className="text-sm text-gray-600">
                                    <strong>Data de Lançamento:</strong>{" "}
                                    {movieInfo.releaseDate}
                                </p>
                                <p className="text-sm text-gray-600">
                                    <strong>Nota Média:</strong>{" "}
                                    {movieInfo.voteAverage} ⭐
                                </p>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </ProtectedRoute>
    );
}
