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
    const [showPassword, setShowPassword] = useState(false);

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
            await login(data.user, data.token);

            // Redirecionar para a página inicial após login
            router.push("/");
        } catch (err) {
            setError(err.message || "Falha ao fazer login");
        } finally {
            setIsLoading(false);
        }
    };

    const handleOAuthLogin = async (provider) => {
        localStorage.setItem('oauth_action', 'login');
        try {
            const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:6789';
            const res = await fetch(`${baseUrl}/api/oauth/${provider}/url`);
            const data = await res.json();
            if (data.url) {
                window.location.href = data.url;
            } else {
                setError(data.error || "Erro ao conectar com provedor.");
            }
        } catch (err) {
            setError("Erro ao se conectar ao servidor.");
        }
    };

    return (
        <div className="relative flex flex-col items-center justify-center min-h-screen px-4 bg-background overflow-hidden">
            {/* Efeitos de fundo (Orbs luminosos) */}
            <div className="absolute top-[-10%] left-[-10%] w-96 h-96 bg-accent/20 rounded-full blur-[100px] pointer-events-none"></div>
            <div className="absolute bottom-[-10%] right-[-10%] w-[500px] h-[500px] bg-accent/10 rounded-full blur-[120px] pointer-events-none"></div>

            <div className="w-full max-w-md relative z-10">
                <div className="bg-foreground/20 backdrop-blur-xl border border-white/10 p-8 rounded-3xl shadow-2xl">
                    <div className="flex items-center justify-center mb-6">
                        <img
                            src="/flixmate-logo-teste1.svg"
                            alt="Flixmate Logo"
                            className="h-10 transform hover:scale-105 transition-transform duration-300"
                        />
                    </div>

                    <h1 className="text-3xl text-primary font-bold text-center mb-2 tracking-tight">
                        Bem-vindo de volta
                    </h1>
                    <p className="text-center text-secondary text-sm mb-8">
                        Entre na sua conta para continuar
                    </p>

                    {successMessage && (
                        <div className="bg-green-500/10 border border-green-500/20 text-green-400 p-4 rounded-xl mb-6 text-sm flex items-center gap-3 animate-fadeIn">
                            <svg className="w-5 h-5 shrink-0" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd"/></svg>
                            {successMessage}
                        </div>
                    )}

                    {error && (
                        <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-4 rounded-xl mb-6 text-sm flex items-center gap-3 animate-fadeIn">
                            <svg className="w-5 h-5 shrink-0" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd"/></svg>
                            {error}
                        </div>
                    )}

                    <form
                        onSubmit={handleSubmit}
                        className="space-y-5"
                        key="login-form"
                        autoComplete="off"
                    >
                        <div>
                            <input
                                type="email"
                                placeholder="E-mail"
                                className="w-full p-4 text-primary bg-background/50 border border-white/5 rounded-xl placeholder-secondary/50 focus:border-accent focus:ring-1 focus:ring-accent focus:bg-background/80 outline-none transition-all duration-200"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                autoComplete="new-email"
                            />
                        </div>

                        <div className="space-y-2">
                            <div className="relative group">
                                <input
                                    type={showPassword ? "text" : "password"}
                                    placeholder="Senha"
                                    className="w-full p-4 pr-12 text-primary bg-background/50 border border-white/5 rounded-xl placeholder-secondary/50 focus:border-accent focus:ring-1 focus:ring-accent focus:bg-background/80 outline-none transition-all duration-200"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                    autoComplete="new-password"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-4 top-1/2 -translate-y-1/2 text-secondary hover:text-primary transition-colors focus:outline-none"
                                >
                                    {showPassword ? (
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"/><path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"/><path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"/><line x1="2" x2="22" y1="2" y2="22"/></svg>
                                    ) : (
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
                                    )}
                                </button>
                            </div>
                            <div className="flex justify-end">
                                <Link href="/forgot-password" className="text-xs text-secondary hover:text-accent font-medium transition-colors">
                                    Esqueceu a senha?
                                </Link>
                            </div>
                        </div>

                        <button
                            type="submit"
                            className="w-full py-4 bg-accent text-background rounded-xl font-bold hover:bg-accent/90 hover:shadow-[0_0_20px_rgba(var(--accent),0.3)] hover:-translate-y-0.5 active:translate-y-0 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:translate-y-0 disabled:hover:shadow-none"
                            disabled={isLoading}
                        >
                            {isLoading ? (
                                <span className="flex items-center justify-center gap-2">
                                    <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"/><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"/></svg>
                                    Entrando...
                                </span>
                            ) : "Entrar"}
                        </button>
                    </form>

                    <div className="relative my-8">
                        <div className="absolute inset-0 flex items-center">
                            <div className="w-full border-t border-white/10"></div>
                        </div>
                        <div className="relative flex justify-center text-sm">
                            <span className="px-4 bg-[#1a1a1a] text-secondary/70">Ou continue com</span>
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <button 
                            type="button"
                            onClick={() => handleOAuthLogin('google')}
                            className="flex items-center justify-center gap-3 py-3 px-4 bg-background/30 hover:bg-white/5 border border-white/10 rounded-xl transition-all duration-200 text-sm font-medium text-primary group"
                        >
                            <svg className="w-5 h-5 group-hover:scale-110 transition-transform" viewBox="0 0 24 24">
                                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                            </svg>
                            Google
                        </button>
                        <button 
                            type="button"
                            onClick={() => handleOAuthLogin('github')}
                            className="flex items-center justify-center gap-3 py-3 px-4 bg-background/30 hover:bg-white/5 border border-white/10 rounded-xl transition-all duration-200 text-sm font-medium text-primary group"
                        >
                            <svg className="w-5 h-5 group-hover:scale-110 transition-transform" fill="currentColor" viewBox="0 0 24 24">
                                <path fillRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clipRule="evenodd" />
                            </svg>
                            GitHub
                        </button>
                    </div>

                    <p className="text-center mt-8 text-secondary text-sm">
                        Não tem uma conta?{" "}
                        <Link href="/signup" className="text-primary font-bold hover:text-accent transition-colors underline decoration-accent/30 underline-offset-4 hover:decoration-accent">
                            Cadastre-se
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}