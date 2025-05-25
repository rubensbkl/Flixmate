"use client";

import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import { fetchMovieById, getMovieRating, setMovieRate } from "@/lib/api";
import {
    HandThumbDownIcon,
    HandThumbUpIcon,
} from "@heroicons/react/24/outline";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function MovieProfilePage() {
    const { movieId } = useParams();

    const [movieInfo, setMovieInfo] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [rating, setRating] = useState(null);
    const [ratingLoading, setRatingLoading] = useState(false);

    useEffect(() => {
        const loadMovie = async () => {
            try {
                setLoading(true);
                const { movie, rating: initialRating } = await fetchMovieById(movieId);
                console.log("Dados do filme:", movie);
                console.log("Rating inicial do filme:", initialRating);

                if (!movie) {
                    setError("Filme não encontrado.");
                    return;
                }
                setMovieInfo(movie);
                
                // Buscar o rating atual do usuário para este filme
                try {
                    const currentRating = await getMovieRating(movieId);
                    setRating(currentRating);
                    console.log("Rating atual encontrado:", currentRating);
                } catch (ratingError) {
                    console.log("Nenhum rating encontrado para este filme");
                    setRating(null);
                }
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

    const handleLike = async () => {
        if (ratingLoading) return;
        
        try {
            setRatingLoading(true);
            
            console.log("Estado atual antes do like:", rating);
            
            // Sempre envia a requisição para o backend
            const response = await setMovieRate(movieId, true);
            
            console.log("Resposta do backend:", response);
            
            // Verifica se a resposta contém currentRating (mesmo que seja null)
            if (response && ('currentRating' in response)) {
                setRating(response.currentRating);
                console.log("Novo estado após like:", response.currentRating);
                console.log("Operação realizada:", response.operation);
            } else {
                console.error("Resposta do backend não contém currentRating");
                console.error("Propriedades da resposta:", Object.keys(response || {}));
            }
            
        } catch (err) {
            console.error("Erro ao processar like:", err);
            // Em caso de erro, recarrega o estado atual do servidor
            try {
                const currentRating = await getMovieRating(movieId);
                setRating(currentRating);
                console.log("Estado recarregado após erro:", currentRating);
            } catch (reloadError) {
                console.error("Erro ao recarregar estado:", reloadError);
            }
        } finally {
            setRatingLoading(false);
        }
    };

    const handleDislike = async () => {
        if (ratingLoading) return;
        
        try {
            setRatingLoading(true);
            
            console.log("Estado atual antes do dislike:", rating);
            
            // Sempre envia a requisição para o backend
            const response = await setMovieRate(movieId, false);
            
            console.log("Resposta do backend:", response);
            
            // Verifica se a resposta contém currentRating (mesmo que seja null)
            if (response && ('currentRating' in response)) {
                setRating(response.currentRating);
                console.log("Novo estado após dislike:", response.currentRating);
                console.log("Operação realizada:", response.operation);
            } else {
                console.error("Resposta do backend não contém currentRating");
                console.error("Propriedades da resposta:", Object.keys(response || {}));
            }
            
        } catch (err) {
            console.error("Erro ao processar dislike:", err);
            // Em caso de erro, recarrega o estado atual do servidor
            try {
                const currentRating = await getMovieRating(movieId);
                setRating(currentRating);
                console.log("Estado recarregado após erro:", currentRating);
            } catch (reloadError) {
                console.error("Erro ao recarregar estado:", reloadError);
            }
        } finally {
            setRatingLoading(false);
        }
    };

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
            <div className="flex flex-col md:flex-row h-screen overflow-hidden">
                <div className="md:w-64">
                    <Navbar />
                </div>
                <main className="flex-1">
                    <div className="max-w-4xl mx-auto px-4 md:px-8 py-6">
                        <div className="flex gap-6 flex-col md:flex-row">
                            <div className="w-full md:w-1/3">
                                <img
                                    src={`https://image.tmdb.org/t/p/w500${movieInfo.posterPath}`}
                                    alt={movieInfo.title}
                                    className="rounded-2xl shadow-lg w-full"
                                />
                            </div>
                            <div className="flex-1">
                                <h1 className="text-3xl font-bold mb-6 text-primary">
                                    {movieInfo.title}
                                </h1>
                                <p className="text-gray-200 mb-4">
                                    {movieInfo.overview}
                                </p>
                                <p className="text-sm text-gray-400">
                                    <strong>Gêneros:</strong>{" "}
                                    {movieInfo.genres?.join(", ")}
                                </p>
                                <p className="text-sm text-gray-400">
                                    <strong>Data de Lançamento:</strong>{" "}
                                    {movieInfo.releaseDate}
                                </p>
                                <p className="text-sm text-gray-400">
                                    <strong>Rating:</strong> {movieInfo.rating}
                                </p>
                            </div>
                        </div>
                    </div>
                    <div className="flex space-x-4 py-4 md:py-2 justify-center mb-20 md:mb-4">
                        <button
                            onClick={handleDislike}
                            disabled={ratingLoading}
                            className={`w-14 h-14 flex items-center justify-center rounded-full shadow-lg hover:scale-105 transition ${
                                rating === false ? "bg-accent" : "bg-foreground"
                            } ${ratingLoading ? "opacity-50 cursor-not-allowed" : ""}`}
                            title="Descurtir"
                        >
                            <HandThumbDownIcon
                                className={`w-6 h-6 ${
                                    rating === false
                                        ? "text-foreground"
                                        : "text-accent"
                                }`}
                            />
                        </button>
                        <button
                            onClick={handleLike}
                            disabled={ratingLoading}
                            className={`w-14 h-14 flex items-center justify-center rounded-full shadow-lg hover:scale-105 transition ${
                                rating === true ? "bg-accent" : "bg-foreground"
                            } ${ratingLoading ? "opacity-50 cursor-not-allowed" : ""}`}
                            title="Curtir"
                        >
                            <HandThumbUpIcon
                                className={`w-6 h-6 ${
                                    rating === true
                                        ? "text-foreground"
                                        : "text-accent"
                                }`}
                            />
                        </button>
                    </div>
                </main>
            </div>
        </ProtectedRoute>
    );
}