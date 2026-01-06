import { NextResponse } from "next/server"
import type { NextRequest } from "next/server"

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl
  
  // Si l'utilisateur est sur la racine "/" et n'est pas connecté, rediriger vers /welcome
  if (pathname === "/") {
    const token = request.cookies.get("auth_token")
    
    if (!token) {
      return NextResponse.redirect(new URL("/welcome", request.url))
    }
  }
  
  return NextResponse.next()
}

export const config = {
  matcher: ["/"],
}
