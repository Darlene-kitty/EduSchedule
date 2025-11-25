"use client"

import type React from "react"
import { useState } from "react"
import { Calendar, ArrowLeft, Eye, EyeOff } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import Link from "next/link"

export function ResetPasswordView() {
  const [newPassword, setNewPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [success, setSuccess] = useState(false)
  const [passwordStrength, setPasswordStrength] = useState(0)

  const calculatePasswordStrength = (password: string) => {
    let strength = 0
    if (password.length >= 8) strength++
    if (/[A-Z]/.test(password)) strength++
    if (/[0-9]/.test(password)) strength++
    if (/[^A-Za-z0-9]/.test(password)) strength++
    setPasswordStrength(strength)
  }

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const password = e.target.value
    setNewPassword(password)
    calculatePasswordStrength(password)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (newPassword !== confirmPassword) {
      alert("Les mots de passe ne correspondent pas")
      return
    }
    if (passwordStrength < 2) {
      alert("Le mot de passe n'est pas assez fort")
      return
    }
    setSuccess(true)
  }

  const getStrengthColor = (strength: number) => {
    if (strength === 0) return "bg-gray-200"
    if (strength === 1) return "bg-red-500"
    if (strength === 2) return "bg-yellow-500"
    if (strength === 3) return "bg-blue-500"
    return "bg-green-500"
  }

  const getStrengthText = (strength: number) => {
    if (strength === 0) return "Très faible"
    if (strength === 1) return "Faible"
    if (strength === 2) return "Moyen"
    if (strength === 3) return "Fort"
    return "Très fort"
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
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Réinitialiser le mot de passe</h2>
            <p className="text-gray-600">Créez un nouveau mot de passe sécurisé</p>
          </div>

          {!success ? (
            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <Label htmlFor="newPassword" className="text-gray-900 font-medium">
                  Nouveau mot de passe
                </Label>
                <div className="relative mt-2">
                  <Input
                    id="newPassword"
                    type={showPassword ? "text" : "password"}
                    placeholder="••••••••"
                    value={newPassword}
                    onChange={handlePasswordChange}
                    className="h-12 pr-10"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-3 text-gray-500 hover:text-gray-700"
                  >
                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>

                {/* Password strength indicator */}
                <div className="mt-3">
                  <div className="flex gap-1 mb-1">
                    {[0, 1, 2, 3].map((i) => (
                      <div
                        key={i}
                        className={`flex-1 h-2 rounded-full ${i < passwordStrength ? getStrengthColor(passwordStrength) : "bg-gray-200"}`}
                      />
                    ))}
                  </div>
                  <p className="text-xs text-gray-600">
                    Force: <span className="font-semibold">{getStrengthText(passwordStrength)}</span>
                  </p>
                </div>

                <ul className="mt-3 text-xs text-gray-600 space-y-1">
                  <li className={newPassword.length >= 8 ? "text-green-600" : ""}>
                    {newPassword.length >= 8 ? "✓" : "○"} Au moins 8 caractères
                  </li>
                  <li className={/[A-Z]/.test(newPassword) ? "text-green-600" : ""}>
                    {/[A-Z]/.test(newPassword) ? "✓" : "○"} Une lettre majuscule
                  </li>
                  <li className={/[0-9]/.test(newPassword) ? "text-green-600" : ""}>
                    {/[0-9]/.test(newPassword) ? "✓" : "○"} Un chiffre
                  </li>
                  <li className={/[^A-Za-z0-9]/.test(newPassword) ? "text-green-600" : ""}>
                    {/[^A-Za-z0-9]/.test(newPassword) ? "✓" : "○"} Un caractère spécial
                  </li>
                </ul>
              </div>

              <div>
                <Label htmlFor="confirmPassword" className="text-gray-900 font-medium">
                  Confirmer le mot de passe
                </Label>
                <div className="relative mt-2">
                  <Input
                    id="confirmPassword"
                    type={showConfirm ? "text" : "password"}
                    placeholder="••••••••"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className="h-12 pr-10"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirm(!showConfirm)}
                    className="absolute right-3 top-3 text-gray-500 hover:text-gray-700"
                  >
                    {showConfirm ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
              </div>

              <Button type="submit" className="w-full h-12 bg-[#15803D] hover:bg-[#166534] text-white font-medium">
                Réinitialiser le mot de passe
              </Button>
            </form>
          ) : (
            <div className="space-y-4 text-center">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto">
                <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">Mot de passe réinitialisé!</h3>
                <p className="text-gray-600 mb-6">Votre mot de passe a été réinitialisé avec succès.</p>
              </div>

              <Link href="/login">
                <Button className="w-full h-12 bg-[#15803D] hover:bg-[#166534] text-white font-medium">
                  Se connecter
                </Button>
              </Link>
            </div>
          )}

          <div className="mt-6 pt-6 border-t border-gray-200">
            <Link
              href="/login"
              className="flex items-center gap-2 text-[#15803D] hover:text-[#166534] font-medium justify-center"
            >
              <ArrowLeft className="w-4 h-4" />
              Retour à la connexion
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
