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

// Cache para armazenar os filmes por usuário
const movieCache = {
    data: {},
    timestamp: {},
    CACHE_DURATION: 30 * 60 * 1000, // 30 minutos em milissegundos
    
    // Verifica se o cache para este usuário está válido
    isValid: function(userId) {
        return (
            this.data[userId] && 
            this.timestamp[userId] && 
            Date.now() - this.timestamp[userId] < this.CACHE_DURATION
        );
    },
    
    // Armazena filmes no cache
    store: function(userId, movies) {
        this.data[userId] = [...movies]; // Clone para evitar mutações
        this.timestamp[userId] = Date.now();
    },
    
    // Obtém filmes do cache
    get: function(userId) {
        return this.isValid(userId) ? [...this.data[userId]] : null;
    },
    
    // Limpa o cache para um usuário específico
    clear: function(userId) {
        delete this.data[userId];
        delete this.timestamp[userId];
    }
};

// Função para remover filmes duplicados baseado no ID
const removeDuplicateMovies = (movies) => {
    const uniqueIds = new Set();
    return movies.filter(movie => {
        if (uniqueIds.has(movie.id)) {
            return false;
        }
        uniqueIds.add(movie.id);
        return true;
    });
};

// Função para buscar filmes da API
const fetchMovies = async (userId) => {
    try {
        // Verifica se temos um cache válido
        const cachedMovies = movieCache.get(userId);
        if (cachedMovies) {
            console.log("Usando filmes em cache");
            return cachedMovies;
        }
        
        console.log("Buscando novos filmes da API");
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/movies`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ userId }),
        });
        
        if (!res.ok) {
            throw new Error(`Erro na API: ${res.status}`);
        }
        
        const data = await res.json();
        
        if (!data.movies || !Array.isArray(data.movies)) {
            throw new Error("Formato de resposta inválido");
        }
        
        // Processa os filmes recebidos
        const processedMovies = data.movies.map((movie) => ({
            id: movie.id,
            title: movie.title,
            studio: "TMDb",
            image: `https://image.tmdb.org/t/p/w500${movie.poster_path}`,
            description: movie.overview,
            release_date: movie.release_date,
            vote_average: movie.vote_average,
            popularity: movie.popularity,
        }));
        
        // Remove duplicatas e armazena no cache
        const uniqueMovies = removeDuplicateMovies(processedMovies);
        movieCache.store(userId, uniqueMovies);
        
        return uniqueMovies;
    } catch (error) {
        console.error("Erro ao buscar filmes:", error);
        return [];
    }
};

const gerarRecomendacao = async (userId) => {
    console.log("🔁 Tentando gerar recomendação...");
    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/recommendation`,
            {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ userId }),
            }
        );

        if (res.ok) {
            const recomendacao = await res.json();
            alert(`Nova recomendação recebida: ${recomendacao.recomendacao}`);
            // Limpa o cache para forçar novo carregamento
            movieCache.clear(userId);
        } else {
            console.error("Erro ao gerar recomendação");
        }
    } catch (error) {
        console.error("Erro de conexão ao gerar recomendação:", error);
    }
};

export default function Home() {
    const [interactionCount, setInteractionCount] = useState(0);
    const [movies, setMovies] = useState([]);
    const [currentIndex, setCurrentIndex] = useState(-1);
    const [loadingProgress, setLoadingProgress] = useState(0);
    const [isAnimating, setIsAnimating] = useState(false);
    const [swipeDirection, setSwipeDirection] = useState(null);
    const [loading, setLoading] = useState(true);
    const currentMovieRef = useRef(null);
    const userId = useRef(null);
    const [isLoadingRecommendation, setIsLoadingRecommendation] = useState(false);
    const [showRecommendationLoader, setShowRecommendationLoader] = useState(false);

    // Inicialize userId logo no início
    useEffect(() => {
        try {
            const user = JSON.parse(localStorage.getItem("user"));
            if (user && user.id) {
                userId.current = user.id;
            } else {
                // Fallback para 1 se não conseguir obter o ID do usuário
                userId.current = 1;
                console.warn("ID do usuário não encontrado, usando fallback");
            }
        } catch (error) {
            console.error("Erro ao obter ID do usuário:", error);
            userId.current = 1;
        }
    }, []);

    const updateProgress = useCallback((count) => {
        const progress = (count / 10) * 100;
        setLoadingProgress(progress);
    }, []);

    // Função para carregar filmes
    const loadMovies = useCallback(async () => {
        if (!userId.current) return;
        
        setLoading(true);
        try {
            const fetchedMovies = await fetchMovies(userId.current);
            
            if (fetchedMovies.length > 0) {
                // Inverte a ordem para que possamos começar do primeiro item
                const reversedMovies = [...fetchedMovies].reverse();
                setMovies(reversedMovies);
                setCurrentIndex(reversedMovies.length - 1);
                updateProgress(0);
            } else {
                setMovies([]);
                setCurrentIndex(-1);
                setLoadingProgress(100);
            }
        } catch (err) {
            console.error("Erro ao carregar filmes:", err);
        } finally {
            setLoading(false);
        }
    }, [updateProgress]);

    // Carrega filmes quando o componente é montado
    useEffect(() => {
        if (userId.current) {
            loadMovies();
        }
    }, [loadMovies]);

    // Atualiza o progresso quando o índice atual muda
    useEffect(() => {
        updateProgress(interactionCount);
    }, [interactionCount, updateProgress]);

    useEffect(() => {
        if (isLoadingRecommendation) {
            setShowRecommendationLoader(true);
        }
    }, [isLoadingRecommendation]);

    // Verifica se pode deslizar
    const canSwipe = currentIndex >= 0 && !isAnimating && !loading && !isLoadingRecommendation;

    // Função executada quando um cartão é deslizado
    const swiped = async (direction, index) => {
        if (!canSwipe) return;
        
        setIsAnimating(true);
        setSwipeDirection(direction);

        const value = direction === "right";
        const movie = movies[index];

        console.log(`Você ${value ? "curtiu" : "descartou"}: ${movie.title}`);

        // Enviar interação para o backend
        try {
            await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/feedback`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    userId: userId.current,
                    movieId: movie.id,
                    interaction: value,
                }),
            });
        } catch (error) {
            console.error("Erro ao enviar interação:", error);
        }

        // Atualiza contador e verifica se precisa gerar recomendação
        const newCount = interactionCount + 1;
        setInteractionCount(newCount);
        
        if (newCount >= 10) {
            setTimeout(() => {
              setIsLoadingRecommendation(true);
            }, 300);
          
            await gerarRecomendacao(userId.current);
          
            // simula tempo de carregamento antes de sumir o loader
            setTimeout(() => {
              setIsLoadingRecommendation(false);
              setShowRecommendationLoader(false);
            }, 1500);
          
            setInteractionCount(0);
        }

        // Avança para o próximo card com atraso para animação
        setTimeout(() => {
            setCurrentIndex((prev) => prev - 1);
            setIsAnimating(false);
            setSwipeDirection(null);
        }, 300);
    };

    // Função para deslizar programaticamente
    const swipe = (dir) => {
        if (canSwipe && currentMovieRef.current) {
            setSwipeDirection(dir);
            currentMovieRef.current.swipe(dir);
        }
    };

    // Quando um cartão sai da tela
    const outOfFrame = (idx) => {
        console.log(`${movies[idx]?.title} saiu da tela.`);
    };

    // Configuração de deslizamento
    const handlers = useSwipeable({
        onSwipedLeft: () => swipe("left"),
        onSwipedRight: () => swipe("right"),
        preventDefaultTouchmoveEvent: true,
        trackMouse: true,
    });

    // Função para resetar e buscar novos filmes
    const resetMatches = async () => {
        // Resetar o contador de interações
        setInteractionCount(0);
        
        // Limpar o cache para forçar o carregamento de novos filmes
        if (userId.current) {
            movieCache.clear(userId.current);
            await loadMovies();
        }
    };



    // Renderização condicional durante o carregamento
    if (loading && movies.length === 0) {
        return (
            <ProtectedRoute>
                <div className="bg-gray-100 md:flex">
                    <Navbar />
                    <main className="flex-1 overflow-hidden flex flex-col h-[calc(100vh-4rem)] md:h-screen items-center justify-center">
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
                                style={{ width: `${loadingProgress}%` }}
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
                                            onClick={resetMatches}
                                            className="px-4 py-2 bg-black text-white rounded-lg hover:bg-gray-900"
                                        >
                                            Buscar Novos Filmes
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
                        showRecommendationLoader ? "opacity-100" : "opacity-0 pointer-events-none"
                    }`}>
                        <div className="text-xl font-semibold">Gerando recomendação...</div>
                        <div className="mt-4 w-16 h-16 border-t-4 border-yellow-500 border-solid rounded-full animate-spin"></div>
                    </div>
                </main>
            </div>

        </ProtectedRoute>
    );
}