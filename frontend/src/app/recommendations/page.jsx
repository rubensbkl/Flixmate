"use client";

import Header from "@/components/Header";
import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
// Importe as funções da API
import {
    deleteMovie,
    fetchRecommendations,
    updatefavoriteMovie, // Renomeado para corresponder ao nome no arquivo api.js
    updateWatchlistMovie,
} from "@/lib/api";
import { useEffect, useState } from "react";

import {
    ClockIcon as ClockOutline,
    StarIcon as StarOutline,
    TrashIcon as TrashOutline,
} from "@heroicons/react/24/outline";

import {
    ClockIcon as ClockSolid,
    StarIcon as StarSolid
} from "@heroicons/react/24/solid";

export default function HistoricoPage() {
    const [activeIcons, setActiveIcons] = useState({});
    const [recomendacoes, setRecomendacoes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Função toggleIcon atualizada para chamar a API
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
                case "star": // 'star' corresponde a 'favorite' na API
                    success = await updatefavoriteMovie(movieId);
                    break;
                case "clock": // 'clock' corresponde a 'watchlist' na API
                    success = await updateWatchlistMovie(movieId);
                    break;
                case "trash": // 'trash' corresponde a 'delete' na API
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
            if (!success && iconType !== 'trash') { // Não reverte se a deleção falhar (já foi removido otimisticamente)
                console.error(`Falha ao atualizar ${iconType} para o filme ${movieId}. Revertendo estado.`);
                setActiveIcons((prev) => ({
                    ...prev,
                    [movieId]: { ...prev[movieId], [iconType]: previousIconsState },
                }));
                // Mostrar mensagem de erro para o usuário (opcional)
                // setError(`Falha ao atualizar status do filme ${movieId}.`);
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
            // Mostrar mensagem de erro (opcional)
            // setError(`Erro ao comunicar com o servidor para o filme ${movieId}.`);
        }
    };


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
                // Inicializa o estado activeIcons com base nos dados recebidos (se necessário)
                const initialIcons = {};
                dados.forEach(movie => {
                    initialIcons[movie.id] = {
                        clock: movie.watched ?? false, // Usa ?? para default false se watched for null/undefined
                        star: movie.favorite ?? false, // Usa ?? para default false se favorite for null/undefined
                        trash: false // Trash não tem estado persistente vindo da API
                    };
                });
                setActiveIcons(initialIcons);
                setError(null);
            } catch (err) {
                console.error("Erro ao carregar recomendações:", err);
                setError("Não foi possível carregar o histórico de recomendações.");
                setRecomendacoes([]); // Limpa recomendações em caso de erro
                setActiveIcons({}); // Limpa ícones em caso de erro
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
                            <div className="flex flex-1 justify-center items-center p-8">
                                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
                            </div>
                        ) : error ? (
                            <div className="flex flex-1 justify-center items-center text-red-500 p-4 text-center">
                                {error}
                            </div>
                        ) : recomendacoes.length === 0 ? (
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
                                                {/* Lógica para exibir gêneros (mantida como estava) */}
                                                {movie.genres && movie.genres.length > 0
                                                    ? Array.isArray(movie.genres[0])
                                                        ? movie.genres.map(g => g.name).join(', ')
                                                        : movie.genres.join(', ')
                                                    : movie.genre_ids?.length > 0
                                                        ? 'Carregando gêneros...'
                                                        : 'Sem gêneros'}
                                            </p>
                                        </div>
                                        <div className="flex space-x-2">
                                            {/* Botão Watchlist (Clock) */}
                                            <button
                                                className={`p-1 rounded-full transition-colors duration-200 ${activeIcons[movie.id]?.clock
                                                    ? "text-blue-600 bg-blue-100 hover:bg-blue-200"
                                                    : "text-gray-500 hover:bg-gray-200"
                                                    }`}
                                                onClick={() =>
                                                    toggleIcon(movie.id, "clock") // Mapeado para 'watchlist' na API
                                                }
                                                aria-label={activeIcons[movie.id]?.clock ? "Remover da watchlist" : "Adicionar à watchlist"}
                                            >
                                                {activeIcons[movie.id]?.clock ? (
                                                    <ClockSolid className="w-5 h-5 md:w-6 md:h-6" />
                                                ) : (
                                                    <ClockOutline className="w-5 h-5 md:w-6 md:h-6" />
                                                )}
                                            </button>
                                            {/* Botão Favorito (Star) */}
                                            <button
                                                className={`p-1 rounded-full transition-colors duration-200 ${activeIcons[movie.id]?.star
                                                    ? "text-yellow-400 bg-yellow-100 hover:bg-yellow-200"
                                                    : "text-gray-500 hover:bg-gray-200"
                                                    }`}
                                                onClick={() =>
                                                    toggleIcon(movie.id, "star") // Mapeado para 'favorite' na API
                                                }
                                                aria-label={activeIcons[movie.id]?.star ? "Remover dos favoritos" : "Adicionar aos favoritos"}
                                            >
                                                {activeIcons[movie.id]?.star ? (
                                                    <StarSolid className="w-5 h-5 md:w-6 md:h-6" />
                                                ) : (
                                                    <StarOutline className="w-5 h-5 md:w-6 md:h-6" />
                                                )}
                                            </button>
                                            {/* Botão Deletar (Trash) */}
                                            <button
                                                className={`p-1 rounded-full transition-colors duration-200 ${activeIcons[movie.id]?.trash // Embora não tenha estado real, pode ser usado para feedback visual temporário se desejado
                                                    ? "text-red-600 bg-red-100 hover:bg-red-200"
                                                    : "text-gray-500 hover:bg-gray-200"
                                                    }`}
                                                onClick={() =>
                                                    toggleIcon(movie.id, "trash") // Mapeado para 'delete' na API
                                                }
                                                aria-label="Deletar recomendação"
                                            >
                                                {/* Ícone de lixeira não muda, apenas a cor/fundo */}
                                                <TrashOutline className="w-5 h-5 md:w-6 md:h-6" />
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