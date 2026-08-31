"use client";

import { useAuth, useUserId } from "@/contexts/AuthContext";
import {
    ArrowLeftStartOnRectangleIcon,
    FilmIcon,
    HomeIcon,
    MagnifyingGlassIcon,
    UserIcon,
    Cog6ToothIcon,
} from "@heroicons/react/24/outline";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import SettingsModal from "./SettingsModal";

export default function Navbar() {
    const { user, logout } = useAuth();
    const pathname = usePathname();
    const [isMobile, setIsMobile] = useState(false);

    
    const userId = useUserId(); // ← Sempre funciona!

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

    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const [isSettingsOpen, setIsSettingsOpen] = useState(false);

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
                                !pathname.startsWith(`/profile/${userId}`) &&
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
                        href={`/profile/${userId}`}
                        className={`flex flex-col items-center justify-center p-2 rounded-lg transition-colors ${(pathname.startsWith(`/profile/${userId}`) || pathname === `/profile/edit`)
                                ? "text-accent"
                                : "text-secondary hover:text-accent"
                            }`}
                    >
                        <UserIcon className="w-5 h-5" />
                        <span className="text-xs mt-1 font-medium">Perfil</span>
                    </Link>

                    <button
                        onClick={() => setIsSettingsOpen(true)}
                        className="flex flex-col items-center justify-center p-2 rounded-lg text-secondary hover:text-accent transition-colors"
                    >
                        <Cog6ToothIcon className="w-5 h-5" />
                        <span className="text-xs mt-1 font-medium">Config</span>
                    </button>
                </nav>
                <SettingsModal isOpen={isSettingsOpen} onClose={() => setIsSettingsOpen(false)} />
            </>
        );
    }

    // Desktop sidebar (mantém como está)
    return (
        <aside className="w-64 h-full flex flex-col relative z-40">
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
                            !pathname.startsWith(`/profile/${userId}`) &&
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
                    href={`/profile/${userId}`}
                    className={
                        pathname.startsWith(`/profile/${userId}`) || pathname === `/profile/edit`
                            ? "flex items-center px-4 py-3 rounded-r-xl text-primary bg-foreground transition-colors"
                            : "flex items-center px-4 py-3 rounded-r-xl text-secondary hover:bg-foreground transition-colors"
                    }
                >
                    <UserIcon className="w-5 h-5 mr-3" />
                    Meu Perfil
                </Link>

                {user?.isAdmin && (
                    <>
                        <div className="pt-4 pb-2 px-4">
                            <p className="text-xs font-bold text-secondary uppercase tracking-wider">
                                Administração
                            </p>
                        </div>
                        <Link
                            href="/admin"
                            className={
                                pathname.startsWith("/admin")
                                    ? "flex items-center px-4 py-3 rounded-r-xl text-accent bg-accent/10 border-l-4 border-accent transition-colors"
                                    : "flex items-center px-4 py-3 rounded-r-xl text-secondary hover:bg-foreground transition-colors"
                            }
                        >
                            <svg className="w-5 h-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                            </svg>
                            Painel Admin
                        </Link>
                    </>
                )}
            </nav>

            <div className="p-4 relative">
                {isDropdownOpen && (
                    <>
                        <div 
                            className="fixed inset-0 z-30"
                            onClick={() => setIsDropdownOpen(false)}
                        ></div>
                        <div className="absolute bottom-full left-4 right-4 mb-2 bg-foreground border border-white/10 rounded-xl shadow-xl overflow-hidden z-40 animate-fadeIn">
                            <button
                                onClick={() => { setIsDropdownOpen(false); setIsSettingsOpen(true); }}
                                className="w-full flex items-center px-4 py-3 text-sm text-secondary hover:text-primary hover:bg-white/5 transition-colors"
                            >
                                <Cog6ToothIcon className="w-4 h-4 mr-3" />
                                Configurações
                            </button>
                            <button
                                onClick={handleLogout}
                                className="w-full flex items-center px-4 py-3 text-sm text-red-400 hover:text-red-300 hover:bg-red-500/10 transition-colors border-t border-white/5"
                            >
                                <ArrowLeftStartOnRectangleIcon className="w-4 h-4 mr-3" />
                                Sair
                            </button>
                        </div>
                    </>
                )}
                
                <button
                    onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                    className="flex items-center w-full p-2 rounded-xl hover:bg-foreground transition-colors text-left"
                >
                    <div className="w-10 h-10 rounded-full bg-accent/20 flex items-center justify-center text-accent font-bold shrink-0">
                        {user?.firstName?.[0] || 'U'}
                    </div>
                    <div className="ml-3 overflow-hidden">
                        <p className="text-sm font-medium text-primary truncate">
                            {user?.firstName} {user?.lastName}
                        </p>
                        <p className="text-xs text-secondary truncate">
                            {user?.email}
                        </p>
                    </div>
                </button>
            </div>
            
            <SettingsModal isOpen={isSettingsOpen} onClose={() => setIsSettingsOpen(false)} />
        </aside>
    );
}