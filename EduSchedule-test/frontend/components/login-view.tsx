"use client"

import type React from "react"
import { useState, useEffect } from "react"
import { Calendar, Eye, EyeOff, User, Lock, ArrowRight, Sparkles, GraduationCap, BookOpen, Users } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { useAuth } from "@/contexts/auth-context"
import { useToast } from "@/hooks/use-toast"

export function LoginView() {
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [showPassword, setShowPassword] = useState(false)
  const [rememberMe, setRememberMe] = useState(false)
  const [loading, setLoading] = useState(false)
  const [mounted, setMounted] = useState(false)
  
  const { login } = useAuth()
  const router = useRouter()
  const { toast } = useToast()

  useEffect(() => {
    setMounted(true)
    
    // Charger l'email sauvegardé si "Remember Me" était activé
    const savedEmail = localStorage.getItem("savedEmail")
    if (savedEmail) {
      setUsername(savedEmail)
      setRememberMe(true)
    }
  }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!username.trim() || !password.trim()) {
      toast({
        title: "Erreur",
        description: "Veuillez remplir tous les champs",
        variant: "destructive",
      })
      return
    }

    setLoading(true)

    try {
      await login(username, password, rememberMe)
      toast({
        title: "Connexion réussie",
        description: "Bienvenue dans EduSchedule",
        variant: "default",
      })
      router.push('/')
    } catch (error: any) {
      console.error('Login error:', error)
      toast({
        title: "Erreur de connexion",
        description: error.message || "Nom d'utilisateur ou mot de passe incorrect",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  if (!mounted) {
    return null
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-50 via-white to-blue-50 relative overflow-hidden">
      {/* Animated background elements */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-gradient-to-br from-green-200/30 to-blue-200/30 rounded-full animate-blob"></div>
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-gradient-to-br from-yellow-200/30 to-green-200/30 rounded-full animate-blob animation-delay-2000"></div>
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-60 h-60 bg-gradient-to-br from-blue-200/20 to-purple-200/20 rounded-full animate-blob animation-delay-4000"></div>
      </div>

      <div className="relative z-10 min-h-screen flex">
        {/* Left side - Branding and features */}
        <div className="hidden lg:flex lg:w-1/2 flex-col justify-center px-12 xl:px-16">
          <div className="animate-fade-in-down">
            <div className="flex items-center mb-8">
              <div className="flex items-center justify-center w-16 h-16 bg-gradient-to-br from-[#15803D] to-[#22C55E] rounded-2xl shadow-lg mr-4">
                <Calendar className="w-8 h-8 text-white" />
              </div>
              <div>
                <h1 className="text-3xl font-bold text-gray-900">EduSchedule</h1>
                <p className="text-lg text-gray-600">IUSJC</p>
              </div>
            </div>
            
            <h2 className="text-4xl xl:text-5xl font-bold text-gray-900 mb-6 leading-tight">
              Gérez vos emplois du temps
              <span className="text-[#15803D] block">en toute simplicité</span>
            </h2>
            
            <p className="text-xl text-gray-600 mb-12 leading-relaxed">
              Plateforme moderne de gestion académique pour optimiser l'organisation 
              des cours, salles et ressources éducatives.
            </p>

            <div className="space-y-6">
              <div className="flex items-center space-x-4">
                <div className="flex items-center justify-center w-12 h-12 bg-green-100 rounded-xl">
                  <GraduationCap className="w-6 h-6 text-[#15803D]" />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">Gestion des cours</h3>
                  <p className="text-gray-600">Planifiez et organisez facilement vos cours</p>
                </div>
              </div>
              
              <div className="flex items-center space-x-4">
                <div className="flex items-center justify-center w-12 h-12 bg-blue-100 rounded-xl">
                  <BookOpen className="w-6 h-6 text-blue-600" />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">Réservation de salles</h3>
                  <p className="text-gray-600">Réservez vos espaces d'apprentissage en temps réel</p>
                </div>
              </div>
              
              <div className="flex items-center space-x-4">
                <div className="flex items-center justify-center w-12 h-12 bg-purple-100 rounded-xl">
                  <Users className="w-6 h-6 text-purple-600" />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">Collaboration</h3>
                  <p className="text-gray-600">Travaillez en équipe avec enseignants et étudiants</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right side - Login form */}
        <div className="w-full lg:w-1/2 flex items-center justify-center p-4 lg:p-8">
          <div className="w-full max-w-md animate-fade-in-up">
            {/* Mobile header */}
            <div className="lg:hidden text-center mb-8">
              <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-[#15803D] to-[#22C55E] rounded-3xl shadow-lg mb-6">
                <Calendar className="w-10 h-10 text-white" />
              </div>
              <h1 className="text-2xl font-bold text-gray-900 mb-2">EduSchedule - IUSJC</h1>
              <p className="text-gray-600">Système de Gestion des Emplois du Temps</p>
            </div>

            <div className="bg-white/80 backdrop-blur-sm rounded-3xl shadow-2xl border border-white/20 p-8 lg:p-10">
              <div className="mb-8">
                <div className="flex items-center mb-4">
                  <Sparkles className="w-6 h-6 text-[#15803D] mr-2" />
                  <h2 className="text-2xl lg:text-3xl font-bold text-gray-900">Connexion</h2>
                </div>
                <p className="text-gray-600">Accédez à votre espace de gestion personnalisé</p>
              </div>

              <form onSubmit={handleSubmit} className="space-y-6">
                <div className="space-y-2">
                  <Label htmlFor="username" className="text-gray-900 font-medium flex items-center">
                    <User className="w-4 h-4 mr-2" />
                    Nom d'utilisateur
                  </Label>
                  <div className="relative">
                    <Input
                      id="username"
                      type="text"
                      placeholder="Entrez votre nom d'utilisateur"
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      className="h-12 pl-4 pr-4 bg-gray-50/50 border-gray-200 focus:bg-white focus:border-[#15803D] focus:ring-[#15803D]/20 transition-all duration-200"
                      required
                      disabled={loading}
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="password" className="text-gray-900 font-medium flex items-center">
                    <Lock className="w-4 h-4 mr-2" />
                    Mot de passe
                  </Label>
                  <div className="relative">
                    <Input
                      id="password"
                      type={showPassword ? "text" : "password"}
                      placeholder="Entrez votre mot de passe"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="h-12 pl-4 pr-12 bg-gray-50/50 border-gray-200 focus:bg-white focus:border-[#15803D] focus:ring-[#15803D]/20 transition-all duration-200"
                      required
                      disabled={loading}
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-3 text-gray-500 hover:text-[#15803D] transition-colors duration-200"
                      disabled={loading}
                    >
                      {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                    </button>
                  </div>
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center">
                    <input
                      id="remember-me"
                      name="remember-me"
                      type="checkbox"
                      checked={rememberMe}
                      onChange={(e) => setRememberMe(e.target.checked)}
                      className="h-4 w-4 text-[#15803D] focus:ring-[#15803D] border-gray-300 rounded transition-colors duration-200"
                      disabled={loading}
                    />
                    <label htmlFor="remember-me" className="ml-3 block text-sm text-gray-700">
                      Se souvenir de moi
                    </label>
                  </div>

                  <Link
                    href="/forgot-password"
                    className="text-sm text-[#15803D] hover:text-[#166534] font-medium transition-colors duration-200"
                  >
                    Mot de passe oublié ?
                  </Link>
                </div>

                <Button 
                  type="submit" 
                  className="w-full h-12 bg-gradient-to-r from-[#15803D] to-[#22C55E] hover:from-[#166534] hover:to-[#16A34A] text-white font-medium shadow-lg hover:shadow-xl transition-all duration-200 group"
                  disabled={loading}
                >
                  {loading ? (
                    <div className="flex items-center">
                      <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></div>
                      Connexion en cours...
                    </div>
                  ) : (
                    <div className="flex items-center justify-center">
                      Se connecter
                      <ArrowRight className="w-5 h-5 ml-2 group-hover:translate-x-1 transition-transform duration-200" />
                    </div>
                  )}
                </Button>
              </form>

              <div className="mt-8 pt-6 border-t border-gray-200">
                <p className="text-center text-gray-600">
                  Pas encore de compte ?{" "}
                  <Link
                    href="/register"
                    className="text-[#15803D] hover:text-[#166534] font-medium transition-colors duration-200"
                  >
                    Créer un compte
                  </Link>
                </p>
              </div>

              {/* Quick access info */}
              <div className="mt-6 p-4 bg-gradient-to-r from-green-50 to-blue-50 rounded-2xl border border-green-100">
                <p className="text-sm text-gray-700 text-center">
                  <span className="font-medium">Accès rapide :</span> Utilisez vos identifiants IUSJC pour vous connecter
                </p>
                {rememberMe && (
                  <p className="text-xs text-gray-600 text-center mt-2">
                    ✓ Vos identifiants seront sauvegardés pendant 30 jours
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}