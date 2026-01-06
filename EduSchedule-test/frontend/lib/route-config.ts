import type { UserRole } from "@/contexts/auth-context"

export type ProtectedRoute = {
  path: string
  requiredRoles: UserRole[]
}

export const protectedRoutes: ProtectedRoute[] = [
  { path: "/", requiredRoles: ["admin", "teacher"] },
  { path: "/schedule", requiredRoles: ["admin", "teacher"] },
  { path: "/users", requiredRoles: ["admin"] },
  { path: "/courses", requiredRoles: ["admin", "teacher"] },
  { path: "/reservations", requiredRoles: ["admin", "teacher"] },
  { path: "/resources", requiredRoles: ["admin"] },
  { path: "/conflicts", requiredRoles: ["admin"] },
  { path: "/reports", requiredRoles: ["admin"] },
  { path: "/notifications", requiredRoles: ["admin", "teacher"] },
  { path: "/calendar", requiredRoles: ["admin", "teacher"] },
]

export const publicRoutes = ["/welcome", "/login", "/register", "/forgot-password", "/reset-password", "/verify-email"]

export function isProtectedRoute(path: string): boolean {
  return protectedRoutes.some((route) => route.path === path)
}

export function getRequiredRoles(path: string): UserRole[] {
  const route = protectedRoutes.find((r) => r.path === path)
  return route?.requiredRoles || []
}

export function isPublicRoute(path: string): boolean {
  return publicRoutes.includes(path)
}
