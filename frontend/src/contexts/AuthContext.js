// frontend/src/contexts/AuthContext.js
"use client";

import { createContext, useContext, useEffect, useState } from "react";
import {
  decodeToken,
  getCurrentUser,
  clearAuth,
  saveToken,
  getToken,
} from "@/lib/auth";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    const initAuth = async () => {
      try {
        const token = getToken();
        if (!token) {
            setUser(null);
            setLoading(false);
            setIsInitialized(true);
            return;
        }

        const decoded = decodeToken(token);
        if (!decoded) {
            clearAuth();
            setUser(null);
            setLoading(false);
            setIsInitialized(true);
            return;
        }

        // Tentar obter dados iniciais rápidos do localStorage
        const sessionDataStr = localStorage.getItem('session_data');
        let initialUser = { ...decoded };
        if (sessionDataStr) {
          try {
              initialUser = { ...initialUser, ...JSON.parse(sessionDataStr) };
          } catch(e) {}
        }
        setUser(initialUser);

        // Buscar dados completos e atualizados do backend
        try {
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/verify`, {
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (res.ok) {
                const data = await res.json();
                if (data.valid && data.user) {
                    const fullUser = { ...decoded, ...data.user, userId: data.user.id };
                    setUser(fullUser);
                    localStorage.setItem('session_data', JSON.stringify(fullUser));
                }
            } else {
                clearAuth();
                setUser(null);
            }
        } catch (fetchErr) {
            console.error("Erro ao verificar token no backend:", fetchErr);
        }
      } catch (err) {
        setUser(null);
      } finally {
        setLoading(false);
        setIsInitialized(true);
      }
    };
    initAuth();
  }, []);

  const login = async (userData, token) => {
    try {
      if (!token) throw new Error("Token não fornecido");
      if (!userData) throw new Error("Dados de usuário não fornecidos");

      saveToken(token);
      localStorage.setItem('session_data', JSON.stringify(userData));
      const decodedUser = decodeToken(token);
      if (!decodedUser) throw new Error(`Falha ao decodificar token: ${token.substring(0, 10)}...`);

      const fullUser = {
        ...userData,
        userId: decodedUser.userId,
        email: decodedUser.email ?? userData.email,
      };
      setUser(fullUser);
      setLoading(false);
    } catch (error) {
      console.error("Erro no login:", error);
      throw error;
    }
  };

  const logout = () => {
    clearAuth();
    setUser(null);
    setLoading(false);
    window.location.href = "/login"; // Redireciona para a página de login
  };

  // Método para pegar o userId diretamente do token se user estiver null
  function getUserId() {
    const current = getCurrentUser();
    return current?.userId || null;
  }

  const value = {
    user,
    isAuthenticated: !!user,
    loading,
    isInitialized,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context)
    throw new Error("useAuth deve ser usado dentro de um AuthProvider");
  return context;
}

export function useUserId() {
  const { user } = useAuth();
  if (user && user.userId) {
    return user.userId;
  }
  return getCurrentUser()?.userId || null;
}