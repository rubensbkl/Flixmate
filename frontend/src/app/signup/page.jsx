"use client";
import { useAuth } from "@/contexts/AuthContext";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { DateInputWithPlaceholder } from "@/components/DateInputWithPlaceholder";

export default function SignUpPage() {
    const { login } = useAuth();
    const [formData, setFormData] = useState({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        gender: "",
        birthdate: "",
        favoriteGenres: [],
    });
    const [step, setStep] = useState(1); // Controle de etapas
    const [genres, setGenres] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState("");
    const router = useRouter();

    // Fetch genres when the component mounts
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
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleGenreToggle = (genreId) => {
        setFormData((prev) => {
            // Check if the genre is already selected
            if (prev.favoriteGenres.includes(genreId)) {
                // Remove the genre
                return {
                    ...prev,
                    favoriteGenres: prev.favoriteGenres.filter(
                        (id) => id !== genreId
                    ),
                };
            } else {
                // Add the genre (limit to 5 selections)
                if (prev.favoriteGenres.length < 5) {
                    return {
                        ...prev,
                        favoriteGenres: [...prev.favoriteGenres, genreId],
                    };
                }
                return prev; // Don't change if already at 5 selections
            }
        });
    };

    // Função para verificar se o usuário é maior de idade
    const isAdult = (birthdate) => {
        if (!birthdate) return false;

        const today = new Date();
        const birthDate = new Date(birthdate);
        let age = today.getFullYear() - birthDate.getFullYear();
        const monthDiff = today.getMonth() - birthDate.getMonth();

        // Ajusta a idade se ainda não fez aniversário este ano
        if (
            monthDiff < 0 ||
            (monthDiff === 0 && today.getDate() < birthDate.getDate())
        ) {
            age--;
        }

        return age >= 18;
    };

    const nextStep = () => {
        // Verificação de campos obrigatórios
        if (
            !formData.firstName ||
            !formData.lastName ||
            !formData.email ||
            !formData.password ||
            !formData.gender ||
            !formData.birthdate
        ) {
            setError("Por favor, preencha todos os campos");
            return;
        }

        // Validação de email
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(formData.email)) {
            setError("Por favor, insira um email válido");
            return;
        }

        // Validação de comprimento mínimo da senha
        if (formData.password.length < 6) {
            setError("A senha deve ter pelo menos 6 caracteres");
            return;
        }

        // Validação de força da senha (mín. 1 maiúscula, 1 minúscula e 1 número)
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{6,}$/;
        if (!passwordRegex.test(formData.password)) {
            setError(
                "A senha deve conter letras maiúsculas, minúsculas e números"
            );
            return;
        }

        // Validação da data de nascimento
        const birthDate = new Date(formData.birthdate);
        const today = new Date();
        const minDate = new Date("1900-01-01");

        // Remove horas para comparação exata (só data)
        birthDate.setHours(0, 0, 0, 0);
        today.setHours(0, 0, 0, 0);

        if (isNaN(birthDate.getTime())) {
            setError("Data de nascimento inválida");
            return;
        }

        if (birthDate > today) {
            setError("A data de nascimento não pode ser no futuro");
            return;
        }

        if (birthDate < minDate) {
            setError("A data de nascimento não pode ser anterior a 1900");
            return;
        }

        // Tudo certo
        setError(""); // Limpa qualquer erro
        setStep(2);   // Avança para o próximo passo
    };

    const prevStep = () => {
        setStep(1);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setError("");

        // Validation
        if (formData.favoriteGenres.length === 0) {
            setError("Por favor, selecione pelo menos um gênero favorito");
            setIsLoading(false);
            return;
        }

        // Calcular se é maior de idade
        const isUserAdult = isAdult(formData.birthdate);

        console.log("isUserAdult", isUserAdult);
        console.log("formData", formData);

        try {
            const response = await fetch(
                `${process.env.NEXT_PUBLIC_API_URL}/api/register`,
                {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        ...formData,
                        isUserAdult,
                    }),
                }
            );

            const data = await response.json();

            if (!response.ok)
                throw new Error(data.error || "Erro ao criar conta");

            login(data.user, data.token);
            router.push("/");
        } catch (err) {
            setError(err.message || "Ocorreu um erro ao criar sua conta");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="flex flex-col items-center justify-center min-h-screen px-4 bg-background">
            <div className="w-full max-w-md">
                <div className="flex items-center justify-center mb-8">
                    <img
                        src="flixmate-logo-teste1.svg"
                        alt="Flixmate Logo"
                        className="h-12"
                    />
                </div>

                <h1 className="text-2xl text-primary font-bold text-center mb-2">
                    Crie uma conta
                </h1>
                <p className="text-center text-secondary text-gray-600 mb-8">
                    Utilize seu email para entrar no Flixmate
                </p>

                {error && (
                    <div className="border border-yellow-500 text-yellow-400 p-3 rounded-lg mb-4"
                        style={{ backgroundColor: 'rgba(234, 179, 8, 0.1)' }} // Tailwind yellow-500 + 10% opacity
                    >
                        {error}
                    </div>
                )}

                {step === 1 ? (
                    <form
                        className="space-y-4"
                        key="signup-form-step1"
                        autoComplete="off"
                    >
                        <input
                            type="text"
                            name="firstName"
                            placeholder="Nome"
                            className="w-full p-3 text-primary bg-foreground border border-foreground bg-foreground rounded-lg placeholder-secondary focus:border-accent focus:outline-none transition-colors"
                            value={formData.firstName}
                            onChange={handleChange}
                            required
                            autoComplete="new-firstName"
                        />
                        <input
                            type="text"
                            name="lastName"
                            placeholder="Sobrenome"
                            className="w-full p-3 text-primary bg-foreground border border-foreground bg-foreground rounded-lg placeholder-secondary focus:border-accent focus:outline-none transition-colors"
                            value={formData.lastName}
                            onChange={handleChange}
                            required
                            autoComplete="new-lastName"
                        />
                        <input
                            type="email"
                            name="email"
                            placeholder="Email"
                            className="w-full p-3 text-primary bg-foreground border border-foreground bg-foreground rounded-lg placeholder-secondary focus:border-accent focus:outline-none transition-colors"
                            value={formData.email}
                            onChange={handleChange}
                            required
                            autoComplete="new-email"
                        />
                        <input
                            type="password"
                            name="password"
                            placeholder="Senha (mín. 6 caracteres)"
                            className="w-full p-3 text-primary bg-foreground border border-foreground bg-foreground rounded-lg placeholder-secondary focus:border-accent focus:outline-none transition-colors"
                            value={formData.password}
                            onChange={handleChange}
                            required
                            minLength={6}
                            autoComplete="new-password"
                        />

                        <DateInputWithPlaceholder
                            placeholder="Data de Nascimento"
                            value={formData.birthdate}
                            onChange={handleChange}
                            name="birthdate"
                            required
                            min="1900-01-01"
                            max={new Date().toISOString().split("T")[0]}
                        />



                        <select
                            name="gender"
                            className={`w-full p-3 bg-foreground border border-foreground rounded-lg focus:border-accent focus:outline-none transition-colors ${formData.gender === "" ? "text-secondary" : "text-primary"
                                }`}
                            value={formData.gender}
                            onChange={handleChange}
                            required
                            autoComplete="new-gender"
                        >
                            <option value="">Selecione o gênero</option>
                            <option value="M">Masculino</option>
                            <option value="F">Feminino</option>
                            <option value="O">Outro</option>
                        </select>

                        <button
                            type="button"
                            onClick={nextStep}
                            className="w-full p-3 bg-accent text-background rounded-lg font-medium"
                        >
                            Continuar
                        </button>
                    </form>
                ) : (
                    <form
                        onSubmit={handleSubmit}
                        className="space-y-4 "
                        key="signup-form-step2"
                        autoComplete="off"
                    >
                        <div className="space-y-2 ">
                            <h2 className="text-lg font-medium text-primary">
                                Gêneros favoritos
                            </h2>
                            <p className="text-sm text-gray-600 mb-3">
                                Selecione até 5 gêneros de filmes que você mais
                                gosta
                            </p>

                            <div className="grid grid-cols-2 gap-2">
                                {genres.map((genre) => (
                                    <div
                                        key={genre.id}
                                        className="flex items-start"
                                    >
                                        <div className="flex items-center h-5">
                                            <div className="relative">
                                                <input
                                                    id={`genre-${genre.id}`}
                                                    type="checkbox"
                                                    checked={formData.favoriteGenres.includes(genre.id)}
                                                    onChange={() => handleGenreToggle(genre.id)}
                                                    className="sr-only" // esconde o checkbox padrão
                                                    disabled={
                                                        !formData.favoriteGenres.includes(genre.id) &&
                                                        formData.favoriteGenres.length >= 5
                                                    }
                                                />
                                                <label
                                                    htmlFor={`genre-${genre.id}`}
                                                    className={`
            flex items-center justify-center w-4 h-4 border-2 rounded cursor-pointer transition-all duration-200
            ${formData.favoriteGenres.includes(genre.id)
                                                            ? 'bg-accent border-accent text-background'
                                                            : 'bg-foreground border-secondary hover:border-accent'
                                                        }
            ${(!formData.favoriteGenres.includes(genre.id) && formData.favoriteGenres.length >= 5)
                                                            ? 'opacity-50 cursor-not-allowed'
                                                            : 'hover:scale-105'
                                                        }
        `}
                                                >
                                                    {formData.favoriteGenres.includes(genre.id) && (
                                                        <svg className="w-3 h-3 text-background" fill="currentColor" viewBox="0 0 20 20">
                                                            <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                                                        </svg>
                                                    )}
                                                </label>
                                            </div>
                                        </div>
                                        <div className="ml-2 text-sm">
                                            <label
                                                htmlFor={`genre-${genre.id}`}
                                                className="text-secondary"
                                            >
                                                {genre.name}
                                            </label>
                                        </div>
                                    </div>
                                ))}
                            </div>
                            <p className="text-sm text-gray-500 mt-2">
                                {formData.favoriteGenres.length}/5 selecionados
                            </p>
                        </div>

                        <div className="flex space-x-3">
                            <button
                                type="button"
                                onClick={prevStep}
                                className="flex-1 p-3 border-none bg-foreground text-primary rounded-lg font-medium"
                            >
                                Voltar
                            </button>
                            <button
                                type="submit"
                                className="flex-1 p-3 bg-accent text-backgrou rounded-lg font-medium"
                                disabled={isLoading}
                            >
                                {isLoading ? "Cadastrando..." : "Cadastrar"}
                            </button>
                        </div>
                    </form>
                )}

                <p className="text-center text-secondary mt-6 text-gray-600">
                    Já tem uma conta?{" "}
                    <Link href="/login" className="text-accent font-medium">
                        Entre aqui
                    </Link>
                </p>
            </div>
        </div>
    );
}
