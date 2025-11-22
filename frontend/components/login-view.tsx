"use client"

import type React from "react"

import type { ReactElement } from "react"
import { useState } from "react"
import { Calendar, Eye, EyeOff } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { useAuth } from "@/contexts/auth-context"
import { useToast } from "@/contexts/toast-context"

export function LoginView(): ReactElement {
  const router = useRouter()
  const { login, isLoading } = useAuth()
  const { addToast } = useToast()
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [rememberMe, setRememberMe] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  useState(() => {
    const savedEmail = localStorage.getItem("savedEmail")
    const rememberMeFlag = localStorage.getItem("rememberMe")
    if (savedEmail && rememberMeFlag === "true") {
      setEmail(savedEmail)
      setRememberMe(true)
    }
  }, [])

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!email || !password) {
      addToast("Veuillez remplir tous les champs", "error")
      return
    }

    try {
      await login(email, password, rememberMe)
      addToast(`Bienvenue ${email}!`, "success")
      router.push("/")
    } catch (error) {
      addToast("Erreur lors de la connexion", "error")
    }
  }

  const handleQuickLogin = async (role: string) => {
    const quickEmails = {
      admin: "admin@eduscheduler.com",
      teacher: "teacher@eduscheduler.com",
    }

    try {
      await login(quickEmails[role as keyof typeof quickEmails], "password123", false)
      addToast(`Connecté en tant que ${role}`, "success")
      router.push("/")
    } catch (error) {
      addToast("Erreur lors de la connexion rapide", "error")
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-24 h-24 bg-[#FBBF24] rounded-3xl shadow-lg mb-6">
            <Calendar className="w-12 h-12 text-[#1F2937]" />
          </div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Système de Gestion</h1>
          <p className="text-gray-600">Salles & Emplois du Temps</p>
        </div>

        <div className="bg-white rounded-2xl shadow-xl p-8">
          <div className="mb-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Connexion</h2>
            <p className="text-gray-600">Accédez à votre espace de gestion</p>
          </div>

          <form onSubmit={handleLogin} className="space-y-5">
            <div>
              <Label htmlFor="email" className="text-gray-900 font-medium">
                Email
              </Label>
              <Input
                id="email"
                type="email"
                placeholder="nom@exemple.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="mt-1.5 h-12"
                required
                disabled={isLoading}
              />
            </div>

            <div>
              <Label htmlFor="password" className="text-gray-900 font-medium">
                Mot de passe
              </Label>
              <div className="relative mt-1.5">
                <Input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="h-12 pr-10"
                  required
                  disabled={isLoading}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-3 text-gray-500 hover:text-gray-700"
                  disabled={isLoading}
                >
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            <div className="flex items-center">
              <input
                id="rememberMe"
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                className="w-4 h-4 text-[#15803D] rounded border-gray-300 focus:ring-[#15803D]"
                disabled={isLoading}
              />
              <label htmlFor="rememberMe" className="ml-2 text-sm text-gray-700">
                Se souvenir de moi
              </label>
            </div>

            <Button
              type="submit"
              className="w-full h-12 bg-[#15803D] hover:bg-[#166534] text-white font-medium disabled:opacity-50"
              disabled={isLoading}
            >
              {isLoading ? "Connexion en cours..." : "Se connecter"}
            </Button>
          </form>

          <div className="mt-4 flex gap-2 text-sm">
            <Link href="/forgot-password" className="text-[#15803D] hover:text-[#166534] font-medium">
              Mot de passe oublié?
            </Link>
            <span className="text-gray-400">•</span>
            <Link href="/verify-email" className="text-[#15803D] hover:text-[#166534] font-medium">
              Vérifier email
            </Link>
          </div>

          <div className="mt-6 pt-6 border-t border-gray-200">
            <p className="text-sm text-gray-600 mb-3">Connexion rapide (démo):</p>
            <div className="space-y-2">
              <button
                type="button"
                onClick={() => handleQuickLogin("admin")}
                className="w-full flex items-center gap-3 px-4 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50"
                disabled={isLoading}
              >
                <span className="w-5 h-5 text-gray-600">👤</span>
                <span className="text-gray-900 font-medium">Administrateur</span>
              </button>
              <button
                type="button"
                onClick={() => handleQuickLogin("teacher")}
                className="w-full flex items-center gap-3 px-4 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50"
                disabled={isLoading}
              >
                <span className="w-5 h-5 text-gray-600">🕐</span>
                <span className="text-gray-900 font-medium">Enseignant</span>
              </button>
            </div>
          </div>

          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              Pas encore de compte ?{" "}
              <Link href="/register" className="text-[#15803D] hover:text-[#166534] font-medium">
                S'inscrire
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
