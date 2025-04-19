import { movieCache } from "./cache";

// Função auxiliar para obter o token
const getToken = () => {
    return localStorage.getItem("token");
};

export const fetchMovies = async (page = 1) => {
    const token = getToken();

    // Usamos o token como chave do cache em vez do ID
    if (page === 1) {
        const cached = movieCache.get(token);
        if (cached) {
            console.log("🔄 Usando cache");
            return cached;
        }
    }

    console.log("📡 Buscando filmes da API - página", page);

    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/movies`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ page }),
    });

    if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

    const { movies } = await res.json();
    const processed = movies.map((movie) => ({
        id: movie.id,
        title: movie.title,
        image: `https://image.tmdb.org/t/p/w500${movie.poster_path}`,
        description: movie.overview,
        release_date: movie.release_date,
        vote_average: movie.vote_average,
        popularity: movie.popularity,
        original_language: movie.original_language,
        adult: movie.adult,
    }));

    if (page === 1) movieCache.store(token, processed);

    return processed;
};

export const sendFeedback = async (movieId, liked) => {
    const token = getToken();

    await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/feedback`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
            movieId: movieId,
            feedback: liked,
        }),
    });
};

export const gerarRecomendacao = async () => {
    const token = getToken();
    console.log("🔁 Gerando recomendação...");

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/recommendation`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            }
        );

        const data = await res.json();

        // Mesmo se o status não for 200, verifique se há uma resposta com formato válido
        if (!res.ok) {
            console.error("Erro na resposta da API:", data);
            throw new Error(data.error || "Erro ao gerar recomendação");
        }

        console.log("📬 Recomendação recebida:", data.recomendacao);
        return data.recomendacao;
    } catch (error) {
        console.error("Erro ao buscar recomendação:", error);
        throw error;
    }
};

export const fetchRecommendations = async () => {
    const token = getToken();
    console.log("📡 Buscando histórico de recomendações");

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/recommendations`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({}),
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
    const token = getToken();
    console.log("📡 Buscando usuários da plataforma");

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/users`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
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

export const fetchMe = async () => {
    const token = getToken();
    console.log(`📡 Buscando informações básicas do usuário:`);

    try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/me`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
        });

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        console.log("Dados recebidos:", data); // Adicionei este log para depuração

        if (data.status === "ok") {
            console.log(`🔍 Informações básicas do usuário ${data} carregadas`);
            return data.user;
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            throw new Error("Formato de resposta inválido do servidor");
        }
    } catch (error) {
        console.error(
            `❌ Erro ao buscar informações do usuário ${data}:`,
            error
        );
        throw error;
    }
};

/**
 * Fetch user profile data
 * @returns {Promise<Object>} User data
 */
export const fetchPrivate = async () => {
    const token = getToken();
    console.log(`📡 Buscando informações do usuário`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/private`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            }
        );

        if (!res.ok) {
            throw new Error(`Erro na API: ${res.status}`);
        }

        const data = await res.json();

        if (data.status === "ok") {
            // Handle different API response formats
            let userData = {};

            // If user data is directly in the response
            if (data.user) {
                userData = { ...data.user };
            }

            // If we have preferred genres as objects, extract and add to userData
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
    const token = getToken();
    console.log(`📝 Atualizando perfil do usuário`)
    console.log("Dados do perfil:", profileData); // Adicionei este log para depuração
    const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/profile/update`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(profileData),
        }
    );

    if (!res.ok) throw new Error("Erro ao atualizar perfil.");

    return await res.json();
};

export const fetchUserProfile = async (userId) => {
    const token = getToken();
    console.log(`📡 Buscando informações básicas do usuário: ${userId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/profile/${userId}`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            }
        );

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        console.log("Dados recebidos:", data); // Adicionei este log para depuração

        if (data.status === "ok") {
            console.log(
                `🔍 Informações básicas do usuário ${userId} carregadas`
            );
            return data.user;
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            throw new Error("Formato de resposta inválido do servidor");
        }
    } catch (error) {
        console.error(
            `❌ Erro ao buscar informações do usuário ${userId}:`,
            error
        );
        throw error;
    }
};

export const fetchUserRecents = async (userId) => {
    const token = getToken();
    console.log(`📡 Buscando filmes recentes do usuário: ${userId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/profile/${userId}/watched`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
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
        console.error(
            `❌ Erro ao buscar filmes recentes do usuário ${userId}:`,
            error
        );
        return [];
    }
};

export const fetchUserFavorites = async (userId) => {
    const token = getToken();
    console.log(`📡 Buscando filmes favoritos do usuário: ${userId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/profile/${userId}/favorites`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
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
        console.error(
            `❌ Erro ao buscar filmes favoritos do usuário ${userId}:`,
            error
        );
        return [];
    }
};

export const fetchUserRecommended = async (userId) => {
    const token = getToken();
    console.log(`📡 Buscando filmes recomendados do usuário: ${userId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/profile/${userId}/recommended`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            }
        );

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        console.log("Dados recebidos:", data); // Adicionei este log para depuração

        if (data.status === "ok") {
            console.log(
                `🔍 Filmes recomendados do usuário ${userId} carregados`
            );
            return data.movies || [];
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            return [];
        }
    } catch (error) {
        console.error(
            `❌ Erro ao buscar filmes recomendados do usuário ${userId}:`,
            error
        );
        return [];
    }
};

export const updateWatchlistMovie = async (movieId, watched) => {
    const token = getToken();
    console.log(`📝 Adicionando filme à watchlist: ${movieId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/recommendation/watched`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ movieId, watched }),
            }
        );

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        if (data.status === "ok") {
            console.log(
                `🔍 Filme ${movieId} adicionado à watchlist com sucesso`
            );
            return true;
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            return false;
        }
    } catch (error) {
        console.error(
            `❌ Erro ao adicionar filme ${movieId} à watchlist:`,
            error
        );
        return false;
    }
};

export const updatefavoriteMovie = async (movieId, favorite) => {
    const token = getToken();
    console.log(`⭐ Favoritando filme: ${movieId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/recommendation/favorite`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
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

export const deleteMovie = async (movieId) => {
    const token = getToken();
    console.log(`🗑️ Deletando filme: ${movieId}`);

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/recommendation/delete`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
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

export const resetFeedbacks = async () => {
    const token = getToken();
    console.log("🗑️ Resetando todos os feedbacks do usuário");

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/feedbacks/reset`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            }
        );

        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

        const data = await res.json();

        console.log("Dados recebidos:", data); // Adicionei este log para depuração

        if (data.status === "ok") {
            console.log("🔍 Feedbacks resetados com sucesso");
            return "ok";
        } else if (data.status === "no feedbacks") {
            console.log("⚠️ Erro ao resetar feedbacks:", data.message);
            return "no feedbacks";
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            return "erro";
        }
    } catch (error) {
        console.error("❌ Erro ao resetar feedbacks:", error);
        return false;
    }
};

export const getRandomRecomendationSuprise = async () => {
    const token = getToken();
    console.log("🎉 Surpresa! Gerando uma recomendação aleatória...");

    try {
        const res = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/recommendation/surprise`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({}),
            }
        );

        if (!res.ok) throw new Error("Erro ao gerar recomendação");

        const data = await res.json();
        console.log("📬 Recomendação recebida:", data.recomendacao);
        return data.recomendacao; // Return the recommendation object instead of showing an alert
    } catch (error) {
        console.error("Erro ao buscar recomendação:", error);
        throw error;
    }
};
