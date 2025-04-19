"use client";

import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import { fetchPrivate, updateMyProfile } from "@/lib/api";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

export default function EditProfilePage() {
  const [genres, setGenres] = useState([]); // IDs selecionados
  const [contentFilter, setContentFilter] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const router = useRouter();

  const availableGenres = [
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
  ];

  useEffect(() => {
    const loadUserPreferences = async () => {
      try {
        const data = await fetchPrivate();
        console.log("Dados do usuário:", data.contentFilter);
        setGenres(data.genres || []);
        setContentFilter(data.contentFilter);
      } catch (err) {
        console.error("Erro ao carregar dados:", err);
        setError("Não foi possível carregar suas preferências.");
      } finally {
        setLoading(false);
      }
    };

    loadUserPreferences();
  }, []);

  const handleGenreToggle = (genreId) => {
    setGenres((prev) => {
      if (prev.includes(genreId)) {
        return prev.filter((id) => id !== genreId);
      } else {
        if (prev.length < 5) {
          return [...prev, genreId];
        }
        return prev;
      }
    });
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      await updateMyProfile({ genres, contentFilter });
      router.push("/profile");
    } catch (err) {
      console.error("Erro ao salvar perfil:", err);
      setError("Erro ao salvar as alterações.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <p>Carregando...</p>;
  if (error) return <p className="text-red-500">{error}</p>;

  return (
    <ProtectedRoute>
      <div className="flex md:flex-row min-h-screen">
        <Navbar />

        <main className="flex-1 p-8 max-w-3xl mx-auto">
          <h1 className="text-2xl font-bold mb-6">Editar Perfil</h1>

          {/* Gêneros */}
          <div className="mb-6">
            <h2 className="text-lg font-medium mb-2">Gêneros Favoritos</h2>
            <p className="text-sm text-gray-600 mb-3">Selecione até 5 gêneros</p>

            <div className="grid grid-cols-2 gap-2">
              {availableGenres.map((genre) => (
                <div key={genre.id} className="flex items-start">
                  <input
                    id={`genre-${genre.id}`}
                    type="checkbox"
                    checked={genres.includes(genre.id)}
                    onChange={() => handleGenreToggle(genre.id)}
                    className="h-4 w-4 text-blue-600 border-gray-300 rounded"
                    disabled={!genres.includes(genre.id) && genres.length >= 5}
                  />
                  <label htmlFor={`genre-${genre.id}`} className="ml-2 text-gray-700">
                    {genre.name}
                  </label>
                </div>
              ))}
            </div>

            <p className="text-sm text-gray-500 mt-2">
              {genres.length}/5 selecionados
            </p>
          </div>

          {/* Filtro de Conteúdo */}
          <div className="mb-6 flex items-center space-x-3">
            <label className="text-gray-700 font-medium">Filtro de Conteúdo</label>
            <button
              onClick={() => setContentFilter(!contentFilter)}
              className={`w-14 h-7 rounded-full flex items-center transition ${
                contentFilter ? "bg-blue-600" : "bg-gray-300"
              }`}
            >
              <div
                className={`w-6 h-6 bg-white rounded-full shadow-md transform transition ${
                  contentFilter ? "translate-x-7" : "translate-x-1"
                }`}
              />
            </button>
          </div>

          {/* Botão de Salvar */}
          <button
            onClick={handleSave}
            disabled={saving}
            className="px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            {saving ? "Salvando..." : "Salvar Alterações"}
          </button>
        </main>
      </div>
    </ProtectedRoute>
  );
}
