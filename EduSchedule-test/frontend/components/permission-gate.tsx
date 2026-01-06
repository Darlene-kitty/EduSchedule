"use client"

import { useAuth } from "@/contexts/auth-context"
import type { UserRole } from "@/contexts/auth-context"
import type { ReactNode } from "react"

interface PermissionGateProps {
  children: ReactNode
  requiredRoles: UserRole[]
  fallback?: ReactNode
}

export function PermissionGate({ children, requiredRoles, fallback = null }: PermissionGateProps) {
  const { user } = useAuth()

  if (!user || !requiredRoles.includes(user.role)) {
    return fallback
  }

  return <>{children}</>
}
