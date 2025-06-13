// lib/auth.js - Funções utilitárias para JWT
import { jwtDecode } from 'jwt-decode';

/**
 * Decodifica o JWT e extrai informações do usuário
 * @param {string} token - JWT token
 * @returns {Object|null} - Dados do usuário ou null se inválido
 */
export const decodeToken = (token) => {
    try {
        if (!token) return null;
        
        const decoded = jwtDecode(token);
        const now = Date.now() / 1000;
        
        // Verificar se o token não expirou
        if (decoded.exp && decoded.exp < now) {
            console.warn('Token expirado');
            return null;
        }
        
        return {
            userId: decoded.userId,
            email: decoded.sub, // subject do JWT
            exp: decoded.exp,
            iat: decoded.iat
        };
    } catch (error) {
        return null;
    }
};

/**
 * Obtém o token do localStorage ou cookies
 * @returns {string|null} - Token ou null
 */
export const getToken = () => {
    if (typeof window === 'undefined') return null;
    
    // Primeiro tenta localStorage
    const localToken = localStorage.getItem('token');
    if (localToken) return localToken;
    
    // Fallback para cookies
    const cookieToken = getCookie('token');
    return cookieToken;
};

/**
 * Obtém dados do usuário do token atual
 * @returns {Object|null} - Dados do usuário ou null
 */
export const getCurrentUser = () => {
    const token = getToken();
    return decodeToken(token);
};

/**
 * Verifica se o usuário está autenticado (token válido)
 * @returns {boolean}
 */
export const isAuthenticated = () => {
    const user = getCurrentUser();
    return !!user;
};

/**
 * Remove tokens e limpa autenticação
 */
export const clearAuth = () => {
    if (typeof window !== 'undefined') {
        localStorage.removeItem('token');
        localStorage.removeItem('session_data');
        deleteCookie('token');
    }
};

/**
 * Salva token nos storages
 * @param {string} token 
 */
export const saveToken = (token) => {
    if (typeof window !== 'undefined') {
        localStorage.setItem('token', token);
        setCookie('token', token, 7); // 7 dias
    }
};

// Helper functions for cookies
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