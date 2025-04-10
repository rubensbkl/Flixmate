'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'

import {
    HomeIcon as HomeOutline,
    UserIcon as UserOutline,
    ClockIcon as ClockOutline,
    MagnifyingGlassIcon as SearchOutline
} from '@heroicons/react/24/outline'

import {
    HomeIcon as HomeSolid,
    UserIcon as UserSolid,
    ClockIcon as ClockSolid,
    MagnifyingGlassIcon as SearchSolid
} from '@heroicons/react/24/solid'

export default function Navbar() {
    const pathname = usePathname()

    const navItems = [
        {
            name: 'Home',
            href: '/',
            icon: (active) => active ? <HomeSolid className="w-7 h-7" /> : <HomeOutline className="w-7 h-7" />
        },
        {
            name: 'Recomendações',
            href: '/recommendations',
            icon: (active) => active ? <ClockSolid className="w-7 h-7" /> : <ClockOutline className="w-7 h-7" />
        },
        {
            name: 'Procurar',
            href: '/search',
            icon: (active) => active ? <SearchSolid className="w-7 h-7" /> : <SearchOutline className="w-7 h-7" />
        },
        {
            name: 'Perfil',
            href: '/profile',
            icon: (active) => active ? <UserSolid className="w-7 h-7" /> : <UserOutline className="w-7 h-7" />
        }
    ]

    return (
        <nav className="fixed bottom-0 left-0 right-0 bg-white shadow-md z-10 md:top-0 md:bottom-0 md:w-56 md:h-full md:flex md:flex-col md:justify-start">
            <ul className="flex justify-around items-center p-6 md:flex-col md:items-start md:gap-6 md:pt-10 md:px-4">
                {navItems.map((item) => {
                    const isActive = pathname === item.href
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
                    )
                })}
            </ul>
        </nav>
    )
}
