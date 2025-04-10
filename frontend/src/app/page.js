"use client";

import MovieCard from "@/components/MovieCard";
import Navbar from "@/components/Navbar";
import Image from "next/image";
import { useEffect, useRef, useState } from "react";
import { useSwipeable } from "react-swipeable";
import TinderCard from "react-tinder-card";

// Dados de exemplo (em produção, você buscaria do backend)
const SAMPLE_MOVIES = [
    {
        id: 1, //  data (will be replaced with API calls)

        title: "Carros",
        studio: "Disney • Pixar",
        image: "/movies/cars.jpg",
        description:
            "Um carro de corrida aprende que a vida é mais que fama e vitórias.",
    },
    {
        id: 2,
        title: "Toy Story",
        studio: "Disney • Pixar",
        image: "/movies/toystory.jpg",
        description: "Brinquedos ganham vida quando ninguém está olhando.",
    },
    {
        id: 3,
        title: "Divertida Mente",
        studio: "Disney • Pixar",
        image: "/movies/insideout.jpg",
        description:
            "As emoções de uma menina ganham vida enquanto ela se adapta a uma nova cidade.",
    },
    {
        id: 4,
        title: "Ratatouille",
        studio: "Disney • Pixar",
        image: "/movies/ratatouille.jpg",
        description: "Um rato sonha em se tornar um grande chef em Paris.",
    },
];

export default function Home() {
    const [movies, setMovies] = useState(SAMPLE_MOVIES);
    const [currentIndex, setCurrentIndex] = useState(0);
    const [swipeDirection, setSwipeDirection] = useState(null);
    const [loadingProgress, setLoadingProgress] = useState(25); // progresso da barra de loading (25% = 1/4 filmes)
    const [isAnimating, setIsAnimating] = useState(false);

    const currentMovieRef = useRef(null);
    const canSwipe = currentIndex >= 0 && !isAnimating;

    const updateLoadingProgress = (index) => {
        const progress = ((index + 1) / movies.length) * 100;
        setLoadingProgress(progress);
    };

    useEffect(() => {
        updateLoadingProgress(currentIndex);
    }, [currentIndex]);

    // Funções de swipe
    const swiped = (direction, index) => {
        setIsAnimating(true);
        setSwipeDirection(direction);

        // Processar like/dislike
        const action = direction === "right" ? "liked" : "disliked";
        console.log(`Você ${action} o filme: ${movies[index].title}`);

        setTimeout(() => {
            setCurrentIndex((prevIndex) => prevIndex - 1);
            setSwipeDirection(null);
            setIsAnimating(false);
        }, 300);
    };

    const outOfFrame = (idx) => {
        console.log(`${movies[idx].title} saiu da tela!`);
    };

    // Swipe manual via botões
    const swipe = (dir) => {
        if (!canSwipe) return;

        if (currentMovieRef.current) {
            currentMovieRef.current.swipe(dir);
        }
    };

    // Handlers para gesto de swipe via touch
    const handlers = useSwipeable({
        onSwipedLeft: () => canSwipe && swipe("left"),
        onSwipedRight: () => canSwipe && swipe("right"),
        preventDefaultTouchmoveEvent: true,
        trackMouse: true,
    });

    return (
        <main className="min-h-screen pb-16 md:pb-4">
            <div className="flex flex-col h-screen overflow-hidden bg-gray-100">
                {/* Barra de progresso superior */}
                <div className="relative h-1 w-full bg-gray-200">
                    <div
                        className="absolute h-1 bg-black"
                        style={{ width: `${loadingProgress}%` }}
                    ></div>
                </div>

                {/* Logo */}
                <div className="flex justify-center py-4">
                    <div className="w-10 h-10">
                        <Image
                            src="/flixmate-logo.svg"
                            alt="Flixmate Logo"
                            width={40}
                            height={40}
                        />
                    </div>
                </div>

                {/* Área principal com cards */}
                <div className="flex-1 relative overflow-hidden" {...handlers}>
                    <div className="absolute inset-0 flex items-center justify-center">
                        {currentIndex >= 0 ? (
                            movies.map(
                                (movie, index) =>
                                    index === currentIndex && (
                                        <TinderCard
                                            ref={
                                                currentIndex === index
                                                    ? currentMovieRef
                                                    : null
                                            }
                                            key={movie.id}
                                            onSwipe={(dir) =>
                                                swiped(dir, index)
                                            }
                                            onCardLeftScreen={() =>
                                                outOfFrame(index)
                                            }
                                            preventSwipe={["up", "down"]}
                                            className="absolute"
                                        >
                                            <MovieCard movie={movie} />
                                        </TinderCard>
                                    )
                            )
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
                                        setMovies(SAMPLE_MOVIES);
                                        setCurrentIndex(
                                            SAMPLE_MOVIES.length - 1
                                        );
                                    }}
                                    className="px-4 py-2 bg-black text-white rounded-lg"
                                >
                                    Recomeçar
                                </button>
                            </div>
                        )}
                    </div>
                </div>

                {/* Botões de ação */}
                <div className="p-4 flex justify-between items-center">
                    <div className="flex space-x-4 mx-auto">
                        <button
                            onClick={() => swipe("left")}
                            className="w-16 h-16 flex items-center justify-center bg-white rounded-full shadow-lg focus:outline-none"
                            disabled={!canSwipe}
                        >
                            {/* <ThumbDownIcon className="h-8 w-8 text-gray-500" /> */}
                        </button>

                        <button
                            onClick={() => swipe("right")}
                            className="w-16 h-16 flex items-center justify-center bg-white rounded-full shadow-lg focus:outline-none"
                            disabled={!canSwipe}
                        >
                            {/* <ThumbUpIcon className="h-8 w-8 text-gray-500" /> */}
                        </button>

                        <button className="w-16 h-16 flex items-center justify-center bg-white rounded-full shadow-lg focus:outline-none">
                            {/* <SunIcon className="h-8 w-8 text-gray-500" /> */}
                        </button>
                    </div>
                </div>
            </div>
            <Navbar />
        </main>
    );
}
