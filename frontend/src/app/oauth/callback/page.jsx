"use client";

import { useEffect, useState, Suspense, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuth } from '@/contexts/AuthContext';
import { getToken } from '@/lib/auth';

function OAuthCallbackContent() {
    const searchParams = useSearchParams();
    const router = useRouter();
    const { login } = useAuth();
    
    const [status, setStatus] = useState("Processando autenticação...");
    const [error, setError] = useState(null);
    const calledRef = useRef(false);

    useEffect(() => {
        if (calledRef.current) return;
        calledRef.current = true;
        
        const code = searchParams.get('code');
        const provider = searchParams.get('provider') || searchParams.get('state');
        
        if (!code) {
            setError("Código de autorização não encontrado.");
            setTimeout(() => router.push('/login'), 3000);
            return;
        }
        if (!provider) {
            setError("Provedor não especificado.");
            setTimeout(() => router.push('/login'), 3000);
            return;
        }

        setStatus(`Autenticando com ${provider.charAt(0).toUpperCase() + provider.slice(1)}...`);

        const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:6789';
        const action = localStorage.getItem('oauth_action') || 'login';
        const performAuth = async () => {
            try {
                localStorage.removeItem('oauth_action');
                const res = await fetch(`${baseUrl}/api/oauth/${provider}/callback`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        ...(getToken() ? { 'Authorization': `Bearer ${getToken()}` } : {})
                    },
                    body: JSON.stringify({ code, action })
                });
                
                const data = await res.json();
                
                if (!res.ok) {
                    setError(data.error || "Erro ao processar autenticação");
                    setTimeout(() => router.push('/login'), 4000);
                    return;
                }
                
                await login(data.user, data.token);
                setStatus("Autenticação concluída! Redirecionando...");
                setTimeout(() => {
                    if (action === 'signup') {
                        router.push('/onboarding');
                    } else {
                        router.push('/');
                    }
                }, 1000);
            } catch (err) {
                console.error("Erro na autenticação:", err);
                setError(err.message || "Falha na autenticação OAuth.");
                setTimeout(() => router.push('/login'), 4000);
            }
        };

        performAuth();
    }, [searchParams, router, login]);

    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-background text-primary px-4">
            <div className="w-full max-w-md bg-foreground/20 backdrop-blur-xl border border-white/10 p-8 rounded-3xl shadow-2xl flex flex-col items-center">
                {error ? (
                    <>
                        <div className="w-16 h-16 bg-red-500/20 text-red-500 rounded-full flex items-center justify-center mb-6">
                            <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </div>
                        <h2 className="text-xl font-bold mb-2">Erro na Autenticação</h2>
                        <p className="text-secondary text-center mb-4">{error}</p>
                        <p className="text-sm text-secondary/50">Redirecionando para o login...</p>
                    </>
                ) : (
                    <>
                        <svg className="w-16 h-16 text-accent animate-spin mb-6" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                        </svg>
                        <h2 className="text-xl font-bold mb-2">Aguarde um momento</h2>
                        <p className="text-secondary text-center">{status}</p>
                    </>
                )}
            </div>
        </div>
    );
}

export default function OAuthCallbackPage() {
    return (
        <Suspense fallback={
            <div className="flex items-center justify-center min-h-screen bg-background text-primary">
                <svg className="w-10 h-10 animate-spin text-accent" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
            </div>
        }>
            <OAuthCallbackContent />
        </Suspense>
    );
}
