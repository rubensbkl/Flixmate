"use client";

import Header from "@/components/Header";
import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import MovieCard from "@/components/RecomendationCard";
import {
    deleteMovie,
    fetchRecommendations,
    updatefavoriteMovie,
    updateWatchlistMovie,
} from "@/lib/api";
import "@/styles/recomendationcard.css";
import { useEffect, useState } from "react";
// Importe o CSS de responsividade (inclua no seu arquivo global.css ou importe aqui)
// import "@/styles/responsive-fixes.css";

export default function HistoricoPage() {
    const [activeIcons, setActiveIcons] = useState({});
    const [recomendacoes, setRecomendacoes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isMobile, setIsMobile] = useState(false);

    // Detectar se estamos em dispositivo móvel
    useEffect(() => {
        const checkMobile = () => {
            setIsMobile(window.innerWidth < 768);
        };
        
        // Verificar no carregamento inicial
        checkMobile();
        
        // Adicionar event listener para redimensionamento
        window.addEventListener('resize', checkMobile);
        
        // Limpar o event listener
        return () => window.removeEventListener('resize', checkMobile);
    }, []);

    // Função toggleIcon para chamar a API
    const toggleIcon = async (movieId, iconType) => {
        // 1. Guarda o estado anterior para possível reversão
        const previousIconsState = activeIcons[movieId]?.[iconType];

        // 2. Atualiza o estado local imediatamente para feedback visual
        setActiveIcons((prev) => {
            const currentMovieIcons = prev[movieId] || {};
            return {
                ...prev,
                [movieId]: {
                    ...currentMovieIcons,
                    [iconType]: !currentMovieIcons[iconType],
                },
            };
        });

        // 3. Envia a requisição para a API com base no tipo de ícone
        try {
            let success = false;
            switch (iconType) {
                case "star":
                    success = await updatefavoriteMovie(movieId);
                    break;
                case "clock":
                    success = await updateWatchlistMovie(movieId);
                    break;
                case "trash":
                    success = await deleteMovie(movieId);
                    // Se deletar com sucesso, remove o filme da lista local
                    if (success) {
                        setRecomendacoes(prev => prev.filter(movie => movie.id !== movieId));
                        console.log(`Filme ${movieId} removido da lista após deleção.`);
                    }
                    break;
                default:
                    console.warn(`Tipo de ícone desconhecido: ${iconType}`);
                    // Reverte o estado se o tipo for inválido
                    setActiveIcons((prev) => ({
                        ...prev,
                        [movieId]: { ...prev[movieId], [iconType]: previousIconsState },
                    }));
                    return;
            }

            // 4. Reverte o estado se a API falhar
            if (!success && iconType !== 'trash') {
                console.error(`Falha ao atualizar ${iconType} para o filme ${movieId}. Revertendo estado.`);
                setActiveIcons((prev) => ({
                    ...prev,
                    [movieId]: { ...prev[movieId], [iconType]: previousIconsState },
                }));
            } else if (success && iconType !== 'trash') {
                 console.log(`Ação '${iconType}' para filme ${movieId} realizada com sucesso.`);
            }
        } catch (err) {
            console.error(`Erro na chamada da API para ${iconType} no filme ${movieId}:`, err);
            // Reverte o estado em caso de erro inesperado na API
             setActiveIcons((prev) => ({
                ...prev,
                [movieId]: { ...prev[movieId], [iconType]: previousIconsState },
            }));
        }
    };

    // Carregar recomendações
    useEffect(() => {
        async function carregarRecomendacoes() {
            try {
                setLoading(true);
                const dados = await fetchRecommendations();

                // Verificar se deu erro na resposta por nao ter filmes
                if (!dados || dados.length === 0) {
                    setRecomendacoes([]);
                    return;
                }

                setRecomendacoes(dados);
                console.log("Dados recebidos:", dados);
                // Inicializa o estado activeIcons com base nos dados recebidos
                const initialIcons = {};
                dados.forEach(movie => {
                    initialIcons[movie.id] = {
                        clock: movie.watched ?? false,
                        star: movie.favorite ?? false,
                        trash: false
                    };
                });
                setActiveIcons(initialIcons);
                setError(null);
            } catch (err) {
                console.error("Erro ao carregar recomendações:", err);
                setError("Não foi possível carregar o histórico de recomendações.");
                setRecomendacoes([]);
                setActiveIcons({});
            } finally {
                setLoading(false);
            }
        }

        carregarRecomendacoes();
    }, []);

    return (
        <ProtectedRoute>
            <div className="bg-gray-100 flex flex-col md:flex-row min-h-screen">
                {/* Navbar - ajustada para mobile/desktop */}
                <div className="md:w-64 md:min-h-screen">
                    <Navbar />
                </div>

                <main className="flex-1 flex flex-col pt-5 md:pt-0 pb-16 md:pb-0 page-content">
                    <Header />

                    <div className="flex flex-col items-center w-full flex-1 overflow-y-auto px-2 md:px-4 scroll-container">
                        <h1 className="text-xl md:text-2xl font-bold py-3 md:py-4">Histórico de Recomendações</h1>

                        {loading ? (
                            <div className="flex flex-1 justify-center items-center p-4 md:p-8">
                                <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-blue-500"></div>
                            </div>
                        ) : error ? (
                            <div className="flex flex-1 justify-center items-center text-red-500 p-4 text-center">
                                {error}
                            </div>
                        ) : recomendacoes.length === 0 ? (
                            <div className="flex flex-1 justify-center items-center text-center p-4 md:p-8 text-gray-500">
                                Você ainda não possui recomendações.
                            </div>
                        ) : (
                            <section className="w-full md:w-[60%] space-y-3 mb-16 md:mb-0 movie-list-container">
                                {recomendacoes.map((movie) => (
                                    <MovieCard
                                        key={movie.id}
                                        movie={movie}
                                        activeIcons={activeIcons}
                                        toggleIcon={toggleIcon}
                                        isMobile={isMobile}
                                    />
                                ))}
                            </section>
                        )}
                    </div>
                </main>
            </div>
        </ProtectedRoute>
    );
}