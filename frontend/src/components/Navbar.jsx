'use client';

import { useAuth } from '@/contexts/AuthContext';
import {
    ClockIcon as ClockOutline,
    HomeIcon as HomeOutline,
    ArrowRightOnRectangleIcon as LogoutIcon,
    MagnifyingGlassIcon as SearchOutline,
    UserIcon as UserOutline
} from '@heroicons/react/24/outline';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

import {
    ClockIcon as ClockSolid,
    HomeIcon as HomeSolid,
    MagnifyingGlassIcon as SearchSolid,
    UserIcon as UserSolid
} from '@heroicons/react/24/solid';

export default function Navbar() {
    const pathname = usePathname();
    const { logout, user } = useAuth();

    const navItems = [
        {
            name: 'Home',
            href: '/',
            icon: (active) => active ? <HomeSolid className="w-6 h-6 md:w-7 md:h-7" /> : <HomeOutline className="w-6 h-6 md:w-7 md:h-7" />
        },
        {
            name: 'Recomendações',
            href: '/recommendations',
            icon: (active) => active ? <ClockSolid className="w-6 h-6 md:w-7 md:h-7" /> : <ClockOutline className="w-6 h-6 md:w-7 md:h-7" />
        },
        {
            name: 'Procurar',
            href: '/search',
            icon: (active) => active ? <SearchSolid className="w-6 h-6 md:w-7 md:h-7" /> : <SearchOutline className="w-6 h-6 md:w-7 md:h-7" />
        },
        {
            name: 'Perfil',
            href: '/profile',
            icon: (active) => active ? <UserSolid className="w-6 h-6 md:w-7 md:h-7" /> : <UserOutline className="w-6 h-6 md:w-7 md:h-7" />
        }
    ];

    return (
        <nav className="fixed bottom-0 left-0 right-0 bg-white shadow-md z-10 md:static md:w-56 md:h-auto md:flex md:flex-col md:justify-start">
            {user && (
                <div className="hidden md:flex md:flex-col md:items-center md:mt-6 md:mb-8">
                    <div className="h-12 w-12 bg-gray-200 rounded-full flex items-center justify-center overflow-hidden mb-2">
                        <UserOutline className="h-6 w-6 text-gray-600" />
                    </div>
                    <div className="text-sm font-medium">{user.firstName} {user.lastName}</div>
                    <div className="text-xs text-gray-500 mb-2">{user.email}</div>
                    <button 
                        onClick={logout} 
                        className="mt-1 flex items-center text-xs text-red-500 hover:text-red-700"
                    >
                        <LogoutIcon className="w-4 h-4 mr-1" />
                        Sair
                    </button>
                </div>
            )}
            
            <ul className="flex justify-around items-center p-3 md:flex-col md:items-start md:gap-6 md:pt-4 md:px-4">
                {navItems.map((item) => {
                    const isActive = pathname === item.href;
                    return (
                        <li key={item.name} className="w-full">
                            <Link
                                href={item.href}
                                className={`flex flex-col items-center text-sm md:flex-row md:gap-3 md:items-center ${
                                    isActive ? 'text-blue-600 font-medium' : 'text-gray-600'
                                }`}
                            >
                                {item.icon(isActive)}
                                <span className="hidden md:block text-sm">{item.name}</span>
                            </Link>
                        </li>
                    );
                })}
                
                {/* Botão de logout para versão mobile */}
                {user && (
                    <li className="md:hidden">
                        <button
                            onClick={logout}
                            className="flex flex-col items-center text-sm text-red-500"
                        >
                            <LogoutIcon className="w-6 h-6" />
                            <span className="text-xs">Sair</span>
                        </button>
                    </li>
                )}
            </ul>
        </nav>
    );
}