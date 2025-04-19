// src/components/Navbar.jsx
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

    const linkClass = (path) =>
        `flex items-center px-4 py-3 ${
            pathname === path
                ? "text-blue-600 bg-blue-50"
                : "text-gray-700 hover:bg-gray-100"
        } rounded-lg transition-colors`;

    // Mobile bottom navigation
    if (isMobile) {
        return (
            <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 flex justify-around items-center h-16 z-10 safe-area-bottom">
                <Link
                    href="/"
                    className={`flex flex-col items-center justify-center p-2 ${
                        pathname === "/" ? "text-blue-600" : "text-gray-700"
                    }`}
                >
                    <HomeIcon className="w-6 h-6" />
                    <span className="text-xs mt-1">Home</span>
                </Link>

                <Link
                    href="/search"
                    className={`flex flex-col items-center justify-center p-2 ${
                        pathname === "/search"
                            ? "text-blue-600"
                            : "text-gray-700"
                    }`}
                >
                    <MagnifyingGlassIcon className="w-6 h-6" />
                    <span className="text-xs mt-1">Buscar</span>
                </Link>

                <Link
                    href="/recommendations"
                    className={`flex flex-col items-center justify-center p-2 ${
                        pathname === "/recommendations"
                            ? "text-blue-600"
                            : "text-gray-700"
                    }`}
                >
                    <FilmIcon className="w-6 h-6" />
                    <span className="text-xs mt-1">Filmes</span>
                </Link>

                <Link
                    href={`/profile`}
                    className={`flex flex-col items-center justify-center p-2 ${
                        pathname.startsWith("/profile")
                            ? "text-blue-600"
                            : "text-gray-700"
                    }`}
                >
                    <UserIcon className="w-6 h-6" />
                    <span className="text-xs mt-1">Perfil</span>
                </Link>
                {/* Logout */}
                <button
                    onClick={handleLogout}
                    className="flex text-red-700 flex-col items-center justify-center p-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
                >
                    <ArrowLeftStartOnRectangleIcon className="w-6 h-6" />
                    <span className="text-xs mt-1">Sair</span>
                </button>
            </nav>
        );
    }

    // Desktop sidebar
    return (
        <aside className="w-64 bg-white h-full border-r border-gray-200 flex flex-col">
            <div className="p-4 border-b border-gray-200">
                <h1 className="text-xl font-bold text-gray-800">FlixMate</h1>
                <p className="text-sm text-gray-500">
                    Seu assistente de filmes
                </p>
            </div>

            <nav className="flex-1 p-4 space-y-1">
                <Link href="/" className={linkClass("/")}>
                    <HomeIcon className="w-5 h-5 mr-3" />
                    Home
                </Link>
                <Link href="/search" className={linkClass("/search")}>
                    <MagnifyingGlassIcon className="w-5 h-5 mr-3" />
                    Buscar
                </Link>
                <Link
                    href="/recommendations"
                    className={linkClass("/recommendations")}
                >
                    <FilmIcon className="w-5 h-5 mr-3" />
                    Recomendações
                </Link>
                <Link
                    href={`/profile`}
                    className={linkClass(`/profile`)}
                >
                    <UserIcon className="w-5 h-5 mr-3" />
                    Meu Perfil
                </Link>
            </nav>

            <div className="p-4 border-t border-gray-200">
                <button
                    onClick={handleLogout}
                    className="flex text-red-700 items-center w-full px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
                >
                    <ArrowLeftStartOnRectangleIcon className="w-5 h-5 mr-3" />
                    Sair
                </button>
            </div>
        </aside>
    );
}
