'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { createContext, useContext, useEffect, useState } from 'react';
import {
    decodeToken,
    getToken,
    getCurrentUser,
    isAuthenticated,
    clearAuth,
    saveToken
} from '@/lib/auth';

const AuthContext = createContext();

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isInitialized, setIsInitialized] = useState(false);
    const router = useRouter();
    const searchParams = useSearchParams();

    // Inicialização SÍNCRONA no client
    useEffect(() => {
        const initAuth = () => {
            try {
                const userData = getCurrentUser();
                if (userData) {
                    console.log('✅ Usuário autenticado:', userData);
                    setUser(userData);
                } else {
                    console.log('❌ Nenhum usuário autenticado');
                    setUser(null);
                }
            } catch (error) {
                console.error('Erro na inicialização da auth:', error);
                setUser(null);
            } finally {
                setLoading(false);
                setIsInitialized(true);
            }
        };

        // Aguardar hidratação do React
        const timer = setTimeout(initAuth, 0);
        return () => clearTimeout(timer);
    }, []);

    // Verificação assíncrona opcional (para validar com servidor)
    const verifyWithServer = async (token) => {
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
                // Token válido no servidor, atualizar dados se necessário
                const serverUser = data.user;
                const localUser = getCurrentUser();

                // Combinar dados locais (do JWT) com dados do servidor
                const combinedUser = {
                    ...localUser,
                    ...serverUser,
                    userId: localUser.userId, // Manter o userId do JWT
                };

                setUser(combinedUser);
                return true;
            } else {
                // Token inválido no servidor
                console.warn('Token rejeitado pelo servidor');
                logout();
                return false;
            }
        } catch (error) {
            console.error('Erro ao verificar com servidor:', error);
            // Em caso de erro de rede, manter autenticação local
            return null; // null = erro de rede
        }
    };

    const login = async (userData, token) => {
        try {
            // Salvar token
            saveToken(token);

            // Decodificar e obter dados completos
            const decodedUser = decodeToken(token);
            if (!decodedUser) {
                throw new Error('Token inválido');
            }

            // Combinar dados do login com dados do JWT
            const fullUser = {
                ...userData,
                userId: decodedUser.userId,
                email: decodedUser.email || userData.email,
            };

            setUser(fullUser);
            setLoading(false);

            console.log('✅ Login realizado:', fullUser);

            // Verificar com servidor em background
            verifyWithServer(token);

            // Redirecionar
            const redirectTo = searchParams.get('redirect') || '/';
            router.replace(redirectTo);

        } catch (error) {
            console.error('Erro no login:', error);
            logout();
        }
    };

    const logout = () => {
        clearAuth();
        setUser(null);
        setLoading(false);
        console.log('👋 Logout realizado');
        router.replace('/login');
    };

    // Função para refrescar dados do usuário
    const refreshUser = () => {
        const userData = getCurrentUser();
        setUser(userData);
        return userData;
    };

    // Função para obter userId de forma segura
    const getUserId = () => {
        return user?.userId || getCurrentUser()?.userId || null;
    };

    const value = {
        user,
        userId: getUserId(), // Sempre disponível
        isAuthenticated: !!user,
        loading,
        isInitialized,
        login,
        logout,
        refreshUser,
        verifyWithServer,
        getUserId,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth deve ser usado dentro de um AuthProvider');
    }
    return context;
}

// Hook para garantir que o userId está sempre disponível
export function useUserId() {
    const { userId, getUserId } = useAuth();

    // Se não tem no state, tenta pegar diretamente do token
    if (!userId) {
        return getUserId();
    }

    return userId;
}