'use client';

import { useRouter } from 'next/navigation';
import { createContext, useContext, useEffect, useState } from 'react';

const AuthContext = createContext();

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const router = useRouter();

    // ✅ Verifica o token e atualiza o estado apenas se for necessário
    const verifyToken = async (token) => {
        try {
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/verify`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            const data = await response.json();

            if (response.ok && data.valid) {
                setUser(data.user);
                return true;
            } else {
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                setUser(null);
                return false;
            }
        } catch (error) {
            console.error('Erro ao verificar token:', error);
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            setUser(null);
            return false;
        }
    };

    useEffect(() => {
        const checkAuth = async () => {
            const savedUser = localStorage.getItem('user');
            const token = localStorage.getItem('token');

            if (savedUser) {
                setUser(JSON.parse(savedUser));
            }

            if (token) {
                await verifyToken(token); // <- só faz isso uma vez
            }

            setLoading(false);
        };

        checkAuth(); // <- não depende de nenhum estado, roda só uma vez
    }, []);

    const login = (userData, token) => {
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(userData));
        setUser(userData);
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
        router.push('/login');
    };

    const isAuthenticated = !!user;

    return (
        <AuthContext.Provider
            value={{
                user,
                isAuthenticated,
                login,
                logout,
                loading,
                verifyToken,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}