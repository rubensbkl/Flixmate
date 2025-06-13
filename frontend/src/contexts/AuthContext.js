// frontend/src/contexts/AuthContext.js
"use client";

import { createContext, useContext, useEffect, useState } from "react";
import {
  decodeToken,
  getCurrentUser,
  clearAuth,
  saveToken,
} from "@/lib/auth";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    const initAuth = () => {
      try {
        const userData = getCurrentUser();
        setUser(userData);
      } catch {
        setUser(null);
      } finally {
        setLoading(false);
        setIsInitialized(true);
      }
    };
    const timer = setTimeout(initAuth, 0);
    return () => clearTimeout(timer);
  }, []);

  const login = async (userData, token) => {
    try {
      saveToken(token);
      const decodedUser = decodeToken(token);
      if (!decodedUser) throw new Error("Token inválido");

      const fullUser = {
        ...userData,
        userId: decodedUser.userId,
        email: decodedUser.email ?? userData.email,
      };
      setUser(fullUser);
      setLoading(false);
    } catch (error) {
      logout();
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