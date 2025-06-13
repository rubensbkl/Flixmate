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
            return cached;
        }
    }

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
            throw new Error(err.error || "Erro ao buscar rating");
        }
    } catch (err) {
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
            return data;
        } else {
            const err = await res.json();
            throw new Error(err.error || "Erro na requisição");
        }
    } catch (err) {
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
            throw new Error(err.error || "Erro ao remover rating");
        }
    } catch (err) {
        throw err;
    }
};

export const getRecommendation = async () => {

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
            throw new Error(data.error || "Erro ao gerar recomendação");
        }

        return data;
    } catch (error) {
        throw error;
    }
};

export const fetchRecommendations = async (userId) => {
    // Se userId não for fornecido, pegar do token
    const targetUserId = userId || getUserId();
    if (!targetUserId) {
        throw new Error('UserId não encontrado');
    }

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

        if (data.status === "ok" && Array.isArray(data.movies)) {
            return data.movies;
        } else {
            return [];
        }
    } catch (error) {
        throw error;
    }
};

export const fetchUsers = async () => {

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
            return data.users;
        } else {
            return [];
        }
    } catch (error) {
        throw error;
    }
};

export const fetchPrivate = async () => {

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

            return userData;
        } else {
            throw new Error("Formato de resposta inválido do servidor");
        }
    } catch (error) {
        throw error;
    }
};

export const updateMyProfile = async (profileData) => {
    
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
            return data.user;
        } else {
            throw new Error("Formato de resposta inválido do servidor");
        }
    } catch (error) {
        throw error;
    }
};

export const fetchUserWatchList = async (userId) => {

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
            return data.movies || [];
        } else {
            return [];
        }
    } catch (error) {
        return [];
    }
};

export const fetchUserFavorites = async (userId) => {

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
            return data.movies || [];
        } else {
            return [];
        }
    } catch (error) {
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
        return false;
    }
};

export const updateWatchlistMovie = async (movieId, watched) => {

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
            return true;
        } else {
            return false;
        }
    } catch (error) {
        return false;
    }
};

export const updatefavoriteMovie = async (movieId, favorite) => {

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
            return true;
        } else {
            return false;
        }
    } catch (error) {
        return false;
    }
};

export const deleteMovieRecommendation = async (movieId) => {

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
            return true;
        } else {
            return false;
        }
    } catch (error) {
        return false;
    }
};

export const fetchMovieById = async (movieId) => {

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
        throw error;
    }
};

export const fetchMovies = async (query, page = 1, limit = 25) => {

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
            return data;
        } else {
            return { results: [], total: 0 };
        }
    } catch (error) {
        throw error;
    }
};

export const checkMovieRecommended = async (movieId) => {
    
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
        
        return data.isRecommended;
    } catch (error) {
        return false;
    }
};

export const verifyUser = async () => {

    try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/verify`, {
            method: "GET",
            headers: getAuthHeaders(),
        });

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        return data;
    } catch (error) {
        throw error;
    }
};