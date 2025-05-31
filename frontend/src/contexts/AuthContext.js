'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { createContext, useContext, useEffect, useState } from 'react';

const AuthContext = createContext();

// Funções helper para cookies (mais rápidas)
const setCookie = (name, value, days = 7) => {
    if (typeof document === 'undefined') return;
    const expires = new Date();
    expires.setTime(expires.getTime() + (days * 24 * 60 * 60 * 1000));
    document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/;SameSite=Lax`;
};

const getCookie = (name) => {
    if (typeof document === 'undefined') return null;
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
};

const deleteCookie = (name) => {
    if (typeof document === 'undefined') return;
    document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
};

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const router = useRouter();
    const searchParams = useSearchParams();

    // Verificação INSTANTÂNEA no primeiro carregamento
    useEffect(() => {
        const quickCheck = () => {
            // Verificar se está no browser
            if (typeof window === 'undefined') {
                setLoading(false);
                return;
            }

            // Aguardar hidratação completa
            const timer = setTimeout(() => {
                const token = localStorage.getItem('token') || getCookie('token');
                
                if (token) {
                    // Se tem token, assumir autenticado temporariamente
                    // A verificação real acontece de forma assíncrona
                    setUser({ quickAuth: true, token }); // Estado temporário
                    verifyTokenAsync(token); // Verificação real em background
                } else {
                    setLoading(false);
                }
            }, 0);

            return () => clearTimeout(timer);
        };

        quickCheck();
    }, []);

    // Verificação assíncrona real (em background)
    const verifyTokenAsync = async (token) => {
        try {
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/verify`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            const data = await response.json();

            if (response.ok && data.valid) {
                // Token válido - atualizar com dados reais do usuário
                setUser(data.user);
                setCookie('token', token);
                localStorage.setItem('token', token);
            } else {
                // Token inválido - fazer logout
                console.warn('Token inválido ou expirado');
                logout();
            }
        } catch (error) {
            console.error('Erro ao verificar token:', error);
            // Em caso de erro de rede, manter usuário logado temporariamente
            // mas marcar como erro para retry posterior
            setUser(prev => ({ ...prev, verificationError: true }));
        } finally {
            setLoading(false);
        }
    };

    const login = (userData, token) => {
        // Armazenar em ambos os locais
        localStorage.setItem('token', token);
        setCookie('token', token);
        setUser(userData);
        setLoading(false);
        
        // Verificar se há um redirect pendente
        const redirectTo = searchParams.get('redirect') || '/';
        router.replace(redirectTo);
    };

    const logout = () => {
        // Limpar tudo
        localStorage.removeItem('token');
        localStorage.removeItem('session_data');
        deleteCookie('token');
        setUser(null);
        setLoading(false);
        
        // Redirecionar para login
        router.replace('/login');
    };

    // Função para retry da verificação em caso de erro de rede
    const retryVerification = () => {
        const token = localStorage.getItem('token') || getCookie('token');
        if (token && user?.verificationError) {
            verifyTokenAsync(token);
        }
    };

    const isAuthenticated = !!user && !user.quickAuth;
    const hasValidToken = !!user; // Inclui quickAuth

    return (
        <AuthContext.Provider
            value={{
                user,
                isAuthenticated,
                hasValidToken,
                login,
                logout,
                loading,
                verifyTokenAsync,
                retryVerification,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth deve ser usado dentro de um AuthProvider');
    }
    return context;
}