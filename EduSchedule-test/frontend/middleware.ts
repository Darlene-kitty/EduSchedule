import { NextResponse } from "next/server"
import type { NextRequest } from "next/server"

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl
  
  // Pages publiques qui ne nécessitent pas d'authentification
  const publicPages = ["/welcome", "/login", "/register", "/forgot-password", "/reset-password", "/verify-email"]
  
  // Si c'est une page publique, laisser passer
  if (publicPages.some(page => pathname.startsWith(page))) {
    return NextResponse.next()
  }
  
  // Pour les autres pages, on laisse le AuthGuard côté client gérer l'authentification
  // car les tokens sont stockés dans localStorage, pas dans les cookies
  return NextResponse.next()
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
}
