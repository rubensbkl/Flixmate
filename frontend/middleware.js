// middleware.js - Na raiz do projeto
import { NextResponse } from 'next/server';
import { jwtDecode } from 'jwt-decode';

// Rotas que não precisam de autenticação
const publicPaths = ['/login', '/signup'];

// Função para verificar se o token é válido
function isValidToken(token) {
  if (!token) return false;
  
  try {
    const decoded = jwtDecode(token);
    const now = Date.now() / 1000;
    
    // Verificar se não expirou
    if (decoded.exp && decoded.exp < now) {
      return false;
    }
    
    // Verificar se tem userId
    if (!decoded.userId) {
      return false;
    }
    
    return true;
  } catch {
    return false;
  }
}

export function middleware(request) {
  const { pathname } = request.nextUrl;
  
  // Permitir rotas públicas
  if (publicPaths.includes(pathname)) {
    return NextResponse.next();
  }
  
  // Permitir assets estáticos
  if (pathname.startsWith('/_next') || 
      pathname.startsWith('/static') ||
      pathname.includes('.')) {
    return NextResponse.next();
  }
  
  // Verificar token
  const token = request.cookies.get('token')?.value || request.headers.get('authorization')?.replace('Bearer ', '');
  
  if (!isValidToken(token)) {
    const loginUrl = new URL('/login', request.url);
    if (pathname !== '/') {
      loginUrl.searchParams.set('redirect', pathname);
    }
    
    return NextResponse.redirect(loginUrl);
  }
  
  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
};