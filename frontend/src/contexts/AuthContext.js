'use client';

import { useRouter } from 'next/navigation';
import { createContext, useContext, useEffect, useState } from 'react';

const AuthContext = createContext();

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const router = useRouter();

    // Verifica o token e atualiza o estado
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
                logout();
                return false;
            }
        } catch (error) {
            console.error('Erro ao verificar token:', error);
            logout();
            return false;
        }
    };

    useEffect(() => {
        const checkAuth = async () => {
            const token = localStorage.getItem('token');

            if (token) {
                await verifyToken(token);
            }

            setLoading(false);
        };

        checkAuth();
    }, []);

    const login = (userData, token) => {
        localStorage.setItem('token', token);
        setUser(userData);
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('session_data');
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