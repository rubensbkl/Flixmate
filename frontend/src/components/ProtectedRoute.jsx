"use client";

import { useAuth } from "@/contexts/AuthContext";

export default function ProtectedRoute({ children }) {
    const { loading } = useAuth();

    // Se está carregando, mostrar loading
    if (loading) {
        return (
            <div className="flex items-center justify-center h-screen bg-background text-white">
                <div className="flex flex-col items-center space-y-4">
                    {/* Spinner animado */}
                    <div className="animate-spin h-12 w-12 rounded-full border-4 border-t-transparent border-accent"></div>

                    {/* Texto pulsante */}
                    <p className="text-lg animate-pulse tracking-wide text-secondary">
                        Verificando acesso ao Flixmate...
                    </p>
                </div>
            </div>
        );
    }

    // O middleware + AuthContext cuidam dos redirecionamentos
    // Aqui só renderizamos o conteúdo
    return children;
}