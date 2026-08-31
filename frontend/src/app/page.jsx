"use client";

import ErrorModal from "@/components/ErrorModal";
import ImprovedMovieCard from "@/components/ImprovedMovieCard";
import Navbar from "@/components/Navbar";
import {
    ArrowRightIcon,
    HeartIcon,
    SparklesIcon,
    XMarkIcon,
} from "@heroicons/react/24/outline";
import { useCallback, useEffect, useRef, useState } from "react";
import { useSwipeable } from "react-swipeable";
import TinderCard from "react-tinder-card";

import MovieMatchModal from "@/components/MovieMatchModal";
import { useAuth, useUserId } from "@/contexts/AuthContext";
import {
    fetchMoviesToRate,
    getRecommendation,
    setMovieRate,
} from "@/lib/api";
import { movieCache } from "@/lib/cache";
import { clearSession, loadSession, saveSession } from "@/lib/session";
import { useRouter } from "next/navigation";

export default function Home() {
    const [movies, setMovies] = useState([]);
    const [currentIndex, setCurrentIndex] = useState(-1);
    const [feedbackCount, setFeedbackCount] = useState(0);
    const [loading, setLoading] = useState(true);
    const [isAnimating, setIsAnimating] = useState(false);
    const [swipeDirection, setSwipeDirection] = useState(null);
    const [isLoadingRecommendation, setIsLoadingRecommendation] = useState(false);
    const [isMobile, setIsMobile] = useState(false);
    const router = useRouter();

    const [showMatchModal, setShowMatchModal] = useState(false);
    const [recommendedMovie, setRecommendedMovie] = useState(null);
    const [showErrorModal, setShowErrorModal] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const hasLoadedMovies = useRef(false); // FLAG PARA CONTROLAR CARREGAMENTO

    const { user, isInitialized } = useAuth();
    const userId = useUserId(); // Hook que sempre retorna o userId
    const currentPage = useRef(1);
    const currentMovieRef = useRef(null);

    const [pendingTrainingRequests, setPendingTrainingRequests] = useState(0);
    const [completedTrainingRequests, setCompletedTrainingRequests] = useState(0);
    const trainingQueue = useRef([]);
    const isProcessingQueue = useRef(false);

    const canSwipe =
        currentIndex >= 0 &&
        !isAnimating &&
        !loading &&
        !isLoadingRecommendation;

    // Detect mobile device
    useEffect(() => {
        const checkMobile = () => {
            setIsMobile(window.innerWidth < 768);
        };

        // Check on initial load
        checkMobile();

        // Add event listener for window resize
        window.addEventListener("resize", checkMobile);

        // Cleanup
        return () => window.removeEventListener("resize", checkMobile);
    }, []);


    const handleCloseErrorModal = () => {
        setShowErrorModal(false);
        setErrorMessage("");
    };

    const loadMovies = useCallback(async () => {
        if (!isInitialized || !userId || hasLoadedMovies.current) return;
        
        hasLoadedMovies.current = true; // MARCAR COMO CARREGADO

        const session = loadSession();
        if (session) {
            setMovies(session.movies);
            setCurrentIndex(session.currentIndex);
            setFeedbackCount(session.feedbackCount);
            currentPage.current = session.currentPage || 1;
            setLoading(false);
            return;
        }

        setLoading(true);
        try {
            const fetchedMovies = await fetchMoviesToRate(currentPage.current);
            const reversed = [...fetchedMovies].reverse();
            const startIdx = reversed.length - 1;

            setMovies(reversed);
            setCurrentIndex(startIdx);
            setFeedbackCount(0);
            saveSession({
                movies: reversed,
                currentIndex: startIdx,
                feedbackCount: 0,
                currentPage: currentPage.current,
            });
        } catch (err) {
            hasLoadedMovies.current = false; // RESETAR EM CASO DE ERRO
        } finally {
            setLoading(false);
        }
    }, [isInitialized, userId]);


    // Função para carregar mais filmes automaticamente
    const loadMoreMovies = useCallback(async () => {
        if (loading) return;

        setLoading(true);
        try {
            currentPage.current += 1;
            const fetchedMovies = await fetchMoviesToRate(currentPage.current);
            const reversed = [...fetchedMovies].reverse();
            const startIdx = reversed.length - 1;

            setMovies(reversed);
            setCurrentIndex(startIdx);

            // Limpar cache antigo e salvar nova sessão
            clearSession();
            movieCache.clear(localStorage.getItem("token"));

            saveSession({
                movies: reversed,
                currentIndex: startIdx,
                feedbackCount,
                currentPage: currentPage.current,
            });
        } catch (err) {
        } finally {
            setLoading(false);
        }
    }, [loading, feedbackCount]);

    useEffect(() => {
        if (isInitialized && userId && !hasLoadedMovies.current) {
            loadMovies();
        }
    }, [isInitialized, userId, loadMovies]);


    useEffect(() => {
        if (currentIndex < 0 && movies.length > 0 && !loading) {
            loadMoreMovies();
        }
    }, [currentIndex, movies.length, loading, loadMoreMovies]);

    // Função para processar fila de treinamento assincronamente
    const processTrainingQueue = useCallback(async () => {
        if (isProcessingQueue.current || trainingQueue.current.length === 0) {
            return;
        }

        isProcessingQueue.current = true;

        while (trainingQueue.current.length > 0) {
            const trainingData = trainingQueue.current.shift();
            
            try {
                await setMovieRate(trainingData.movieId, trainingData.liked);
                setCompletedTrainingRequests(prev => prev + 1);
            } catch (error) {
                // Re-adicionar à fila em caso de erro (máximo 3 tentativas)
                if (!trainingData.retryCount) trainingData.retryCount = 0;
                if (trainingData.retryCount < 3) {
                    trainingData.retryCount++;
                    trainingQueue.current.push(trainingData);
                }
            }

            // Pequeno delay para não sobrecarregar o servidor
            await new Promise(resolve => setTimeout(resolve, 100));
        }

        isProcessingQueue.current = false;
    }, []);

    // Função para adicionar treinamento à fila
    const queueTraining = useCallback((movieId, movieTitle, liked) => {
        trainingQueue.current.push({
            movieId,
            movieTitle,
            liked,
            timestamp: Date.now()
        });

        setPendingTrainingRequests(prev => prev + 1);
        
        // Processar fila automaticamente
        processTrainingQueue();
    }, [processTrainingQueue]);

    const swiped = async (direction, index) => {
        if (!canSwipe) return;

        const liked = direction === "right";
        const movie = movies[index];

        setIsAnimating(true);
        setSwipeDirection(direction);

        // Adicionar à fila de treinamento assíncrono
        queueTraining(movie.id, movie.title, liked);

        const newCount = feedbackCount + 1;
        setFeedbackCount(newCount);
        saveSession({
            movies,
            currentIndex: index - 1,
            feedbackCount: newCount,
            currentPage: currentPage.current,
        });

        if (newCount >= 10) {
            try {
                setIsLoadingRecommendation(true);
                
                // Aguardar que pelo menos 8 das 10 requisições de treinamento sejam completadas
                // antes de gerar recomendação (permite margem para requests em andamento)
                const waitForTraining = async () => {
                    const maxWaitTime = 10000; // 10 segundos máximo
                    const startTime = Date.now();
                    
                    while (completedTrainingRequests < Math.max(8, newCount - 2)) {
                        if (Date.now() - startTime > maxWaitTime) {
                            console.warn("⚠️ Timeout aguardando treinamentos. Gerando recomendação mesmo assim.");
                            break;
                        }
                        await new Promise(resolve => setTimeout(resolve, 200));
                    }
                };

                await waitForTraining();
                
                // Get recommendation and show match modal
                const recommendationData = await getRecommendation();
                setRecommendedMovie(recommendationData);

                // Reset feedback count and training counters
                setFeedbackCount(0);
                setCompletedTrainingRequests(0);
                setPendingTrainingRequests(0);
                
                saveSession({
                    movies,
                    currentIndex: index - 1,
                    feedbackCount: 0,
                    currentPage: currentPage.current,
                });

                // Show match modal after loading finishes
                setTimeout(() => {
                    setIsLoadingRecommendation(false);
                    setShowMatchModal(true);
                }, 300);
            } catch (err) {
                setIsLoadingRecommendation(false);
                setErrorMessage("Erro ao gerar recomendação. Tente novamente.");
                setShowErrorModal(true);
            }

            // Release animation and reset direction
            setTimeout(() => {
                setIsAnimating(false);
                setSwipeDirection(null);
            }, 300);

            setCurrentIndex(index - 1);
            return;
        }

        // Only decrement here if no recommendation was generated
        setTimeout(() => {
            setCurrentIndex((prev) => prev - 1);
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

    const handleCloseModal = () => {
        setShowMatchModal(false);
    };

    const skipMovie = () => {
        if (!canSwipe || currentIndex < 0) return;

        setIsAnimating(true);
        setSwipeDirection('up'); // Direção diferente para indicar skip

        // Não conta como feedback, apenas pula para o próximo filme
        setTimeout(() => {
            setCurrentIndex((prev) => prev - 1);
            setIsAnimating(false);
            setSwipeDirection(null);
        }, 300);

        // Atualiza a sessão sem incrementar feedbackCount
        saveSession({
            movies,
            currentIndex: currentIndex - 1,
            feedbackCount,
            currentPage: currentPage.current,
        });
    };

    const surprise = async () => {
        setIsLoadingRecommendation(true);

        const randomMovie = await getRecommendation();
        setRecommendedMovie(randomMovie);
        setTimeout(() => {
            setIsLoadingRecommendation(false);
            setShowMatchModal(true);
        }, 300);
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
            <div className="flex flex-col md:flex-row h-screen">
                <div
                    className={`${isMobile ? "h-16" : "md:w-64 md:min-h-screen"
                        }`}
                >
                    <Navbar />
                </div>

                <main className="flex-1 flex flex-col items-center justify-center">
                    <div className="text-xl font-semibold mb-4 text-primary">
                        Carregando filmes...
                    </div>
                    <div className="w-16 h-16 border-t-4 border-accent border-solid rounded-full animate-spin"></div>
                </main>
            </div>
        );
    }

    return (
        <div className="flex flex-col md:flex-row h-screen overflow-hidden">
            {/* Navbar - Fixed height for mobile, fixed width for desktop */}
            <div
                className={`${isMobile ? "" : "md:w-64 md:min-h-screen"
                    }`}
            >
                <Navbar />
            </div>

            {/* Conteúdo principal */}
            <main className="flex-1 flex flex-col h-[calc(100vh-4rem)] md:h-screen overflow-hidden pb-12 md:pb-0">
                <div className="flex-1 flex flex-col overflow-hidden">
                    {/* Barra de progresso com indicador de treinamento */}
                    <div className="relative h-1 w-full bg-foreground">
                        <div
                            className="absolute h-1 bg-accent transition-all duration-300 ease-in-out"
                            style={{
                                width: `${(feedbackCount / 10) * 100}%`,
                            }}
                        ></div>
                        {/* Indicador de treinamentos pendentes */}
                        {pendingTrainingRequests > completedTrainingRequests && (
                            <div className="absolute -top-6 right-2 text-xs text-secondary flex items-center gap-1">
                                <div className="w-2 h-2 bg-yellow-500 rounded-full animate-pulse"></div>
                                <span>Treinando IA ({completedTrainingRequests}/{pendingTrainingRequests})</span>
                            </div>
                        )}
                    </div>

                    {/* Área de cards de filmes */}
                    <div
                        className="flex-1 flex items-center justify-center relative touch-manipulation"
                        {...handlers}
                    >
                        <div className="relative flex items-center justify-center w-full h-full">
                            {currentIndex >= 0 ? (
                                movies.map((movie, index) => {
                                    const isTop = index === currentIndex;
                                    const isNext =
                                        index === currentIndex - 1;

                                    // Só renderizar o cartão atual e o próximo
                                    if (!isTop && !isNext) return null;

                                    return (
                                        <TinderCard
                                            ref={
                                                isTop
                                                    ? currentMovieRef
                                                    : null
                                            }
                                            key={`${movie.id}-${index}`}
                                            onSwipe={(dir) =>
                                                swiped(dir, index)
                                            }
                                            preventSwipe={["up", "down"]}
                                            className="absolute inset-0 flex items-center justify-center"
                                        >
                                            <div
                                                className={`w-[90vw] max-w-md ${isNext
                                                    ? "scale-90 translate-y-4 transition-all duration-300"
                                                    : ""
                                                    }`}
                                            >
                                                <ImprovedMovieCard
                                                    movie={movie}
                                                    isActive={isTop}
                                                    isAnimating={
                                                        isAnimating && isTop
                                                    }
                                                    swipeDirection={
                                                        isTop
                                                            ? swipeDirection
                                                            : null
                                                    }
                                                    className="h-[60vh] md:h-[70vh]"
                                                />
                                            </div>
                                        </TinderCard>
                                    );
                                })
                            ) : loading ? (
                                // Loading quando não há filmes e está carregando
                                <div className="text-center p-8">
                                    <div className="w-16 h-16 border-t-4 border-accent border-solid rounded-full animate-spin mx-auto mb-4"></div>
                                    <h2 className="text-xl font-semibold text-primary mb-2">
                                        Carregando mais filmes...
                                    </h2>
                                    <p className="text-secondary">
                                        Encontrando filmes perfeitos para você
                                    </p>
                                </div>
                            ) : (
                                // Fallback caso algo dê errado
                                <div className="text-center p-8 bg-foreground rounded-lg shadow">
                                    <h2 className="text-2xl font-bold mb-4 text-primary">
                                        Oops! Algo deu errado
                                    </h2>
                                    <p className="text-secondary mb-4">
                                        Não conseguimos carregar mais filmes.
                                    </p>
                                    <button
                                        onClick={loadMoreMovies}
                                        className="px-6 py-3 bg-accent text-background rounded-lg hover:bg-accent/90 font-medium transition-colors"
                                    >
                                        Tentar Novamente
                                    </button>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Botões de interação - Posicionados no fundo da tela com padding para evitar sobreposição da navbar mobile */}
                    <div className="flex space-x-4 py-4 md:py-2 justify-center pb-24 md:pb-4">
                        <button
                            onClick={skipMovie}
                            disabled={!canSwipe}
                            className="w-14 h-14 flex items-center justify-center bg-foreground rounded-full shadow-lg hover:scale-105 transition disabled:opacity-50"
                            title="Pular filme"
                        >
                            <ArrowRightIcon className="w-6 h-6 text-accent" />
                        </button>
                        <button
                            onClick={() => swipe("left")}
                            className="w-14 h-14 flex items-center justify-center bg-foreground rounded-full shadow-lg hover:scale-105 transition disabled:opacity-50"
                            disabled={!canSwipe}
                            title="Descurtir"
                        >
                            <XMarkIcon className="w-6 h-6 text-accent" />
                        </button>
                        <button
                            onClick={() => swipe("right")}
                            className="w-14 h-14 flex items-center justify-center bg-foreground rounded-full shadow-lg hover:scale-105 transition disabled:opacity-50"
                            disabled={!canSwipe}
                            title="Curtir"
                        >
                            <HeartIcon className="w-6 h-6 text-accent" />
                        </button>
                        <button
                            onClick={surprise}
                            className="w-14 h-14 flex items-center justify-center bg-foreground rounded-full shadow-lg hover:scale-105 transition"
                            disabled={!canSwipe}
                            title="Surpresa"
                        >
                            <SparklesIcon className="w-6 h-6 text-accent" />
                        </button>
                    </div>
                </div>

                {/* Overlay de carregamento com informações do treinamento */}
                <div
                    className={`fixed inset-0 flex flex-col bg-background items-center justify-center transition-opacity duration-500 ${isLoadingRecommendation
                        ? "opacity-100 z-50"
                        : "opacity-0 pointer-events-none"
                        }`}
                >
                    <div className="text-xl font-semibold text-primary mb-2">
                        Gerando recomendação...
                    </div>
                    <div className="text-sm text-secondary mb-4">
                        IA treinada com {completedTrainingRequests} avaliações
                    </div>
                    <div className="mt-4 w-16 h-16 border-t-4 border-yellow-500 border-solid rounded-full animate-spin"></div>
                </div>

                {/* Movie match modal */}
                <MovieMatchModal
                    isOpen={showMatchModal}
                    onClose={handleCloseModal}
                    movie={recommendedMovie}
                    onNavigate={() => {
                        router.push(`/movie/${recommendedMovie.id}`);
                    }}
                />

                {/* Error Modal */}
                <ErrorModal
                    isOpen={showErrorModal}
                    onClose={handleCloseErrorModal}
                    message={errorMessage}
                />
            </main>
        </div>
    );
}
