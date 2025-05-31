"use client";

import { useAuth } from "@/contexts/AuthContext";
import {
    ArrowLeftStartOnRectangleIcon,
    FilmIcon,
    HomeIcon,
    MagnifyingGlassIcon,
    UserIcon,
} from "@heroicons/react/24/outline";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";

export default function Navbar() {
    const { user, logout } = useAuth();
    const pathname = usePathname();
    const [isMobile, setIsMobile] = useState(false);

    useEffect(() => {
        const checkMobile = () => {
            setIsMobile(window.innerWidth < 768);
        };

        checkMobile();
        window.addEventListener("resize", checkMobile);
        return () => window.removeEventListener("resize", checkMobile);
    }, []);

    const handleLogout = () => {
        logout();
    };

    // Mobile bottom navigation - versão melhorada
    if (isMobile) {
        return (
            <>
                <nav className="fixed bottom-0 left-0 right-0 bg-background/95 backdrop-blur-sm border-t border-foreground flex justify-around items-center h-16 z-50 safe-area-pb">
                    <Link
                        href="/"
                        className={`flex flex-col items-center justify-center p-2 rounded-lg transition-colors ${pathname === "/"
                                ? "text-accent"
                                : "text-secondary hover:text-accent"
                            }`}
                    >
                        <HomeIcon className="w-5 h-5" />
                        <span className="text-xs mt-1 font-medium">Home</span>
                    </Link>

                    <Link
                        href="/profile/search"
                        className={`flex flex-col items-center justify-center p-2 rounded-lg transition-colors ${pathname.startsWith("/profile/") &&
                                !pathname.startsWith(`/profile/${user?.id}`) &&
                                !pathname.startsWith("/profile/edit")
                                ? "text-accent"
                                : "text-secondary hover:text-accent"
                            }`}
                    >
                        <MagnifyingGlassIcon className="w-5 h-5" />
                        <span className="text-xs mt-1 font-medium">Buscar</span>
                    </Link>

                    <Link
                        href="/movie/search"
                        className={`flex flex-col items-center justify-center p-2 rounded-lg transition-colors ${pathname.startsWith("/movie/search")
                                ? "text-accent"
                                : "text-secondary hover:text-accent"
                            }`}
                    >
                        <FilmIcon className="w-5 h-5" />
                        <span className="text-xs mt-1 font-medium">Filmes</span>
                    </Link>

                    <Link
                        href={`/profile/${user?.id}`}
                        className={`flex flex-col items-center justify-center p-2 rounded-lg transition-colors ${(pathname.startsWith(`/profile/${user?.id}`) || pathname === `/profile/edit`)
                                ? "text-accent"
                                : "text-secondary hover:text-accent"
                            }`}
                    >
                        <UserIcon className="w-5 h-5" />
                        <span className="text-xs mt-1 font-medium">Perfil</span>
                    </Link>

                    <button
                        onClick={handleLogout}
                        className="flex flex-col items-center justify-center p-2 rounded-lg text-red-400 hover:text-red-300 hover:bg-red-500/10 transition-colors"
                    >
                        <ArrowLeftStartOnRectangleIcon className="w-5 h-5" />
                        <span className="text-xs mt-1 font-medium">Sair</span>
                    </button>
                </nav>
            </>
        );
    }

    // Desktop sidebar (mantém como está)
    return (
        <aside className="w-64 h-full flex flex-col">
            <div className="p-4 pb-10">
                <h1 className="text-xl font-bold text-primary">FlixMate</h1>
                <p className="text-sm text-accent">Seu assistente de filmes</p>
            </div>

            <nav className="flex-1 space-y-2.5">
                <Link
                    href="/"
                    className={`flex items-center px-4 py-3 rounded-r-xl transition-colors ${pathname === "/"
                            ? "text-primary bg-foreground font-bold"
                            : "text-secondary hover:bg-foreground"
                        }`}
                >
                    <HomeIcon className="w-5 h-5 mr-3" />
                    Home
                </Link>
                <Link
                    href="/profile/search"
                    className={
                        pathname.startsWith("/profile/") &&
                            !pathname.startsWith(`/profile/${user?.id}`) &&
                            !pathname.startsWith("/profile/edit")
                            ? "flex items-center px-4 py-3 rounded-r-xl text-primary bg-foreground transition-colors"
                            : "flex items-center px-4 py-3 rounded-r-xl text-secondary hover:bg-foreground transition-colors"
                    }
                >
                    <MagnifyingGlassIcon className="w-5 h-5 mr-3" />
                    Buscar
                </Link>
                <Link
                    href={`/movie/search`}
                    className={
                        pathname.startsWith("/movie/search")
                            ? "flex items-center px-4 py-3 rounded-r-xl text-primary bg-foreground transition-colors"
                            : "flex items-center px-4 py-3 rounded-r-xl text-secondary hover:bg-foreground transition-colors"
                    }
                >
                    <FilmIcon className="w-5 h-5 mr-3" />
                    Filmes
                </Link>
                <Link
                    href={`/profile/${user?.id}`}
                    className={
                        pathname.startsWith(`/profile/${user?.id}`) || pathname === `/profile/edit`
                            ? "flex items-center px-4 py-3 rounded-r-xl text-primary bg-foreground transition-colors"
                            : "flex items-center px-4 py-3 rounded-r-xl text-secondary hover:bg-foreground transition-colors"
                    }
                >
                    <UserIcon className="w-5 h-5 mr-3" />
                    Meu Perfil
                </Link>
            </nav>

            <div className="pb-4">
                <button
                    onClick={handleLogout}
                    className="flex items-center w-full px-4 py-3 rounded-r-xl text-secondary hover:bg-foreground transition-colors"
                >
                    <ArrowLeftStartOnRectangleIcon className="w-5 h-5 mr-3" />
                    Sair
                </button>
            </div>
        </aside>
    );
}