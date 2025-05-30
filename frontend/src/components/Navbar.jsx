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
    const { logout } = useAuth();
    const pathname = usePathname();
    const [isMobile, setIsMobile] = useState(false);

    // Detect if we're on mobile
    useEffect(() => {
        const checkMobile = () => {
            setIsMobile(window.innerWidth < 768);
        };

        // Check on initial load
        checkMobile();

        // Add event listener for window resize
        window.addEventListener("resize", checkMobile);

        // Cleanup
        return () => window.removeEventListener("resize", checkMobile);
    }, []);

    const handleLogout = () => {
        logout();
    };

    // Mobile bottom navigation
    if (isMobile) {
        return (
            <nav className="fixed flex bottom-0 left-0 right-0 bg-background border-t border-foreground justify-around items-center h-16 z-10 safe-area-bottom">

                <Link
                    href="/"
                    className={`flex flex-col items-center justify-center p-2 ${
                        pathname === "/" 
                            ? "text-accent"
                            : "text-secondary"
                    }`}
                >
                    <HomeIcon className="w-6 h-6" />
                    <span className="text-xs mt-1">Home</span>
                </Link>

                <Link
                    href="/profile/search"
                    className={`flex flex-col items-center justify-center p-2 ${
                        (pathname.startsWith("/profile/")) && !(pathname === "/profile/edit")
                            ? "text-accent"
                            : "text-secondary"
                    }`}
                >
                    <MagnifyingGlassIcon className="w-6 h-6" />
                    <span className="text-xs mt-1">Buscar</span>
                </Link>

                <Link
                    href="/movie/search"
                    className={`flex flex-col items-center justify-center p-2 ${
                        pathname.startsWith("/movie/search")
                            ? "text-accent"
                            : "text-secondary"
                    }`}
                >
                    <FilmIcon className="w-6 h-6" />
                    <span className="text-xs mt-1">Filmes</span>
                </Link>

                <Link
                    href={`/profile`}
                    className={`flex flex-col items-center justify-center p-2 ${
                        pathname === "/profile" || pathname === "/profile/edit"
                            ? "text-accent"
                            : "text-secondary"
                    }`}
                >
                    <UserIcon className="w-6 h-6" />
                    <span className="text-xs mt-1">Perfil</span>
                </Link>
                {/* Logout */}
                <button
                    onClick={handleLogout}
                    className="flex text-red-700 flex-col items-center justify-center p-2 text-secondary hover:bg-foreground rounded-lg transition-colors"
                >
                    <ArrowLeftStartOnRectangleIcon className="w-6 h-6" />
                    <span className="text-xs mt-1">Sair</span>
                </button>
            </nav>
        );
    }

    // Desktop sidebar
    return (
        <aside className="w-64 h-full flex flex-col">
            <div className="p-4 pb-10">
                <h1 className="text-xl font-bold text-primary">FlixMate</h1>
                <p className="text-sm text-accent">
                    Seu assistente de filmes
                </p>
            </div>

            <nav className="flex-1 space-y-2.5">
                <Link
                    href="/"
                    className={`flex items-center px-4 py-3 rounded-r-xl transition-colors ${
                        pathname === "/"
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
                        (pathname.startsWith("/profile/") || pathname == "/search") && !(pathname == "/profile/edit")
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
                    href={`/profile`}
                    className={
                        pathname == "/profile" || pathname == "/profile/edit"
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
