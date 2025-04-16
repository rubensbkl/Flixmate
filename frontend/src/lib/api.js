import { movieCache } from "./cache";

export const fetchMovies = async (userId, page) => {
    if (page === 1) {
        const cached = movieCache.get(userId);
        if (cached) {
            console.log("🔄 Usando cache");
            return cached;
        }
    }

    console.log("📡 Buscando filmes da API - página", page);

    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/movies`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId, page }),
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

    if (page === 1) movieCache.store(userId, processed);

    return processed;
};

export const sendFeedback = async (userId, movieId, liked) => {
    await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/feedback`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            userId: userId,
            movieId: movieId,
            feedback: liked,
        }),
    });
};

export const gerarRecomendacao = async (userId) => {
    console.log("🔁 Gerando recomendação...");
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/recommendation`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId }),
    });

    if (!res.ok) throw new Error("Erro ao gerar recomendação");

    const { recomendacao } = await res.json();
    alert(`📢 Nova recomendação: ${recomendacao}`);
};
