"use client";

import { useEffect, useRef, useState } from "react";
import { useSwipeable } from "react-swipeable";
import TinderCard from "react-tinder-card";
import MovieCard from "@/components/MovieCard";
import Navbar from "@/components/Navbar";
import Image from "next/image";

const API_KEY = "17ecd463e6a7525a5e55127d3729508d";
const TMDB_URL = "https://api.themoviedb.org/3";

const fetchPopularMovies = async (page = 1) => {
    const res = await fetch(
        `${TMDB_URL}/movie/popular?api_key=${API_KEY}&language=pt-BR&page=${page}`
    );
    const data = await res.json();
    return data.results.map((movie) => ({
        id: movie.id,
        title: movie.title,
        studio: "TMDb",
        image: `https://image.tmdb.org/t/p/w500${movie.poster_path}`,
        description: movie.overview,
    }));
};

const gerarRecomendacao = async () => {
    try {
        const res = await fetch("http://localhost:8080/api/recomendacao", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                userId: 1 // substitua pelo ID do usuário
            })
        });

        if (res.ok) {
            const recomendacao = await res.json();
            console.log("Nova recomendação recebida:", recomendacao);
            // Aqui você pode redirecionar ou mostrar a recomendação se quiser
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
    const currentMovieRef = useRef(null);
    const canSwipe = currentIndex >= 0 && !isAnimating;

    const updateProgress = (index) => {
        const seen = movies.length - (index + 1);
        const progress = (seen / movies.length) * 100;
        setLoadingProgress(progress);
    };

    useEffect(() => {
        const loadMovies = async () => {
            const page = Math.floor(Math.random() * 5) + 1;
            const fetched = await fetchPopularMovies(page);
            const reversed = fetched.reverse();
            setMovies(reversed);
            setCurrentIndex(reversed.length - 1);
            updateProgress(reversed.length - 1); // progresso inicial
        };
        loadMovies();
    }, []);

    useEffect(() => {
        updateProgress(currentIndex);
    }, [currentIndex, movies.length]);

    const swiped = async (direction, index) => {
        setIsAnimating(true);
    
        const value = direction === "right" ? true : false;
        const movie = movies[index];
    
        console.log(`Você ${value === true ? "curtiu" : "descartou"}: ${movie.title}`);
    
        // 1. Enviar interação para o backend
        try {
            await fetch("http://localhost:6789/api/feedback", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    userId: 1, // substitua pelo ID real do usuário logado
                    movieId: movie.id,
                    interaction: value
                })
            });
        } catch (error) {
            console.error("Erro ao enviar interação:", error);
        }
    
        // 2. Atualizar contador
        setInteractionCount((prev) => {
            const updated = prev + 1;
    
            // 3. Se chegou a 20, chama o backend para gerar recomendação
            if (updated >= 10) {
                gerarRecomendacao();
                return 0; // zera para o próximo ciclo
            }
    
            return updated;
        });
    
        // 4. Avança para o próximo card
        setTimeout(() => {
            setCurrentIndex((prev) => prev - 1);
            setIsAnimating(false);
        }, 300);
    };

    const swipe = (dir) => {
        if (canSwipe && currentMovieRef.current) {
            currentMovieRef.current.swipe(dir);
        }
    };

    const outOfFrame = (idx) => {
        console.log(`${movies[idx]?.title} saiu da tela.`);
    };

    const handlers = useSwipeable({
        onSwipedLeft: () => swipe("left"),
        onSwipedRight: () => swipe("right"),
        preventDefaultTouchmoveEvent: true,
        trackMouse: true,
    });

    const resetMatches = async () => {
        const fetched = await fetchPopularMovies(Math.floor(Math.random() * 5) + 1);
        const reversed = fetched.reverse();
        setMovies(reversed);
        setCurrentIndex(reversed.length - 1);
        updateProgress(reversed.length - 1);
    };

    return (
        <main className="min-h-screen pb-16 md:pb-4">
            <div className="flex flex-col h-screen overflow-hidden bg-gray-100">

                {/* Logo */}
                <div className="flex justify-center py-4">
                    <Image src="/flixmate-logo.svg" alt="Flixmate Logo" width={40} height={40} />
                </div>

                {/* Barra de progresso */}
                <div className="relative h-1 w-full bg-gray-200">
                    <div
                        className="absolute h-1 bg-black transition-all duration-300 ease-in-out"
                        style={{ width: `${loadingProgress}%` }}
                    ></div>
                </div>

                {/* Área principal */}
                <div className="flex-1 relative overflow-hidden" {...handlers}>
                    <div className="absolute inset-0 flex items-center justify-center">
                        {currentIndex >= 0 ? (
                            movies.map((movie, index) => {
                                const isTop = index === currentIndex;
                                const isNext = index === currentIndex - 1;
                                if (!isTop && !isNext) return null;

                                return (
                                    <TinderCard
                                        ref={isTop ? currentMovieRef : null}
                                        key={movie.id}
                                        onSwipe={(dir) => swiped(dir, index)}
                                        onCardLeftScreen={() => outOfFrame(index)}
                                        preventSwipe={["up", "down"]}
                                        className={`absolute transition-all duration-300 ease-in-out ${
                                            isNext ? "scale-90 opacity-80 translate-y-4" : ""
                                        }`}
                                    >
                                        <MovieCard movie={movie} />
                                    </TinderCard>
                                );
                            })
                        ) : (
                            <div className="text-center p-8 bg-white rounded-lg shadow">
                                <h2 className="text-2xl font-bold mb-4">Sem mais filmes!</h2>
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

                {/* Botões */}
                <div className="p-4 flex justify-center items-center space-x-4">
                    <button
                        onClick={resetMatches}
                        className="w-16 h-16 flex items-center justify-center bg-white rounded-full shadow-lg hover:scale-105 transition disabled:opacity-50"
                        disabled={!canSwipe}
                        title="Reiniciar"
                    >
                        🔁
                    </button>
                    <button
                        onClick={() => swipe("left")}
                        className="w-16 h-16 flex items-center justify-center bg-white rounded-full shadow-lg hover:scale-105 transition disabled:opacity-50"
                        disabled={!canSwipe}
                        title="Descurtir"
                    >
                        ❌
                    </button>
                    <button
                        onClick={() => swipe("right")}
                        className="w-16 h-16 flex items-center justify-center bg-white rounded-full shadow-lg hover:scale-105 transition disabled:opacity-50"
                        disabled={!canSwipe}
                        title="Curtir"
                    >
                        ❤️
                    </button>
                </div>
            </div>
            <Navbar />
        </main>
    );
}