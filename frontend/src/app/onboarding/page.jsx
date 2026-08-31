"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";

export default function OnboardingPage() {
    const { user, loading: authLoading } = useAuth();
    const router = useRouter();

    const [genres, setGenres] = useState([]);
    const [selectedGenres, setSelectedGenres] = useState([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!authLoading && !user) {
            router.push("/login");
        }
    }, [user, authLoading, router]);

useEffect(() => {
        // Eu ia fazer uma requisição para pegar os gêneros, mas nem precisa porque já temos os dados e não vai mudar
        setGenres([
            { id: 28, name: "Ação" },
            { id: 12, name: "Aventura" },
            { id: 16, name: "Animação" },
            { id: 35, name: "Comédia" },
            { id: 80, name: "Crime" },
            { id: 99, name: "Documentário" },
            { id: 18, name: "Drama" },
            { id: 10751, name: "Família" },
            { id: 14, name: "Fantasia" },
            { id: 36, name: "História" },
            { id: 27, name: "Terror" },
            { id: 10402, name: "Música" },
            { id: 9648, name: "Mistério" },
            { id: 10749, name: "Romance" },
            { id: 878, name: "Ficção científica" },
            { id: 10770, name: "Cinema TV" },
            { id: 53, name: "Thriller" },
            { id: 10752, name: "Guerra" },
            { id: 37, name: "Faroeste" },
        ]);
        setLoading(false);
    }, []);

    const handleGenreToggle = (id) => {
        setSelectedGenres((prev) => {
            if (prev.includes(id)) {
                return prev.filter(gId => gId !== id);
            }
            if (prev.length >= 5) {
                return prev;
            }
            return [...prev, id];
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (selectedGenres.length === 0) {
            setError("Selecione pelo menos um gênero favorito.");
            return;
        }

        setSaving(true);
        setError(null);

        try {
            const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:6789';
            const token = localStorage.getItem("token");
            
            const res = await fetch(`${baseUrl}/api/profile/update`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({
                    genres: selectedGenres
                })
            });

            if (res.ok) {
                router.push("/");
            } else {
                const data = await res.json();
                setError(data.error || "Erro ao salvar preferências.");
            }
        } catch (err) {
            setError("Erro de conexão ao salvar preferências.");
        } finally {
            setSaving(false);
        }
    };

    if (authLoading || loading) {
        return (
            <div className="flex items-center justify-center min-h-screen bg-background">
                <svg className="animate-spin h-10 w-10 text-accent" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
            </div>
        );
    }

    if (!user) return null;

    return (
        <div className="relative flex flex-col items-center justify-center min-h-screen px-4 bg-background overflow-hidden py-12">
            <div className="absolute top-[10%] left-[-10%] w-[500px] h-[500px] bg-accent/20 rounded-full blur-[120px] pointer-events-none"></div>
            <div className="absolute bottom-[-10%] right-[10%] w-[400px] h-[400px] bg-accent/10 rounded-full blur-[100px] pointer-events-none"></div>

            <div className="w-full max-w-2xl relative z-10">
                <div className="bg-foreground/20 backdrop-blur-xl border border-white/10 p-8 rounded-3xl shadow-2xl">
                    <h1 className="text-3xl text-primary font-bold text-center mb-2 tracking-tight">
                        Quais são os seus favoritos?
                    </h1>
                    <p className="text-center text-secondary text-sm mb-8">
                        Selecione até 5 gêneros para personalizarmos suas recomendações.
                    </p>

                    {error && (
                        <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-4 rounded-xl mb-6 text-sm flex items-center gap-3 animate-fadeIn">
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-8">
                        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
                            {genres.map((genre) => (
                                <div
                                    key={genre.id}
                                    className={`relative p-4 rounded-xl cursor-pointer border transition-all duration-200 flex items-center shadow-sm select-none
                                        ${selectedGenres.includes(genre.id) 
                                            ? 'bg-accent/20 border-accent text-primary' 
                                            : 'bg-background/50 border-white/5 text-secondary hover:bg-background/80 hover:border-white/20'}`}
                                    onClick={() => handleGenreToggle(genre.id)}
                                >
                                    <div className="relative flex items-center justify-center w-5 h-5 mr-3">
                                        <div className={`w-full h-full border-2 rounded-md transition-all ${selectedGenres.includes(genre.id) ? 'bg-accent border-accent' : 'border-secondary/50'}`}></div>
                                        {selectedGenres.includes(genre.id) && (
                                            <svg className="absolute w-3.5 h-3.5 text-background" fill="currentColor" viewBox="0 0 20 20">
                                                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                                            </svg>
                                        )}
                                    </div>
                                    <span className="font-medium text-sm">{genre.name}</span>
                                </div>
                            ))}
                        </div>
                        
                        <div className="flex justify-between items-center text-sm px-1">
                            <span className="text-secondary">Selecionados:</span>
                            <span className={`font-bold ${selectedGenres.length === 5 ? 'text-accent' : 'text-primary'}`}>
                                {selectedGenres.length}/5
                            </span>
                        </div>

                        <div className="flex justify-end pt-4">
                            <button
                                type="submit"
                                className="px-8 py-4 bg-accent text-background rounded-xl font-bold hover:bg-accent/90 hover:shadow-[0_0_20px_rgba(var(--accent),0.3)] hover:-translate-y-0.5 active:translate-y-0 transition-all duration-200 disabled:opacity-50"
                                disabled={saving}
                            >
                                {saving ? "Salvando..." : "Concluir Perfil"}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
