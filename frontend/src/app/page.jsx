"use client";

import ImprovedMovieCard from "@/components/ImprovedMovieCard";
import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import {
    ArrowPathIcon,
    HeartIcon,
    SparklesIcon,
    XMarkIcon,
} from "@heroicons/react/24/outline";
import { useEffect, useRef, useState, useCallback } from "react";
import { useSwipeable } from "react-swipeable";
import TinderCard from "react-tinder-card";

import { loadSession, saveSession, clearSession } from "@/lib/session";
import { fetchMovies, sendFeedback, gerarRecomendacao } from "@/lib/api";
import { movieCache } from "@/lib/cache";

export default function Home() {
    const [movies, setMovies] = useState([]);
    const [currentIndex, setCurrentIndex] = useState(-1);
    const [feedbackCount, setFeedbackCount] = useState(0);
    const [loading, setLoading] = useState(true);
    const [isAnimating, setIsAnimating] = useState(false);
    const [swipeDirection, setSwipeDirection] = useState(null);
    const [isLoadingRecommendation, setIsLoadingRecommendation] = useState(false);
    
    const userId = useRef(1);
    const currentPage = useRef(1);
    const currentMovieRef = useRef(null);

    const canSwipe = currentIndex >= 0 && !isAnimating && !loading && !isLoadingRecommendation;

    useEffect(() => {
        const user = JSON.parse(localStorage.getItem("user"));
        userId.current = user?.id || 1;
    }, []);
    
    const loadMovies = useCallback(async () => {
        const session = loadSession(userId.current);
        if (session) {
            console.log("🔄 Restaurando sessão");
            setMovies(session.movies);
            setCurrentIndex(session.currentIndex);
            setFeedbackCount(session.feedbackCount);
            currentPage.current = session.currentPage || 1;
            setLoading(false);
            return;
        }
    
        setLoading(true);
        try {
            const fetchedMovies = await fetchMovies(userId.current, currentPage.current);
            const reversed = [...fetchedMovies].reverse();
            const startIdx = reversed.length - 1;
        
            setMovies(reversed);
            setCurrentIndex(startIdx);
            setFeedbackCount(0);
            saveSession(userId.current, {
                movies: reversed,
                currentIndex: startIdx,
                feedbackCount: 0,
                currentPage: currentPage.current
            });
        
        } catch (err) {
            console.error("Erro ao carregar filmes:", err);
        } finally {
            setLoading(false);
        }
    }, []);
    
    useEffect(() => { if (userId.current) loadMovies(); }, [loadMovies]);
    
    const swiped = async (direction, index) => {
        if (!canSwipe) return;
    
        const liked = direction === "right";
        const movie = movies[index];
        console.log(`${liked ? "Curtiu" : "Descartou"}: ${movie.title}`);
    
        setIsAnimating(true);
        setSwipeDirection(direction);
    
        await sendFeedback(userId.current, movie.id, liked);
    
        const newCount = feedbackCount + 1;
        setFeedbackCount(newCount);
        saveSession(userId.current, {
            movies,
            currentIndex: index - 1,
            feedbackCount: newCount,
            currentPage: currentPage.current
        });
    
        if (newCount >= 10) {
            setIsLoadingRecommendation(true);
            await gerarRecomendacao(userId.current);
            setIsLoadingRecommendation(false);
            setFeedbackCount(0);

            setCurrentIndex(index - 1);
            saveSession(userId.current, {
                movies,
                currentIndex: index - 1,
                feedbackCount: 0,
                currentPage: currentPage.current
            });

            // Libera a animação e reseta direção depois
            setTimeout(() => {
                setIsAnimating(false);
                setSwipeDirection(null);
            }, 300);

            return; // <-- pra evitar o setTimeout abaixo
        }
    
        // Só decrementa aqui se não teve recomendação
        setTimeout(() => {
            setCurrentIndex(prev => prev - 1);
            setIsAnimating(false);
            setSwipeDirection(null);
        }, 300);
    };
    
    const swipe = (dir) => {
        if (canSwipe && currentMovieRef.current) {
            setSwipeDirection(dir);
            currentMovieRef.current.swipe(dir);
        }
    };
    
    const outOfFrame = (idx) => console.log(`${movies[idx]?.title} saiu da tela.`);
    
    const resetMatches = async () => {
        setFeedbackCount(0);
        clearSession(userId.current);
        currentPage.current = 1;
        movieCache.clear(userId.current);
        await loadMovies();
    };
    
    const handlers = useSwipeable({
        onSwipedLeft: () => swipe("left"),
        onSwipedRight: () => swipe("right"),
        preventDefaultTouchmoveEvent: true,
        trackMouse: true,
    });

    // Renderização condicional durante o carregamento
    if (loading && movies.length === 0) {
        return (
            <ProtectedRoute>
                <div className="bg-gray-100 md:flex">
                    <Navbar />
                    <main className="flex-1 overflow-hidden flex flex-col h-[calc(100vh-4rem)] items-center justify-center">
                        <div className="text-xl font-semibold">Carregando filmes...</div>
                        <div className="mt-4 w-16 h-16 border-t-4 border-blue-500 border-solid rounded-full animate-spin"></div>
                    </main>
                </div>
            </ProtectedRoute>
        );
    }

    return (
        <ProtectedRoute>
            <div className="bg-gray-100 md:flex">
                {/* Navbar */}
                <Navbar />

                {/* Conteúdo principal */}
                <main className="flex-1 overflow-hidden flex flex-col h-[calc(100vh-4rem)] md:h-screen">
                    <div className="flex-1 flex flex-col overflow-hidden pb-16 md:pb-0">
                        {/* Barra de progresso */}
                        <div className="relative h-1 w-full bg-gray-200">
                            <div
                                className="absolute h-1 bg-black transition-all duration-300 ease-in-out"
                                style={{ width: `${(feedbackCount / 10) * 100}%` }}
                            ></div>
                        </div>

                        <div
                            className="h-[80vh] flex items-center justify-center relative"
                            {...handlers}
                        >
                            <div className="relative flex items-center justify-center w-full h-full">
                                {currentIndex >= 0 ? (
                                    movies.map((movie, index) => {
                                        const isTop = index === currentIndex;
                                        const isNext = index === currentIndex - 1;
                                        
                                        // Só renderizar o cartão atual e o próximo
                                        if (!isTop && !isNext) return null;

                                        return (
                                            <TinderCard
                                                ref={isTop ? currentMovieRef : null}
                                                key={`${movie.id}-${index}`}
                                                onSwipe={(dir) => swiped(dir, index)}
                                                onCardLeftScreen={() => outOfFrame(index)}
                                                preventSwipe={["up", "down"]}
                                                className="absolute inset-0 flex items-center justify-center"
                                            >
                                                <div
                                                    className={`w-[90vw] max-w-md ${
                                                        isNext
                                                            ? "scale-90 translate-y-4 transition-all duration-300"
                                                            : ""
                                                    }`}
                                                >
                                                    <ImprovedMovieCard
                                                        movie={movie}
                                                        isActive={isTop}
                                                        isAnimating={isAnimating && isTop}
                                                        swipeDirection={isTop ? swipeDirection : null}
                                                        className="h-[60vh] md:h-[70vh]"
                                                    />
                                                </div>
                                            </TinderCard>
                                        );
                                    })
                                ) : (
                                    <div className="text-center p-8 bg-white rounded-lg shadow">
                                        <h2 className="text-2xl font-bold mb-4">
                                            Sem mais filmes!
                                        </h2>
                                        <p className="text-gray-600 mb-4">
                                            Você viu todos os filmes disponíveis.
                                        </p>
                                        <button
                                            onClick={() => {
                                                currentPage.current += 1;
                                                clearSession(userId.current);
                                                movieCache.clear(userId.current);
                                                loadMovies();
                                            }}
                                            className="px-4 py-2 bg-black text-white rounded-lg hover:bg-gray-900"
                                        >
                                            Buscar Mais Filmes
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Botões de interação */}
                        <div className="flex space-x-4 py-2 justify-center">
                            <button
                                onClick={resetMatches}
                                disabled={loading}
                                className="w-14 h-14 flex items-center justify-center bg-white rounded-full shadow-lg hover:scale-105 transition disabled:opacity-50"
                                title="Reiniciar"
                            >
                                <ArrowPathIcon className="w-6 h-6 text-gray-700" />
                            </button>
                            <button
                                onClick={() => swipe("left")}
                                className="w-14 h-14 flex items-center justify-center bg-white rounded-full shadow-lg hover:scale-105 transition disabled:opacity-50"
                                disabled={!canSwipe}
                                title="Descurtir"
                            >
                                <XMarkIcon className="w-6 h-6 text-red-700" />
                            </button>
                            <button
                                onClick={() => swipe("right")}
                                className="w-14 h-14 flex items-center justify-center bg-white rounded-full shadow-lg hover:scale-105 transition disabled:opacity-50"
                                disabled={!canSwipe}
                                title="Curtir"
                            >
                                <HeartIcon className="w-6 h-6 text-green-500" />
                            </button>
                            <button
                                onClick={() => alert("Modo surpresa!")}
                                className="w-14 h-14 flex items-center justify-center bg-yellow-400 rounded-full shadow-lg hover:scale-105 transition"
                                disabled={!canSwipe}
                                title="Surpresa"
                            >
                                <SparklesIcon className="w-6 h-6 text-gray-700" />
                            </button>
                        </div>
                    </div>

                    <div className={`fixed inset-0 bg-gray-100 flex flex-col items-center justify-center transition-opacity duration-500 ${
                        isLoadingRecommendation  ? "opacity-100" : "opacity-0 pointer-events-none"
                    }`}>
                        <div className="text-xl font-semibold">Gerando recomendação...</div>
                        <div className="mt-4 w-16 h-16 border-t-4 border-yellow-500 border-solid rounded-full animate-spin"></div>
                    </div>
                </main>
            </div>

        </ProtectedRoute>
    );
}