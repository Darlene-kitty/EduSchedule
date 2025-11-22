"use client"

import type React from "react"
import { useState } from "react"
import { Calendar } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { useAuth } from "@/contexts/auth-context"
import { useToast } from "@/contexts/toast-context"
import type { UserRole } from "@/contexts/auth-context"

export function RegisterView() {
  const router = useRouter()
  const { register, isLoading } = useAuth()
  const { addToast } = useToast()
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    phone: "",
    role: "" as UserRole | "",
    password: "",
    confirmPassword: "",
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!formData.name || !formData.email || !formData.password || !formData.role) {
      addToast("Veuillez remplir tous les champs obligatoires", "error")
      return
    }

    if (formData.password !== formData.confirmPassword) {
      addToast("Les mots de passe ne correspondent pas", "error")
      return
    }

    if (formData.password.length < 8) {
      addToast("Le mot de passe doit contenir au moins 8 caractères", "error")
      return
    }

    try {
      await register(formData.email, formData.password, formData.name, formData.role as UserRole)
      addToast("Inscription réussie! Redirection vers le dashboard...", "success")
      router.push("/")
    } catch (error) {
      addToast("Erreur lors de l'inscription", "error")
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
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Inscription</h2>
            <p className="text-gray-600">Créez votre compte de gestion</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <Label htmlFor="name" className="text-gray-900 font-medium">
                Nom complet
              </Label>
              <Input
                id="name"
                type="text"
                placeholder="Jean Dupont"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="mt-1.5 h-11"
                required
                disabled={isLoading}
              />
            </div>

            <div>
              <Label htmlFor="email" className="text-gray-900 font-medium">
                Email
              </Label>
              <Input
                id="email"
                type="email"
                placeholder="nom@exemple.com"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="mt-1.5 h-11"
                required
                disabled={isLoading}
              />
            </div>

            <div>
              <Label htmlFor="phone" className="text-gray-900 font-medium">
                Téléphone (optionnel)
              </Label>
              <Input
                id="phone"
                type="tel"
                placeholder="+33 6 12 34 56 78"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                className="mt-1.5 h-11"
                disabled={isLoading}
              />
            </div>

            <div>
              <Label htmlFor="role" className="text-gray-900 font-medium">
                Rôle
              </Label>
              <Select
                value={formData.role}
                onValueChange={(value) => setFormData({ ...formData, role: value as UserRole })}
                disabled={isLoading}
              >
                <SelectTrigger className="mt-1.5 h-11">
                  <SelectValue placeholder="Sélectionnez un rôle" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="admin">Administrateur</SelectItem>
                  <SelectItem value="teacher">Enseignant</SelectItem>
                  <SelectItem value="student">Étudiant</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div>
              <Label htmlFor="password" className="text-gray-900 font-medium">
                Mot de passe
              </Label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                className="mt-1.5 h-11"
                required
                disabled={isLoading}
              />
              <p className="text-xs text-gray-500 mt-1">Minimum 8 caractères</p>
            </div>

            <div>
              <Label htmlFor="confirmPassword" className="text-gray-900 font-medium">
                Confirmer le mot de passe
              </Label>
              <Input
                id="confirmPassword"
                type="password"
                placeholder="••••••••"
                value={formData.confirmPassword}
                onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                className="mt-1.5 h-11"
                required
                disabled={isLoading}
              />
            </div>

            <Button
              type="submit"
              className="w-full h-11 bg-[#15803D] hover:bg-[#166534] text-white font-medium disabled:opacity-50"
              disabled={isLoading}
            >
              {isLoading ? "Inscription en cours..." : "S'inscrire"}
            </Button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              Vous avez déjà un compte ?{" "}
              <Link href="/login" className="text-[#15803D] hover:text-[#166534] font-medium">
                Se connecter
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
