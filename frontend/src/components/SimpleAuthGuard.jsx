'use client';

import { usePathname, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { isAuthenticated } from '@/lib/auth';

function SimpleAuthGuard({ children }) {
    const pathname = usePathname();
    const router = useRouter();
    const [isAuthorized, setIsAuthorized] = useState(null); // null = checking

    // Rotas públicas
    const publicRoutes = ['/login', '/signup'];
    const isPublicRoute = publicRoutes.includes(pathname);

    useEffect(() => {
        const checkAuth = () => {
            if (typeof window === 'undefined') return;

            if (isPublicRoute) {
                setIsAuthorized(true);
                return;
            }

            // Verificação SÍNCRONA usando o helper JWT
            const authenticated = isAuthenticated();
            
            if (authenticated) {
                setIsAuthorized(true);
            } else {
                router.replace('/login');
            }
        };

        // Executar imediatamente após hidratação
        const timer = setTimeout(checkAuth, 0);
        return () => clearTimeout(timer);
    }, [pathname, isPublicRoute, router]);

    // Se ainda está verificando, mostrar loading
    if (isAuthorized === null) {
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

export default SimpleAuthGuard;