"use client"

import { useAuth } from "@/contexts/auth-context"
import { useRouter } from "next/navigation"
import { useEffect, type ReactNode } from "react"

interface AuthGuardProps {
  children: ReactNode
  requiredRoles?: string[]
  redirectTo?: string
}

export function AuthGuard({ children, requiredRoles = [], redirectTo = "/login" }: AuthGuardProps) {
  const { user, isAuthenticated, isLoading } = useAuth()
  const router = useRouter()

  useEffect(() => {
    if (isLoading) return

    if (!isAuthenticated) {
      router.push(redirectTo)
      return
    }

    if (requiredRoles.length > 0 && !requiredRoles.includes(user?.role || "")) {
      router.push("/unauthorized")
      return
    }
  }, [isAuthenticated, user, isLoading, requiredRoles, router, redirectTo])

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin">
          <div className="w-8 h-8 border-4 border-green-200 border-t-green-500 rounded-full"></div>
        </div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return null
  }

  if (requiredRoles.length > 0 && !requiredRoles.includes(user?.role || "")) {
    return null
  }

  return <>{children}</>
}
