// components/FastAuthGuard.js - Guard que bloqueia IMEDIATAMENTE

'use client';

import { usePathname, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

function FastAuthGuard({ children }) {
    const pathname = usePathname();
    const router = useRouter();
    const [isChecking, setIsChecking] = useState(true);
    const [isAuthorized, setIsAuthorized] = useState(false);

    // Rotas públicas
    const publicRoutes = ['/login', '/signup'];
    const isPublicRoute = publicRoutes.includes(pathname);

    // Função helper para pegar cookie
    const getCookie = (name) => {
        if (typeof document === 'undefined') return null;
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
        return null;
    };

    useEffect(() => {
        const checkAuthSync = () => {
            // Verificação SÍNCRONA e INSTANTÂNEA
            const token = localStorage.getItem('token') || getCookie('token');
            
            if (isPublicRoute) {
                // Rota pública - sempre autorizada
                setIsAuthorized(true);
                setIsChecking(false);
                return;
            }

            if (!token) {
                // Sem token - redirecionar IMEDIATAMENTE
                router.replace('/login');
                return;
            }

            // Com token - autorizado (verificação assíncrona pode vir depois)
            setIsAuthorized(true);
            setIsChecking(false);
        };

        checkAuthSync();
    }, [pathname, isPublicRoute, router]);

    // Se ainda está verificando OU não autorizado, mostrar loading
    if (isChecking || (!isAuthorized && !isPublicRoute)) {
        return (
            <div className="fixed inset-0 flex items-center justify-center bg-background z-50">
                <div className="flex flex-col items-center space-y-4">
                    <div className="animate-spin h-12 w-12 rounded-full border-4 border-t-transparent border-accent"></div>
                    <p className="text-lg animate-pulse tracking-wide text-secondary">
                        Verificando acesso...
                    </p>
                </div>
            </div>
        );
    }

    return children;
}

export default FastAuthGuard;