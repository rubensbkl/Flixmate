"use client";

import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import { fetchPrivate, updateMyProfile } from "@/lib/api";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function EditProfilePage() {
    // Dados do usuário
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [email, setEmail] = useState("");
    const [gender, setGender] = useState("");
    const [genres, setGenres] = useState([]); // IDs selecionados
    const [contentFilter, setContentFilter] = useState(false);
    
    // Estados da UI
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [successMessage, setSuccessMessage] = useState("");
    const [isMobile, setIsMobile] = useState(false);
    
    const router = useRouter();
    
    // Gêneros disponíveis para seleção
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

    // Detectar se estamos em um dispositivo móvel
    useEffect(() => {
        const checkMobile = () => {
            setIsMobile(window.innerWidth < 768);
        };
        
        // Verificar no carregamento inicial
        checkMobile();
        
        // Adicionar listener para redimensionamento da janela
        window.addEventListener('resize', checkMobile);
        
        // Limpeza
        return () => window.removeEventListener('resize', checkMobile);
    }, []);

    // Carregar os dados do usuário ao montar o componente
    useEffect(() => {
        const loadUserPreferences = async () => {
            try {
                setLoading(true);
                setError(null);
                
                const data = await fetchPrivate();
                console.log("Dados do usuário:", data);
                
                // Dados básicos
                setFirstName(data.firstName || "");
                setLastName(data.lastName || "");
                setEmail(data.email || "");
                setGender(data.gender || "");
                setContentFilter(data.contentFilter ?? false);
                
                // Gêneros preferidos
                if (data.genres && Array.isArray(data.genres)) {
                    setGenres(data.genres);
                } else if (data.preferredGenres && Array.isArray(data.preferredGenres)) {
                    // Se os gêneros vierem como objetos, extraímos apenas os IDs
                    setGenres(data.preferredGenres.map(genre => genre.id));
                } else {
                    // Se não houver gêneros, definimos um padrão para evitar que o usuário fique sem gêneros
                    console.warn("Usuário sem gêneros. Definindo gênero padrão.");
                    setGenres([28]); // Definir Ação como gênero padrão
                }
            } catch (err) {
                console.error("Erro ao carregar dados:", err);
                setError("Não foi possível carregar suas preferências.");
            } finally {
                setLoading(false);
            }
        };

        loadUserPreferences();
    }, []);

    // Alternar a seleção de gênero
    const handleGenreToggle = (genreId) => {
        setGenres((prev) => {
            if (prev.includes(genreId)) {
                // VALIDAÇÃO: Impedir a remoção se for o último gênero
                if (prev.length <= 1) {
                    setError("Você deve manter pelo menos um gênero selecionado");
                    return prev;
                }
                return prev.filter((id) => id !== genreId);
            } else {
                if (prev.length < 5) {
                    setError(null); // Limpar mensagens de erro anteriores
                    return [...prev, genreId];
                }
                setError("Você pode selecionar no máximo 5 gêneros");
                return prev;
            }
        });
    };

    // Validar dados antes de salvar
    const validateForm = () => {
        // Limpar mensagens anteriores
        setError(null);
        
        // Validar campos obrigatórios
        if (!firstName.trim()) {
            setError("Nome é obrigatório");
            return false;
        }
        
        if (!lastName.trim()) {
            setError("Sobrenome é obrigatório");
            return false;
        }
        
        if (!email.trim()) {
            setError("Email é obrigatório");
            return false;
        }
        
        // Validar formato de email com expressão regular simples
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            setError("Formato de email inválido");
            return false;
        }
        
        // Validar seleção de gênero (dropdown)
        if (!gender) {
            setError("Gênero é obrigatório");
            return false;
        }
        
        // Validar gêneros preferidos
        if (!genres.length) {
            setError("Selecione pelo menos um gênero preferido");
            return false;
        }
        
        if (genres.length > 5) {
            setError("Você pode selecionar no máximo 5 gêneros");
            return false;
        }
        
        return true;
    };

    // Salvar o perfil
    const handleSave = async () => {
        // Validar o formulário antes de enviar
        if (!validateForm()) {
            return;
        }
        
        try {
            setSaving(true);
            setError(null);
            setSuccessMessage("");
            
            // Preparar os dados do usuário
            const userData = {
                firstName,
                lastName,
                email,
                gender,
                genres,
                contentFilter
            };
            
            console.log("Enviando dados:", userData);
            
            // Enviar dados para a API
            const response = await updateMyProfile(userData);
            
            // Mostrar mensagem de sucesso
            setSuccessMessage("Perfil atualizado com sucesso!");
            
            // Opcional: redirecionar após alguns segundos
            // setTimeout(() => router.push("/profile"), 1500);
        } catch (err) {
            console.error("Erro ao salvar perfil:", err);
            
            // Verificar se é um erro com mensagem da API
            if (err.response && err.response.data && err.response.data.error) {
                setError(err.response.data.error);
            } else {
                setError("Erro ao salvar as alterações. Tente novamente.");
            }
        } finally {
            setSaving(false);
        }
    };

    // Renderização durante o carregamento
    if (loading) {
        return (
            <ProtectedRoute>
                <div className="flex flex-col md:flex-row min-h-screen bg-gray-50">
                    <div className="md:w-64 md:min-h-screen">
                        <Navbar />
                    </div>
                    <main className="flex-1 flex items-center justify-center p-4">
                        <div className="text-center">
                            <div className="w-16 h-16 border-t-4 border-blue-500 border-solid rounded-full animate-spin mx-auto mb-4"></div>
                            <p className="text-xl font-medium text-gray-700">Carregando seu perfil...</p>
                        </div>
                    </main>
                </div>
            </ProtectedRoute>
        );
    }

    return (
        <ProtectedRoute>
            <div className="flex flex-col md:flex-row min-h-screen bg-gray-50">
                <div className="md:w-64 md:min-h-screen">
                    <Navbar />
                </div>

                <main className="flex-1 p-4 md:p-8 max-w-3xl mx-auto mb-20 md:mb-0">
                    <div className="bg-white rounded-lg shadow-sm p-6">
                        <h1 className="text-2xl font-bold mb-6 text-gray-800">Editar Perfil</h1>

                        {/* Mensagem de sucesso */}
                        {successMessage && (
                            <div className="bg-green-100 border border-green-200 text-green-700 px-4 py-3 rounded mb-6">
                                {successMessage}
                            </div>
                        )}

                        {/* Mensagem de erro */}
                        {error && (
                            <div className="bg-red-100 border border-red-200 text-red-700 px-4 py-3 rounded mb-6">
                                {error}
                            </div>
                        )}

                        {/* Informações Básicas */}
                        <div className="mb-6">
                            <h2 className="text-lg font-medium mb-4 text-gray-800">Informações Básicas</h2>
                            
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                {/* Nome */}
                                <div>
                                    <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-1">
                                        Nome *
                                    </label>
                                    <input
                                        type="text"
                                        id="firstName"
                                        value={firstName}
                                        onChange={(e) => setFirstName(e.target.value)}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        required
                                    />
                                </div>
                                
                                {/* Sobrenome */}
                                <div>
                                    <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-1">
                                        Sobrenome *
                                    </label>
                                    <input
                                        type="text"
                                        id="lastName"
                                        value={lastName}
                                        onChange={(e) => setLastName(e.target.value)}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        required
                                    />
                                </div>
                                
                                {/* Email */}
                                <div>
                                    <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
                                        Email *
                                    </label>
                                    <input
                                        type="email"
                                        id="email"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        required
                                    />
                                </div>
                                
                                {/* Gênero */}
                                <div>
                                    <label htmlFor="gender" className="block text-sm font-medium text-gray-700 mb-1">
                                        Gênero *
                                    </label>
                                    <select
                                        id="gender"
                                        value={gender}
                                        onChange={(e) => setGender(e.target.value)}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        required
                                    >
                                        <option value="">Selecione</option>
                                        <option value="M">Masculino</option>
                                        <option value="F">Feminino</option>
                                        <option value="O">Outro</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        {/* Gêneros */}
                        <div className="mb-6">
                            <h2 className="text-lg font-medium mb-2 text-gray-800">
                                Gêneros Favoritos *
                            </h2>
                            <p className="text-sm text-gray-600 mb-3">
                                Selecione de 1 a 5 gêneros
                            </p>

                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                                {availableGenres.map((genre) => (
                                    <div
                                        key={genre.id}
                                        className={`flex items-center p-2 rounded-md ${
                                            genres.includes(genre.id) ? 'bg-blue-50 border border-blue-100' : ''
                                        }`}
                                    >
                                        <input
                                            id={`genre-${genre.id}`}
                                            type="checkbox"
                                            checked={genres.includes(genre.id)}
                                            onChange={() => handleGenreToggle(genre.id)}
                                            className="h-5 w-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                                            disabled={!genres.includes(genre.id) && genres.length >= 5 || (genres.length === 1 && genres.includes(genre.id))}
                                        />
                                        <label
                                            htmlFor={`genre-${genre.id}`}
                                            className={`ml-2 ${
                                                genres.includes(genre.id) 
                                                    ? 'font-medium text-blue-800' 
                                                    : 'text-gray-700'
                                            } ${
                                                !genres.includes(genre.id) && genres.length >= 5
                                                    ? 'text-gray-400'
                                                    : ''
                                            }`}
                                        >
                                            {genre.name}
                                            {genres.length === 1 && genres.includes(genre.id) && (
                                                <span className="ml-1 text-xs text-red-500">(obrigatório)</span>
                                            )}
                                        </label>
                                    </div>
                                ))}
                            </div>

                            <p className="text-sm text-gray-500 mt-2">
                                {genres.length}/5 selecionados {genres.length === 0 && (
                                    <span className="text-red-500">(selecione pelo menos 1)</span>
                                )}
                            </p>
                        </div>

                        {/* Filtro de Conteúdo */}
                        <div className="mb-8 flex items-center space-x-3 p-3 border border-gray-100 rounded-md bg-gray-50">
                            <div className="flex-1">
                                <label className="text-gray-800 font-medium block mb-1">
                                    Filtro de Conteúdo
                                </label>
                                <p className="text-sm text-gray-600">
                                    Ative para filtrar conteúdo adulto e violento
                                </p>
                            </div>
                            <button
                                onClick={() => setContentFilter(!contentFilter)}
                                className={`w-14 h-7 rounded-full flex items-center transition ${
                                    contentFilter ? "bg-blue-600" : "bg-gray-300"
                                }`}
                                aria-pressed={contentFilter}
                                role="switch"
                            >
                                <div
                                    className={`w-6 h-6 bg-white rounded-full shadow-md transform transition ${
                                        contentFilter
                                            ? "translate-x-7"
                                            : "translate-x-1"
                                    }`}
                                />
                            </button>
                        </div>

                        {/* Botão de Salvar */}
                        <div className="flex justify-end">
                            <button
                                onClick={handleSave}
                                disabled={saving || genres.length === 0}
                                className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {saving ? (
                                    <span className="flex items-center">
                                        <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                        </svg>
                                        Salvando...
                                    </span>
                                ) : "Salvar Alterações"}
                            </button>
                        </div>
                    </div>
                </main>
            </div>
        </ProtectedRoute>
    );
}