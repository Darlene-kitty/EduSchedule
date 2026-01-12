"use client"

import type React from "react"
import { createContext, useContext, useState, useEffect, useCallback } from "react"
import { saveSession, getSession, clearSession } from "@/lib/session"
import { authService } from "@/services/auth.service"
import { ApiError } from "@/lib/api-client"

export type UserRole = "admin" | "teacher" | "student"

export interface User {
  id: string
  email: string
  name: string
  role: UserRole
  avatar?: string
  createdAt?: Date
}

export interface AuthContextType {
  user: User | null
  isLoading: boolean
  isAuthenticated: boolean
  login: (email: string, password: string, rememberMe?: boolean) => Promise<void>
  logout: () => Promise<void>
  register: (email: string, password: string, name: string, role: UserRole) => Promise<void>
  resetPassword: (email: string) => Promise<void>
  verifyEmail: (code: string) => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const initializeAuth = async () => {
      try {
        const savedSession = getSession()
        if (savedSession) {
          setUser(savedSession)
        }
      } catch (error) {
        console.error("Failed to initialize auth:", error)
      } finally {
        setIsLoading(false)
      }
    }

    initializeAuth()
  }, [])

  const login = useCallback(async (email: string, password: string, rememberMe = false) => {
    setIsLoading(true)
    try {
      // Le backend attend 'username', on peut utiliser l'email comme username
      const user = await authService.login({ username: email, password })
      setUser(user)
      saveSession(user, rememberMe)
      
      if (rememberMe) {
        localStorage.setItem("savedEmail", email)
      }
    } catch (error) {
      if (error instanceof ApiError) {
        throw error
      }
      throw new Error("Échec de la connexion. Veuillez réessayer.")
    } finally {
      setIsLoading(false)
    }
  }, [])

  const logout = useCallback(async () => {
    setIsLoading(true)
    try {
      await authService.logout()
      setUser(null)
      clearSession()
      localStorage.removeItem("savedEmail")
    } catch (error) {
      // Toujours déconnecter localement même si l'appel API échoue
      setUser(null)
      clearSession()
      localStorage.removeItem("savedEmail")
    } finally {
      setIsLoading(false)
    }
  }, [])

  const register = useCallback(async (email: string, password: string, name: string, role: UserRole) => {
    setIsLoading(true)
    try {
      // Le backend utilise 'username' et n'accepte que lettres, chiffres, _ et -
      const username = name.replace(/\s+/g, '_').toLowerCase().replace(/[^a-zA-Z0-9_-]/g, '')
      
      // Mapper le rôle frontend vers le format backend
      const roleMap: Record<UserRole, string> = {
        'admin': 'ADMIN',
        'teacher': 'TEACHER', 
        'student': 'STUDENT'
      }
      
      const user = await authService.register({
        username,
        email,
        password,
        role: roleMap[role],
      })
      
      setUser(user)
      saveSession(user, false)
    } catch (error) {
      if (error instanceof ApiError) {
        throw error
      }
      throw new Error("Échec de l'inscription. Veuillez réessayer.")
    } finally {
      setIsLoading(false)
    }
  }, [])

  const resetPassword = useCallback(async (email: string) => {
    setIsLoading(true)
    try {
      await authService.forgotPassword(email)
    } catch (error) {
      if (error instanceof ApiError) {
        throw error
      }
      throw new Error("Échec de la réinitialisation. Veuillez réessayer.")
    } finally {
      setIsLoading(false)
    }
  }, [])

  const verifyEmail = useCallback(async (code: string) => {
    setIsLoading(true)
    try {
      await authService.verifyEmail(code)
    } catch (error) {
      if (error instanceof ApiError) {
        throw error
      }
      throw new Error("Échec de la vérification. Veuillez réessayer.")
    } finally {
      setIsLoading(false)
    }
  }, [])

  const value: AuthContextType = {
    user,
    isLoading,
    isAuthenticated: !!user,
    login,
    logout,
    register,
    resetPassword,
    verifyEmail,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}
