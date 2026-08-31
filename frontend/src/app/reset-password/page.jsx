"use client";

import { useState, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { EyeIcon, EyeSlashIcon } from "@heroicons/react/24/outline";

export default function ResetPasswordPage() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const token = searchParams.get("token");

    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    
    const [status, setStatus] = useState("idle");
    const [message, setMessage] = useState("");

    useEffect(() => {
        if (!token) {
            setStatus("error");
            setMessage("Token de redefinição inválido ou ausente. Por favor, solicite um novo link.");
        }
    }, [token]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!token) return;

        if (newPassword.length < 6) {
            setStatus("error");
            setMessage("A senha deve ter pelo menos 6 caracteres.");
            return;
        }

        if (newPassword !== confirmPassword) {
            setStatus("error");
            setMessage("As senhas não coincidem.");
            return;
        }

        setStatus("loading");
        try {
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/password/reset`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ token, newPassword }),
            });
            const data = await res.json();
            
            if (res.ok) {
                setStatus("success");
                setMessage("Sua senha foi redefinida com sucesso!");
                setTimeout(() => {
                    router.push("/login");
                }, 3000);
            } else {
                setStatus("error");
                setMessage(data.error || "Ocorreu um erro ao redefinir a senha.");
            }
        } catch (error) {
            setStatus("error");
            setMessage("Erro de conexão com o servidor.");
        }
    };

    return (
        <div className="min-h-screen bg-background flex flex-col items-center justify-center p-4">
            <div className="w-full max-w-md bg-foreground/20 p-8 rounded-2xl border border-foreground backdrop-blur-sm shadow-xl">
                <div className="text-center mb-8">
                    <h1 className="text-3xl font-bold text-primary mb-2">Criar nova senha</h1>
                    <p className="text-secondary">Digite sua nova senha abaixo.</p>
                </div>

                {status === "success" ? (
                    <div className="bg-green-500/10 border border-green-500/20 rounded-xl p-6 text-center animate-fadeIn">
                        <div className="w-12 h-12 bg-green-500/20 text-green-400 rounded-full flex items-center justify-center mx-auto mb-4">
                            <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                            </svg>
                        </div>
                        <h3 className="text-lg font-medium text-primary mb-2">{message}</h3>
                        <p className="text-sm text-secondary mb-6">Redirecionando para o login...</p>
                        <Link href="/login" className="text-accent hover:text-accent/80 font-medium transition-colors">
                            Ir para o Login agora
                        </Link>
                    </div>
                ) : (
                    <form onSubmit={handleSubmit} className="space-y-6">
                        {status === "error" && (
                            <div className="p-4 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm animate-fadeIn">
                                {message}
                            </div>
                        )}
                        
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-secondary mb-2" htmlFor="newPassword">
                                    Nova Senha
                                </label>
                                <div className="relative">
                                    <input
                                        id="newPassword"
                                        type={showPassword ? "text" : "password"}
                                        value={newPassword}
                                        onChange={(e) => setNewPassword(e.target.value)}
                                        className="w-full pl-4 pr-12 py-3 bg-background border border-foreground rounded-xl text-primary focus:outline-none focus:border-accent focus:ring-1 focus:ring-accent transition-all"
                                        placeholder="••••••••"
                                        disabled={!token || status === "loading"}
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowPassword(!showPassword)}
                                        className="absolute right-4 top-1/2 -translate-y-1/2 text-secondary hover:text-primary transition-colors"
                                    >
                                        {showPassword ? (
                                            <EyeSlashIcon className="w-5 h-5" />
                                        ) : (
                                            <EyeIcon className="w-5 h-5" />
                                        )}
                                    </button>
                                </div>
                            </div>
                            
                            <div>
                                <label className="block text-sm font-medium text-secondary mb-2" htmlFor="confirmPassword">
                                    Confirmar Nova Senha
                                </label>
                                <input
                                    id="confirmPassword"
                                    type={showPassword ? "text" : "password"}
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    className="w-full px-4 py-3 bg-background border border-foreground rounded-xl text-primary focus:outline-none focus:border-accent focus:ring-1 focus:ring-accent transition-all"
                                    placeholder="••••••••"
                                    disabled={!token || status === "loading"}
                                />
                            </div>
                        </div>

                        <button
                            type="submit"
                            disabled={!token || status === "loading"}
                            className="w-full bg-accent hover:bg-accent/90 text-background font-bold py-3 px-4 rounded-xl transition-all disabled:opacity-50 disabled:cursor-not-allowed flex justify-center items-center"
                        >
                            {status === "loading" ? (
                                <svg className="animate-spin h-5 w-5 text-background" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                            ) : (
                                "Redefinir Senha"
                            )}
                        </button>
                    </form>
                )}
            </div>
        </div>
    );
}
