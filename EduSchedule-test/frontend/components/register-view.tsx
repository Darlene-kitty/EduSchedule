"use client"

import type React from "react"
import { useState, useEffect } from "react"
import { Calendar, Eye, EyeOff, User, Mail, Phone, Shield, Lock, ArrowRight, Sparkles, GraduationCap, BookOpen, Users } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { useAuth } from "@/contexts/auth-context"
import { useToast } from "@/hooks/use-toast"
import type { UserRole } from "@/contexts/auth-context"

export function RegisterView() {
  const router = useRouter()
  const { register, isLoading } = useAuth()
  const { toast } = useToast()
  const [mounted, setMounted] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    phone: "",
    role: "" as UserRole | "",
    password: "",
    confirmPassword: "",
  })

  useEffect(() => {
    setMounted(true)
  }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!formData.name || !formData.email || !formData.password || !formData.role) {
      toast({
        title: "Erreur",
        description: "Veuillez remplir tous les champs obligatoires",
        variant: "destructive",
      })
      return
    }

    if (formData.password !== formData.confirmPassword) {
      toast({
        title: "Erreur",
        description: "Les mots de passe ne correspondent pas",
        variant: "destructive",
      })
      return
    }

    if (formData.password.length < 8) {
      toast({
        title: "Erreur",
        description: "Le mot de passe doit contenir au moins 8 caractères",
        variant: "destructive",
      })
      return
    }

    try {
      await register(formData.email, formData.password, formData.name, formData.role as UserRole)
      toast({
        title: "Inscription réussie",
        description: "Bienvenue dans EduSchedule ! Redirection vers le dashboard...",
        variant: "default",
      })
      router.push("/")
    } catch (error) {
      toast({
        title: "Erreur d'inscription",
        description: "Une erreur est survenue lors de l'inscription",
        variant: "destructive",
      })
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
              Rejoignez notre
              <span className="text-[#15803D] block">communauté éducative</span>
            </h2>
            
            <p className="text-xl text-gray-600 mb-12 leading-relaxed">
              Créez votre compte pour accéder à tous les outils de gestion 
              académique et optimiser votre expérience éducative.
            </p>

            <div className="space-y-6">
              <div className="flex items-center space-x-4">
                <div className="flex items-center justify-center w-12 h-12 bg-green-100 rounded-xl">
                  <GraduationCap className="w-6 h-6 text-[#15803D]" />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">Accès personnalisé</h3>
                  <p className="text-gray-600">Interface adaptée à votre rôle et besoins</p>
                </div>
              </div>
              
              <div className="flex items-center space-x-4">
                <div className="flex items-center justify-center w-12 h-12 bg-blue-100 rounded-xl">
                  <BookOpen className="w-6 h-6 text-blue-600" />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">Outils intégrés</h3>
                  <p className="text-gray-600">Tout ce dont vous avez besoin en un seul endroit</p>
                </div>
              </div>
              
              <div className="flex items-center space-x-4">
                <div className="flex items-center justify-center w-12 h-12 bg-purple-100 rounded-xl">
                  <Users className="w-6 h-6 text-purple-600" />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">Support dédié</h3>
                  <p className="text-gray-600">Assistance technique et pédagogique</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right side - Registration form */}
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
                  <h2 className="text-2xl lg:text-3xl font-bold text-gray-900">Inscription</h2>
                </div>
                <p className="text-gray-600">Créez votre compte pour commencer</p>
              </div>

              <form onSubmit={handleSubmit} className="space-y-5">
                <div className="space-y-2">
                  <Label htmlFor="name" className="text-gray-900 font-medium flex items-center">
                    <User className="w-4 h-4 mr-2" />
                    Nom complet
                  </Label>
                  <Input
                    id="name"
                    type="text"
                    placeholder="Entrez votre nom complet"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="h-12 pl-4 pr-4 bg-gray-50/50 border-gray-200 focus:bg-white focus:border-[#15803D] focus:ring-[#15803D]/20 transition-all duration-200"
                    required
                    disabled={isLoading}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="email" className="text-gray-900 font-medium flex items-center">
                    <Mail className="w-4 h-4 mr-2" />
                    Adresse email
                  </Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="nom@exemple.com"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    className="h-12 pl-4 pr-4 bg-gray-50/50 border-gray-200 focus:bg-white focus:border-[#15803D] focus:ring-[#15803D]/20 transition-all duration-200"
                    required
                    disabled={isLoading}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="phone" className="text-gray-900 font-medium flex items-center">
                    <Phone className="w-4 h-4 mr-2" />
                    Téléphone <span className="text-gray-500 text-sm ml-1">(optionnel)</span>
                  </Label>
                  <Input
                    id="phone"
                    type="tel"
                    placeholder="+237 6XX XXX XXX"
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    className="h-12 pl-4 pr-4 bg-gray-50/50 border-gray-200 focus:bg-white focus:border-[#15803D] focus:ring-[#15803D]/20 transition-all duration-200"
                    disabled={isLoading}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="role" className="text-gray-900 font-medium flex items-center">
                    <Shield className="w-4 h-4 mr-2" />
                    Rôle
                  </Label>
                  <Select
                    value={formData.role}
                    onValueChange={(value) => setFormData({ ...formData, role: value as UserRole })}
                    disabled={isLoading}
                  >
                    <SelectTrigger className="h-12 bg-gray-50/50 border-gray-200 focus:bg-white focus:border-[#15803D] focus:ring-[#15803D]/20 transition-all duration-200">
                      <SelectValue placeholder="Sélectionnez votre rôle" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="admin">
                        <div className="flex items-center">
                          <div className="w-2 h-2 bg-red-500 rounded-full mr-2"></div>
                          Administrateur
                        </div>
                      </SelectItem>
                      <SelectItem value="teacher">
                        <div className="flex items-center">
                          <div className="w-2 h-2 bg-blue-500 rounded-full mr-2"></div>
                          Enseignant
                        </div>
                      </SelectItem>
                      <SelectItem value="student">
                        <div className="flex items-center">
                          <div className="w-2 h-2 bg-green-500 rounded-full mr-2"></div>
                          Étudiant
                        </div>
                      </SelectItem>
                    </SelectContent>
                  </Select>
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
                      placeholder="Créez un mot de passe sécurisé"
                      value={formData.password}
                      onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                      className="h-12 pl-4 pr-12 bg-gray-50/50 border-gray-200 focus:bg-white focus:border-[#15803D] focus:ring-[#15803D]/20 transition-all duration-200"
                      required
                      disabled={isLoading}
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-3 text-gray-500 hover:text-[#15803D] transition-colors duration-200"
                      disabled={isLoading}
                    >
                      {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                    </button>
                  </div>
                  <p className="text-xs text-gray-500">Minimum 8 caractères</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="confirmPassword" className="text-gray-900 font-medium flex items-center">
                    <Lock className="w-4 h-4 mr-2" />
                    Confirmer le mot de passe
                  </Label>
                  <div className="relative">
                    <Input
                      id="confirmPassword"
                      type={showConfirmPassword ? "text" : "password"}
                      placeholder="Confirmez votre mot de passe"
                      value={formData.confirmPassword}
                      onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                      className="h-12 pl-4 pr-12 bg-gray-50/50 border-gray-200 focus:bg-white focus:border-[#15803D] focus:ring-[#15803D]/20 transition-all duration-200"
                      required
                      disabled={isLoading}
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      className="absolute right-3 top-3 text-gray-500 hover:text-[#15803D] transition-colors duration-200"
                      disabled={isLoading}
                    >
                      {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                    </button>
                  </div>
                </div>

                <Button
                  type="submit"
                  className="w-full h-12 bg-gradient-to-r from-[#15803D] to-[#22C55E] hover:from-[#166534] hover:to-[#16A34A] text-white font-medium shadow-lg hover:shadow-xl transition-all duration-200 group"
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <div className="flex items-center">
                      <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></div>
                      Inscription en cours...
                    </div>
                  ) : (
                    <div className="flex items-center justify-center">
                      Créer mon compte
                      <ArrowRight className="w-5 h-5 ml-2 group-hover:translate-x-1 transition-transform duration-200" />
                    </div>
                  )}
                </Button>
              </form>

              <div className="mt-8 pt-6 border-t border-gray-200">
                <p className="text-center text-gray-600">
                  Vous avez déjà un compte ?{" "}
                  <Link
                    href="/login"
                    className="text-[#15803D] hover:text-[#166534] font-medium transition-colors duration-200"
                  >
                    Se connecter
                  </Link>
                </p>
              </div>

              {/* Terms and conditions */}
              <div className="mt-6 p-4 bg-gradient-to-r from-green-50 to-blue-50 rounded-2xl border border-green-100">
                <p className="text-xs text-gray-600 text-center">
                  En créant un compte, vous acceptez nos{" "}
                  <Link href="/terms" className="text-[#15803D] hover:underline">
                    conditions d'utilisation
                  </Link>{" "}
                  et notre{" "}
                  <Link href="/privacy" className="text-[#15803D] hover:underline">
                    politique de confidentialité
                  </Link>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}