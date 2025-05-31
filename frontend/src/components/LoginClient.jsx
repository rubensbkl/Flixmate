"use client";

import { useAuth } from "@/contexts/AuthContext";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function LoginClient() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState("");
    const [successMessage, setSuccessMessage] = useState("");

    useEffect(() => {
        return () => {
            setEmail("");
            setPassword("");
        };
    }, []);

    const router = useRouter();
    const searchParams = useSearchParams();
    const { login } = useAuth();

    useEffect(() => {
        if (searchParams.get("registered") === "true") {
            setSuccessMessage(
                "Conta criada com sucesso! Faça login para continuar."
            );
        }
    }, [searchParams]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setError("");

        try {
            const response = await fetch(
                `${process.env.NEXT_PUBLIC_API_URL}/api/login`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({ email, password }),
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || "Credenciais inválidas");
            }

            // Usar o contexto de autenticação para login
            login(data.user, data.token);

            // Redirecionar para a página inicial após login
            router.push("/");
        } catch (err) {
            setError(err.message || "Falha ao fazer login");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="flex flex-col items-center justify-center min-h-screen px-4 bg-background">
            <div className="w-full max-w-md">
                <div className="flex items-center justify-center mb-8">
                    <img
                        src="/flixmate-logo.svg"
                        alt="Flixmate Logo"
                        className="h-12"
                    />
                </div>

                <h1 className="text-2xl text-primary font-bold text-center mb-2">
                    Entre em uma conta
                </h1>
                <p className="text-center text-secondary mb-8">
                    Utilize seu email para entrar no Flixmate
                </p>

                {successMessage && (
                    <div className="bg-green-500/10 border border-green-500/50 text-green-400 p-3 rounded-lg mb-4">
                        {successMessage}
                    </div>
                )}

                {error && (
                    <div className="bg-red-500/10 border border-red-500/50 text-red-400 p-3 rounded-lg mb-4">
                        {error}
                    </div>
                )}

                <form
                    onSubmit={handleSubmit}
                    className="space-y-4"
                    key="login-form"
                    autoComplete="off"
                >
                    <div>
                        <input
                            type="email"
                            placeholder="Email"
                            className="w-full p-3 text-primary bg-foreground border border-foreground rounded-lg placeholder-secondary focus:border-accent focus:outline-none transition-colors"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                            autoComplete="new-email"
                        />
                    </div>

                    <div>
                        <input
                            type="password"
                            placeholder="Senha"
                            className="w-full p-3 text-primary bg-foreground border border-foreground rounded-lg placeholder-secondary focus:border-accent focus:outline-none transition-colors"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            autoComplete="new-password"
                        />
                    </div>

                    <button
                        type="submit"
                        className="w-full p-3 bg-accent text-background rounded-lg font-medium hover:bg-accent/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                        disabled={isLoading}
                    >
                        {isLoading ? "Entrando..." : "Entrar"}
                    </button>
                </form>

                <p className="text-center mt-6 text-secondary">
                    Não tem uma conta?{" "}
                    <Link href="/signup" className="text-accent font-medium hover:text-accent/80 transition-colors">
                        Cadastre-se
                    </Link>
                </p>
            </div>
        </div>
    );
}