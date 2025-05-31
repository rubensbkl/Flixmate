// middleware.js (na raiz do projeto, mesmo nível que package.json)

import { NextResponse } from 'next/server';

export function middleware(request) {
    console.log('🔍 Middleware executado para:', request.nextUrl.pathname);
    
    const { pathname } = request.nextUrl;
    
    // Rotas que NÃO precisam de autenticação
    const publicRoutes = ['/login', '/signup'];
    
    // Pular verificação para arquivos estáticos e API
    if (
        pathname.startsWith('/_next') ||
        pathname.startsWith('/api') ||
        pathname.includes('.') ||
        pathname === '/favicon.ico'
    ) {
        console.log('🚫 Pulando verificação para:', pathname);
        return NextResponse.next();
    }
    
    // Se é rota pública, permitir
    if (publicRoutes.includes(pathname)) {
        console.log('✅ Rota pública permitida:', pathname);
        return NextResponse.next();
    }
    
    // Verificar token no cookie
    const token = request.cookies.get('token')?.value;
    console.log('🔑 Token encontrado:', token ? 'SIM' : 'NÃO');
    
    // Se não tem token, redirecionar para login
    if (!token) {
        console.log('❌ Sem token, redirecionando para login');
        const loginUrl = new URL('/login', request.url);
        if (pathname !== '/') {
            loginUrl.searchParams.set('redirect', pathname);
        }
        return NextResponse.redirect(loginUrl);
    }
    
    console.log('✅ Token presente, permitindo acesso');
    return NextResponse.next();
}

