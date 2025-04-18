import { movieCache } from "./cache";

// Função auxiliar para obter o token
const getToken = () => {
    return localStorage.getItem('token');
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
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ page }),
    });

    if (!res.ok) throw new Error(`Erro na API: ${res.status}`);

    const { movies } = await res.json();
    const processed = movies.map(movie => ({
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
            "Authorization": `Bearer ${token}`
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
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/recommendation`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({}),
      });
      
      if (!res.ok) throw new Error("Erro ao gerar recomendação");
      
      const data = await res.json();
      console.log("📬 Recomendação recebida:", data.recomendacao);
      return data.recomendacao; // Return the recommendation object instead of showing an alert
    } catch (error) {
      console.error("Erro ao buscar recomendação:", error);
      throw error;
    }
};


export const fetchRecommendations = async () => {
    const token = getToken();
    console.log("📡 Buscando histórico de recomendações");
    
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/recommendations`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({}),
      });
      
      if (!res.ok) throw new Error(`Erro na API: ${res.status}`);
      
      const data = await res.json();
      
      if (data.status === "ok" && Array.isArray(data.recommendations)) {
        console.log(`🔍 ${data.recommendations.length} recomendações encontradas`);
        return data.recommendations;
      } else {
        console.log("⚠️ Formato de resposta inesperado:", data);
        return [];
      }
    } catch (error) {
      console.error("❌ Erro ao buscar recomendações:", error);
      throw error;
    }
  };

////////////////////////////////////////////////////////////////////////////////////////////////////

export const fetchUsers = async () => {
    const token = getToken();
    console.log("📡 Buscando usuários da plataforma");
    
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        }
      });
      
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
  
  export const fetchUserProfile = async (userId) => {
    const token = getToken();
    console.log(`📡 Buscando informações básicas do usuário: ${userId}`);
    
    try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/profile/${userId}/`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            }
        });
      
        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);
        
        const data = await res.json();

        console.log("Dados recebidos:", data); // Adicionei este log para depuração
        
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

// Busca apenas filmes favoritos do usuário
export const fetchUserFavorites = async (userId) => {
    const token = getToken();
    console.log(`📡 Buscando filmes favoritos do usuário: ${userId}`);
    
    try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/${userId}/favorites`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            }
        });
      
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

// Busca apenas filmes recentes do usuário
export const fetchUserRecents = async (userId) => {
    const token = getToken();
    console.log(`📡 Buscando filmes recentes do usuário: ${userId}`);
    
    try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/${userId}/recents`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            }
        });
      
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

// Busca apenas filmes recomendados do usuário
export const fetchUserRecommended = async (userId) => {
    const token = getToken();
    console.log(`📡 Buscando filmes recomendados do usuário: ${userId}`);
    
    try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/${userId}/recommended`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            }
        });
      
        if (!res.ok) throw new Error(`Erro na API: ${res.status}`);
        
        const data = await res.json();

        console.log("Dados recebidos:", data); // Adicionei este log para depuração
        
        if (data.status === "ok") {
            console.log(`🔍 Filmes recomendados do usuário ${userId} carregados`);
            return data.movies || [];
        } else {
            console.log("⚠️ Formato de resposta inesperado:", data);
            return [];
        }
    } catch (error) {
        console.error(`❌ Erro ao buscar filmes recomendados do usuário ${userId}:`, error);
        return [];
    }
};

