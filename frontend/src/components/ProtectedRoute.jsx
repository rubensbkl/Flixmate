"use client";

import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function ProtectedRoute({ children }) {
    const { user, loading } = useAuth();
    const router = useRouter();

    useEffect(() => {
        if (!loading && !user) {
            router.push("/login");
        }
    }, [loading, user, router]);

    if (loading || !user) {
        return (
            <div className="flex items-center justify-center h-screen bg-black text-white">
                <div className="flex flex-col items-center space-y-4">
                    {/* Spinner animado */}
                    <div className="animate-spin h-12 w-12 rounded-full border-4 border-t-transparent border-blue-500"></div>


                    {/* Texto pulsante */}
                    <p className="text-lg animate-pulse tracking-wide text-gray-300">
                        Verificando acesso ao Flixmate...
                    </p>
                </div>
            </div>
        );
    }

    return children;
}
