import { getCurrentUser, getToken } from "./auth";
import { movieCache } from "./cache";

// Função auxiliar para obter o token
const getAuthToken = () => {
    return getToken();
};

// Função auxiliar para obter userId
export const getUserId = () => {
    const user = getCurrentUser();
    return user?.userId || null;
};

// Headers padrão para requisições autenticadas
const getAuthHeaders = () => {
    const token = getAuthToken();
    return {
        "Content-Type": "application/json",
        ...(token && { "Authorization": `Bearer ${token}` }),
    };
};

export const fetchMoviesToRate = async (page = 1) => {
    const token = getAuthToken();
    if (!token) {
        throw new Error('Token não encontrado');
    }

    // Usamos o token como chave do cache
    if (page === 1) {
        const cached = movieCache.get(token);
        if (cached) {
            console.log("🔄 Usando cache");
            return cached;
        }
    }

    console.log("📡 Buscando filmes da API - página", page);

    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/feed`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify({ page }),
    });

    if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

    const { movies } = await res.json();
    const processed = movies.map((movie) => ({
        id: movie.id,
        title: movie.title,
        image: `https://image.tmdb.org/t/p/original${movie.poster_path}`,
        description: movie.overview,
        release_date: movie.release_date,
        vote_average: movie.vote_average,
        popularity: movie.popularity,
        original_language: movie.original_language,
    }));

    console.log(`🔍 ${processed.length} filmes encontrados na página ${page}`);
    console.log(processed)

    if (page === 1) movieCache.store(token, processed);

    return processed;
};

export const getMovieRating = async (movieId) => {
    try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/rate/${movieId}`, {
            method: "GET",
            headers: getAuthHeaders(),
        });

        if (res.ok) {
            const data = await res.json();
            return data.currentRating;
        } else if (res.status === 404) {
            return null;
        } else {
            const err = await res.json();
            console.error("Erro na resposta da API:", err);
            throw new Error(err.error || "Erro ao buscar rating");
        }
    } catch (err) {
        console.error("Erro ao buscar rating:", err);
        throw err;
    }
};

export const setMovieRate = async (movieId, rating) => {
    try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/rate`, {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify({
                movieId: parseInt(movieId),
                rating: rating,
            }),
        });

        if (res.ok) {
            const data = await res.json();
            console.log("Avaliação enviada com sucesso:", data);
            return data;
        } else {
            const err = await res.json();
            console.error("Erro na resposta da API:", err);
            throw new Error(err.error || "Erro na requisição");
        }
    } catch (err) {
        console.error("Erro ao enviar avaliação:", err);
        throw err;
    }
};

export const removeMovieRating = async (movieId) => {
    try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/rate/${movieId}`, {
            method: "DELETE",
            headers: getAuthHeaders(),
        });

        if (res.ok) {
            const data = await res.json();
            return data;
        } else {
            const err = await res.json();
            console.error("Erro na resposta da API:", err);
            throw new Error(err.error || "Erro ao remover rating");
        }
    } catch (err) {
        console.error("Erro ao remover rating:", err);
        throw err;
    }
};

export const getRecommendation = async () => {
    console.log("🔁 Gerando surpresa...");

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/recommendation`,
            {
                method: "GET",
                headers: getAuthHeaders(),
            }
        );

        const data = await res.json();

        if (data.erro === "Não há filmes não avaliados para recomendar.") {
            return;
        } else if (!res.ok) {
            console.error("Erro na resposta da API:", data);
            throw new Error(data.error || "Erro ao gerar recomendação");
        }

        return data;
    } catch (error) {
        console.error("Erro ao buscar recomendação:", error);
        throw error;
    }
};

export const fetchRecommendations = async (userId) => {
    // Se userId não for fornecido, pegar do token
    const targetUserId = userId || getUserId();
    if (!targetUserId) {
        throw new Error('UserId não encontrado');
    }

    console.log(`📡 Buscando histórico de recomendações para usuário ${targetUserId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/recommendations/${targetUserId}`,
            {
                method: "GET",
                headers: getAuthHeaders(),
            }
        );

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();
        console.log("📬 Histórico de recomendações recebido:", data);

        if (data.status === "ok" && Array.isArray(data.movies)) {
            console.log(`🔍 ${data.movies.length} recomendações encontradas`);
            return data.movies;
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            return [];
        }
    } catch (error) {
        console.error("❌ Erro ao buscar recomendações:", error);
        throw error;
    }
};

export const fetchUsers = async () => {
    console.log("📡 Buscando usuários da plataforma");

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/users`,
            {
                method: "GET",
                headers: getAuthHeaders(),
            }
        );

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        if (data.status === "ok" && Array.isArray(data.users)) {
            console.log(`🔍 ${data.users.length} usuários encontrados`);
            return data.users;
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            return [];
        }
    } catch (error) {
        console.error("❌ Erro ao buscar usuários:", error);
        throw error;
    }
};

export const fetchPrivate = async () => {
    console.log(`📡 Buscando informações do usuário`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/private`,
            {
                method: "GET",
                headers: getAuthHeaders(),
            }
        );

        if (!res.ok) {
            throw new Error(`Erro na API: ${res.status}`);
        }

        const data = await res.json();

        if (data.status === "ok") {
            let userData = {};

            if (data.user) {
                userData = { ...data.user };
            }

            if (data.preferredGenres && Array.isArray(data.preferredGenres)) {
                userData.genres = data.preferredGenres.map((genre) => genre.id);
                userData.preferredGenres = data.preferredGenres;
            }

            console.log(`🔍 Informações do usuário carregadas`);
            return userData;
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            throw new Error("Formato de resposta inválido do servidor");
        }
    } catch (error) {
        console.error(`❌ Erro ao buscar informações do usuário:`, error);
        throw error;
    }
};

export const updateMyProfile = async (profileData) => {
    console.log(`📝 Atualizando perfil do usuário`);
    console.log("Dados do perfil:", profileData);
    
    const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/profile/update`,
        {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify(profileData),
        }
    );

    if (!res.ok) throw new Error("Erro ao atualizar perfil.");

    return await res.json();
};

export const fetchUserProfile = async (userId) => {
    console.log(`📡 Buscando informações básicas do usuário: ${userId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/profile/${userId}`,
            {
                method: "GET",
                headers: getAuthHeaders(),
            }
        );

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        if (data.status === "ok") {
            console.log(`🔍 Informações básicas do usuário ${userId} carregadas`);
            return data.user;
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            throw new Error("Formato de resposta inválido do servidor");
        }
    } catch (error) {
        console.error(`❌ Erro ao buscar informações do usuário ${userId}:`, error);
        throw error;
    }
};

export const fetchUserWatchList = async (userId) => {
    console.log(`📡 Buscando filmes recentes do usuário: ${userId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/profile/${userId}/watchlist`,
            {
                method: "GET",
                headers: getAuthHeaders(),
            }
        );

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        if (data.status === "ok") {
            console.log(`🔍 Filmes recentes do usuário ${userId} carregados`);
            return data.movies || [];
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            return [];
        }
    } catch (error) {
        console.error(`❌ Erro ao buscar filmes recentes do usuário ${userId}:`, error);
        return [];
    }
};

export const fetchUserFavorites = async (userId) => {
    console.log(`📡 Buscando filmes favoritos do usuário: ${userId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/profile/${userId}/favorites`,
            {
                method: "GET",
                headers: getAuthHeaders(),
            }
        );

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        if (data.status === "ok") {
            console.log(`🔍 Filmes favoritos do usuário ${userId} carregados`);
            return data.movies || [];
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            return [];
        }
    } catch (error) {
        console.error(`❌ Erro ao buscar filmes favoritos do usuário ${userId}:`, error);
        return [];
    }
};

export const checkMovieWatchlist = async (movieId) => {
    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/movie/${movieId}/watchlist`,
            {
                method: "GET",
                headers: getAuthHeaders(),
            }
        );
        
        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);
        
        const data = await res.json();
        return data.isInWatchlist;
    } catch (error) {
        console.error("❌ Erro ao verificar watchlist:", error);
        return false;
    }
};

export const checkMovieFavorite = async (movieId) => {
    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/movie/${movieId}/favorite`,
            {
                method: "GET",
                headers: getAuthHeaders(),
            }
        );
        
        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);
        
        const data = await res.json();
        return data.isFavorite;
    } catch (error) {
        console.error("❌ Erro ao verificar favorito:", error);
        return false;
    }
};

export const updateWatchlistMovie = async (movieId, watched) => {
    console.log(`📝 Adicionando filme à watchlist: ${movieId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/recommendation/watched`,
            {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify({ movieId, watched }),
            }
        );

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        if (data.status === "ok") {
            console.log(`🔍 Filme ${movieId} adicionado à watchlist com sucesso`);
            return true;
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            return false;
        }
    } catch (error) {
        console.error(`❌ Erro ao adicionar filme ${movieId} à watchlist:`, error);
        return false;
    }
};

export const updatefavoriteMovie = async (movieId, favorite) => {
    console.log(`⭐ Favoritando filme: ${movieId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/recommendation/favorite`,
            {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify({ movieId, favorite }),
            }
        );

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        if (data.status === "ok") {
            console.log(`🔍 Filme ${movieId} favoritado com sucesso`);
            return true;
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            return false;
        }
    } catch (error) {
        console.error(`❌ Erro ao favoritar filme ${movieId}:`, error);
        return false;
    }
};

export const deleteMovieRecommendation = async (movieId) => {
    console.log(`🗑️ Deletando filme: ${movieId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/recommendation/delete`,
            {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify({ movieId }),
            }
        );

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        if (data.status === "ok") {
            console.log(`🔍 Filme ${movieId} deletado com sucesso`);
            return true;
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            return false;
        }
    } catch (error) {
        console.error(`❌ Erro ao deletar filme ${movieId}:`, error);
        return false;
    }
};

export const fetchMovieById = async (movieId) => {
    console.log(`📡 Buscando informações completas do filme: ${movieId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/movie/${movieId}/details`,
            {
                method: "GET",
                headers: getAuthHeaders(),
            }
        );

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        console.log("📦 Dados recebidos da API:", data);

        if (data.movieData) {
            return {
                movie: data.movieData,
                watched: data.watched,
                rating: data.rating
            };
        } else {
            console.warn("⚠️ Formato de resposta inesperado:", data);
            throw new Error("Formato de resposta inválido do servidor");
        }

    } catch (error) {
        console.error(`❌ Erro ao buscar informações do filme ${movieId}:`, error);
        throw error;
    }
};

export const fetchMovies = async (query, page = 1, limit = 25) => {
    console.log(`📡 Buscando filmes: query="${query}", página=${page}`);

    try {
        const params = new URLSearchParams({
            query,
            page: page.toString(),
            limit: limit.toString(),
        });

        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/movies/search?${params.toString()}`,
            {
                method: "GET",
                headers: getAuthHeaders(),
            }
        );

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        if (data.status === "ok" && Array.isArray(data.results)) {
            console.log(`🔍 ${data.results.length} filmes encontrados`);
            return data;
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            return { results: [], total: 0 };
        }
    } catch (error) {
        console.error("❌ Erro ao buscar filmes:", error);
        throw error;
    }
};

export const checkMovieRecommended = async (movieId) => {
    console.log(`📡 Verificando se filme ${movieId} está recomendado`);
    
    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/movie/${movieId}/recommended`,
            {
                method: "GET",
                headers: getAuthHeaders(),
            }
        );
        
        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);
        
        const data = await res.json();
        console.log(`🔍 Filme ${movieId} recomendado:`, data.isRecommended);
        
        return data.isRecommended;
    } catch (error) {
        console.error("❌ Erro ao verificar recomendação:", error);
        return false;
    }
};

export const verifyUser = async () => {
    console.log("🔍 Verificando usuário autenticado");

    try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/verify`, {
            method: "GET",
            headers: getAuthHeaders(),
        });

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();
        console.log("✅ Usuário verificado:", data);

        return data;
    } catch (error) {
        console.error("❌ Erro ao verificar usuário:", error);
        throw error;
    }
};