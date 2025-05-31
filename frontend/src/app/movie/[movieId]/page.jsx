"use client";

import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import {
    fetchMovieById,
    getMovieRating,
    setMovieRate,
    checkMovieRecommended,
    updateWatchlistMovie,
    updatefavoriteMovie,
    checkMovieWatchlist,
    checkMovieFavorite,
    deleteMovieRecommendation
} from "@/lib/api";
import {
    HandThumbDownIcon,
    HandThumbUpIcon,
    ClockIcon,
    StarIcon,
    TrashIcon
} from "@heroicons/react/24/outline";

import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { ArrowLeftIcon } from "@heroicons/react/24/outline";
import Image from "next/image";


export default function MovieProfilePage() {
    const { movieId } = useParams();
    const [movieInfo, setMovieInfo] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [rating, setRating] = useState(null);
    const [ratingLoading, setRatingLoading] = useState(false);
    const [watch, setWatch] = useState(false);
    const [favorite, setFavorite] = useState(false);
    const [backdropLoading, setBackdropLoading] = useState(true);
    const [posterLoading, setPosterLoading] = useState(true);
    const [isRecommended, setIsRecommended] = useState(false);
    const [toast, setToast] = useState({ show: false, message: '', type: 'success' });
    const [isMobile, setIsMobile] = useState(false);

    const showToast = (message, type = 'success') => {
        setToast({ show: true, message, type });
        setTimeout(() => {
            setToast({ show: false, message: '', type: 'success' });
        }, 3000); // Desaparece após 3 segundos
    };

    useEffect(() => {
        const checkMobile = () => {
            setIsMobile(window.innerWidth < 768);
        };
        
        checkMobile();
        window.addEventListener("resize", checkMobile);
        return () => window.removeEventListener("resize", checkMobile);
    }, []);
    

    useEffect(() => {
        const loadMovie = async () => {
            try {
                setLoading(true);
                const { movie, rating: initialRating } = await fetchMovieById(movieId);

                if (!movie) {
                    setError("Filme não encontrado.");
                    return;
                }
                setMovieInfo(movie);

                // Carregar todos os status em paralelo
                const [currentRating, isRecommended, isInWatchlist, isFavorite] = await Promise.all([
                    getMovieRating(movieId).catch(() => null),
                    checkMovieRecommended(movieId).catch(() => false),
                    checkMovieWatchlist(movieId).catch(() => false),
                    checkMovieFavorite(movieId).catch(() => false)
                ]);

                setRating(currentRating);
                setIsRecommended(isRecommended);
                setWatch(isInWatchlist);
                setFavorite(isFavorite);

            } catch {
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
            const response = await setMovieRate(movieId, true);
            if (response && 'currentRating' in response) {
                setRating(response.currentRating);
            }
        } catch {
            try {
                const currentRating = await getMovieRating(movieId);
                setRating(currentRating);
            } catch { }
        } finally {
            setRatingLoading(false);
        }
    };

    const handleDislike = async () => {
        if (ratingLoading) return;

        try {
            setRatingLoading(true);
            const response = await setMovieRate(movieId, false);
            if (response && 'currentRating' in response) {
                setRating(response.currentRating);
            }
        } catch {
            try {
                const currentRating = await getMovieRating(movieId);
                setRating(currentRating);
            } catch { }
        } finally {
            setRatingLoading(false);
        }
    };

    const handleWatch = async () => {
        if (ratingLoading) return;

        try {
            setRatingLoading(true);
            const newWatchStatus = !watch;

            const success = await updateWatchlistMovie(movieId, newWatchStatus);

            if (success) {
                setWatch(newWatchStatus);
                showToast(`Filme ${newWatchStatus ? 'adicionado' : 'removido'} da watchlist!`, 'success');
            } else {
                console.error("❌ Falha ao atualizar watchlist");
                showToast("Falha ao atualizar watchlist", 'error');
                // Reverter o estado em caso de erro
                const currentStatus = await checkMovieWatchlist(movieId);
                setWatch(currentStatus);
            }
        } catch (error) {
            console.error("❌ Erro ao atualizar watchlist:", error);
            // Reverter o estado em caso de erro
            try {
                const currentStatus = await checkMovieWatchlist(movieId);
                setWatch(currentStatus);
            } catch { }
        } finally {
            setRatingLoading(false);
        }
    };

    const handleFavorite = async () => {
        if (ratingLoading) return;

        try {
            setRatingLoading(true);
            const newFavoriteStatus = !favorite;

            const success = await updatefavoriteMovie(movieId, newFavoriteStatus);

            if (success) {
                setFavorite(newFavoriteStatus);
                showToast(`Filme ${newFavoriteStatus ? 'adicionado' : 'removido'} dos favoritos!`, 'success');
            } else {
                console.error("❌ Falha ao atualizar favoritos");
                showToast("Falha ao atualizar favoritos", 'error');
                // Reverter o estado em caso de erro
                const currentStatus = await checkMovieFavorite(movieId);
                setFavorite(currentStatus);
            }
        } catch (error) {
            console.error("❌ Erro ao atualizar favoritos:", error);
            // Reverter o estado em caso de erro
            try {
                const currentStatus = await checkMovieFavorite(movieId);
                setFavorite(currentStatus);
            } catch { }
        } finally {
            setRatingLoading(false);
        }
    };

    // Handle delete for Recommended movies
    const handleDeleteRecomended = async () => {
        if (ratingLoading) return;

        try {
            setRatingLoading(true);

            const success = await deleteMovieRecommendation(movieId);

            if (success) {
                setIsRecommended(false);
                console.log("✅ Filme removido das recomendações com sucesso!");
                showToast("Filme removido das recomendações!", "success");
            } else {
                console.error("❌ Falha ao remover filme das recomendações");
                showToast("Falha ao remover filme das recomendações", "error");
                // Reverter o estado em caso de erro
                const currentStatus = await checkMovieRecommended(movieId);
                setIsRecommended(currentStatus);
            }
        } catch (error) {
            console.error("❌ Erro ao remover filme recomendado:", error);
            // Reverter o estado em caso de erro
            try {
                const currentStatus = await checkMovieRecommended(movieId);
                setIsRecommended(currentStatus);
            } catch { }
        } finally {
            setRatingLoading(false);
        }
    }

    const languageMap = {
        en: "Inglês",
        pt: "Português",
        es: "Espanhol",
        fr: "Francês",
        ja: "Japonês",
        ko: "Coreano",
        de: "Alemão",
        zh: "Chinês",
        it: "Italiano",
        hi: "Hindi",
        // adicione mais conforme necessário
    };

    function convertLanguageCode(code) {
        return languageMap[code] || code;
    }

    if (loading) {
        return (
            <ProtectedRoute>
                <div className="flex flex-col md:flex-row min-h-screen bg-background">
                    <div className="md:w-64">
                        <Navbar />
                    </div>
                    <main className="flex-1 flex items-center justify-center overflow-y-auto">
                        <div className="text-center px-4">
                            <div className="w-16 h-16 border-t-4 border-accent border-solid rounded-full animate-spin mx-auto mb-4"></div>
                            <p className="text-xl font-medium text-primary">
                                Carregando informações do filme...
                            </p>
                        </div>
                    </main>
                </div>
            </ProtectedRoute>
        );
    }

    if (error || !movieInfo) {
        return (
            <ProtectedRoute>
                <div className="flex flex-col md:flex-row min-h-screen bg-background">
                    <div className="md:w-64">
                        <Navbar />
                    </div>
                    <main className="flex-1 flex items-center justify-center overflow-y-auto">
                        <div className="text-center px-4">
                            <div className="bg-red-500/10 border border-red-500/50 p-4 rounded-lg mb-4">
                                <p className="text-red-400 font-medium">
                                    {error || "Filme não encontrado."}
                                </p>
                            </div>
                        </div>
                    </main>
                </div>
            </ProtectedRoute>
        );
    }

    return (
        <ProtectedRoute>
            <div className={`flex flex-col md:flex-row min-h-screen overflow-y-auto bg-background ${isMobile ? 'pb-20' : ''}`}>
                  
                <div className="md:w-64">
                    <Navbar />
                </div>

                <main className="flex-1 overflow-y-auto">
                    {/* Backdrop com overlay escuro */}
                    <div className="relative w-full h-[350px] md:h-[450px]">
                        {backdropLoading && (
                            <div className="absolute inset-0 bg-foreground animate-pulse" />
                        )}
                        <Image
                            src={`https://image.tmdb.org/t/p/original${movieInfo.backdropPath}`}
                            alt={movieInfo.title}
                            fill
                            className={`object-cover transition-opacity duration-500 ${backdropLoading ? 'opacity-0' : 'opacity-100'}`}
                            onLoadingComplete={() => setBackdropLoading(false)}
                        />
                        <div className="absolute inset-0 bg-gradient-to-b from-black/80 via-black/60 to-background" />

                        {/* Botão Voltar */}
                        <button
                            onClick={() => window.history.back()}
                            className="absolute top-4 left-4 bg-black/60 hover:bg-black/80 text-white px-3 py-2 rounded-full shadow-md flex items-center gap-2 transition-colors"
                        >
                            <ArrowLeftIcon className="w-5 h-5" />
                            <span className="hidden md:inline">Voltar</span>
                        </button>
                    </div>

                    {/* Conteúdo Principal */}
                    <div className="relative md:flex items-center -mt-48 px-6 max-w-5xl mx-auto gap-10">
                        <div className="flex flex-col items-center md:flex-row gap-6 mb-10 md:mb-0 ">
                            {/* Poster */}
                            <div className="w-48 md:w-64 shrink-0 relative rounded-2xl overflow-hidden h-[288px] md:h-[384px]">
                                {posterLoading && (
                                    <div className="absolute inset-0 bg-foreground animate-pulse rounded-2xl" />
                                )}

                                {movieInfo.posterPath ? (
                                    <Image
                                        src={`https://image.tmdb.org/t/p/original${movieInfo.posterPath}`}
                                        alt={movieInfo.title}
                                        fill
                                        priority
                                        className={`object-cover rounded-2xl shadow-xl transition-opacity duration-500 ${posterLoading ? 'opacity-0' : 'opacity-100'}`}
                                        onLoadingComplete={() => setPosterLoading(false)}
                                    />
                                ) : (
                                    <div className="bg-foreground w-full h-full flex items-center justify-center rounded-2xl">
                                        <span className="text-secondary">Sem imagem</span>
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Informações */}
                        <div className="flex-1">
                            <h1 className="text-4xl font-bold text-primary mb-2">
                                {movieInfo.title}
                            </h1>

                            {/* Rating */}
                            <div className="flex items-center gap-2 text-secondary">
                                {Array.from({ length: 5 }).map((_, index) => (
                                    <span
                                        key={index}
                                        className="cursor-pointer hover:text-accent transition-colors duration-300"
                                    >
                                        {index < Math.round(movieInfo.rating / 2) ? "⭐" : "☆"}
                                    </span>
                                ))}
                                <span className="text-muted text-primary">
                                    ({movieInfo.rating.toFixed(1)})
                                </span>
                            </div>

                            <p className="text-sm text-muted mb-2 text-secondary">
                                {movieInfo.releaseDate} • {movieInfo.genres?.join(", ")} • {convertLanguageCode(movieInfo.originalLanguage)}
                            </p>
                            <p className="text-primary mb-6">
                                {movieInfo.overview}
                            </p>

                            {/* Botões de Ações */}
                            <div className="flex gap-4">
                                <button
                                    onClick={handleDislike}
                                    disabled={ratingLoading}
                                    className={`w-12 h-12 flex items-center justify-center rounded-full shadow-md hover:scale-105 transition ${rating === false ? "bg-accent" : "bg-foreground"} ${ratingLoading ? "opacity-50 cursor-not-allowed" : ""}`}
                                    title="Descurtir"
                                >
                                    <HandThumbDownIcon className={`w-6 h-6 ${rating === false ? "text-foreground" : "text-accent"}`} />
                                </button>

                                <button
                                    onClick={handleLike}
                                    disabled={ratingLoading}
                                    className={`w-12 h-12 flex items-center justify-center rounded-full shadow-md hover:scale-105 transition ${rating === true ? "bg-accent" : "bg-foreground"} ${ratingLoading ? "opacity-50 cursor-not-allowed" : ""}`}
                                    title="Curtir"
                                >
                                    <HandThumbUpIcon className={`w-6 h-6 ${rating === true ? "text-foreground" : "text-accent"}`} />
                                </button>

                                <button
                                    onClick={handleWatch}
                                    disabled={ratingLoading}
                                    className={`w-12 h-12 flex items-center justify-center rounded-full shadow-md hover:scale-105 transition ${watch ? "bg-accent" : "bg-foreground"} ${ratingLoading ? "opacity-50 cursor-not-allowed" : ""}`}
                                    title="Assistir depois"
                                >
                                    <ClockIcon className={`w-6 h-6 ${watch ? "text-foreground" : "text-accent"}`} />
                                </button>

                                <button
                                    onClick={handleFavorite}
                                    disabled={ratingLoading}
                                    className={`w-12 h-12 flex items-center justify-center rounded-full shadow-md hover:scale-105 transition ${favorite ? "bg-accent" : "bg-foreground"} ${ratingLoading ? "opacity-50 cursor-not-allowed" : ""}`}
                                    title="Favoritar"
                                >
                                    <StarIcon className={`w-6 h-6 ${favorite ? "text-foreground" : "text-accent"}`} />
                                </button>

                                {isRecommended && (
                                    <button
                                        onClick={handleDeleteRecomended}
                                        disabled={ratingLoading}
                                        className={`w-12 h-12 flex items-center justify-center rounded-full shadow-md hover:scale-105 transition bg-red-500/20 hover:bg-red-500/30 ${ratingLoading ? "opacity-50 cursor-not-allowed" : ""}`}
                                        title="Remover das recomendações"
                                    >
                                        <TrashIcon className="w-6 h-6 text-red-400" />
                                    </button>
                                )}
                            </div>
                        </div>
                    </div>
                </main>
            </div>

            {/* Toast Notification */}
            {toast.show && (
                <div className="fixed top-4 right-4 z-50 animate-in slide-in-from-top-2 duration-300">
                    <div className={`px-4 py-3 rounded-lg shadow-lg flex items-center gap-3 max-w-sm ${toast.type === 'success'
                            ? 'bg-green-500/10 border border-green-500/20 text-green-400'
                            : 'bg-red-500/10 border border-red-500/20 text-red-400'
                        }`}>
                        {toast.type === 'success' ? (
                            <svg className="w-5 h-5 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                            </svg>
                        ) : (
                            <svg className="w-5 h-5 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        )}
                        <span className="text-sm font-medium">{toast.message}</span>
                    </div>
                </div>
            )}
        </ProtectedRoute>
    );
}