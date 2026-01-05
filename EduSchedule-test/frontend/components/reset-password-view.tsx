"use client"

import type React from "react"
import { useState, useEffect } from "react"
import { Calendar, ArrowLeft, Eye, EyeOff } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import Link from "next/link"
import { useSearchParams, useRouter } from "next/navigation"
import { useToast } from "@/hooks/use-toast"

export function ResetPasswordView() {
  const [newPassword, setNewPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)
  const [validatingToken, setValidatingToken] = useState(true)
  const [tokenValid, setTokenValid] = useState(false)
  const [passwordStrength, setPasswordStrength] = useState(0)
  
  const searchParams = useSearchParams()
  const router = useRouter()
  const { toast } = useToast()
  const token = searchParams.get('token')

  useEffect(() => {
    if (!token) {
      toast({
        title: "Token manquant",
        description: "Le lien de réinitialisation est invalide",
        variant: "destructive",
      })
      router.push('/forgot-password')
      return
    }

    validateToken()
  }, [token])

  const validateToken = async () => {
    if (!token) return

    try {
      setValidatingToken(true)
      const response = await fetch(`/api/auth/reset-password/validate?token=${token}`)
      const data = await response.json()
      
      if (response.ok && data.success) {
        setTokenValid(true)
      } else {
        setTokenValid(false)
        toast({
          title: "Token invalide",
          description: data.message || "Le lien de réinitialisation a expiré ou est invalide",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error('Token validation error:', error)
      setTokenValid(false)
      toast({
        title: "Erreur",
        description: "Impossible de valider le token",
        variant: "destructive",
      })
    } finally {
      setValidatingToken(false)
    }
  }

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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (newPassword !== confirmPassword) {
      toast({
        title: "Erreur",
        description: "Les mots de passe ne correspondent pas",
        variant: "destructive",
      })
      return
    }
    
    if (passwordStrength < 2) {
      toast({
        title: "Mot de passe trop faible",
        description: "Le mot de passe doit contenir au moins 8 caractères avec une majuscule et un chiffre",
        variant: "destructive",
      })
      return
    }

    if (!token) {
      toast({
        title: "Erreur",
        description: "Token manquant",
        variant: "destructive",
      })
      return
    }

    setLoading(true)

    try {
      const response = await fetch('/api/auth/reset-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          token,
          newPassword,
          confirmPassword,
        }),
      })

      const data = await response.json()

      if (response.ok && data.success) {
        setSuccess(true)
        toast({
          title: "Succès",
          description: "Votre mot de passe a été réinitialisé avec succès",
          variant: "default",
        })
      } else {
        toast({
          title: "Erreur",
          description: data.message || "Une erreur est survenue lors de la réinitialisation",
          variant: "destructive",
        })
      }
    } catch (error: any) {
      console.error('Reset password error:', error)
      toast({
        title: "Erreur",
        description: error.message || "Une erreur est survenue lors de la réinitialisation",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
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

  if (validatingToken) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 flex items-center justify-center p-4">
        <div className="w-full max-w-md">
          <div className="bg-white rounded-2xl shadow-xl p-8 text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#15803D] mx-auto mb-4"></div>
            <p className="text-gray-600">Validation du lien de réinitialisation...</p>
          </div>
        </div>
      </div>
    )
  }

  if (!tokenValid) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 flex items-center justify-center p-4">
        <div className="w-full max-w-md">
          <div className="bg-white rounded-2xl shadow-xl p-8 text-center">
            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Lien invalide</h3>
            <p className="text-gray-600 mb-6">
              Ce lien de réinitialisation a expiré ou est invalide.
            </p>
            <Link href="/forgot-password">
              <Button className="w-full h-12 bg-[#15803D] hover:bg-[#166534] text-white font-medium">
                Demander un nouveau lien
              </Button>
            </Link>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-24 h-24 bg-[#FBBF24] rounded-3xl shadow-lg mb-6">
            <Calendar className="w-12 h-12 text-[#1F2937]" />
          </div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">EduSchedule - IUSJC</h1>
          <p className="text-gray-600">Système de Gestion des Emplois du Temps</p>
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
                    disabled={loading}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-3 text-gray-500 hover:text-gray-700"
                    disabled={loading}
                  >
                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>

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
                    disabled={loading}
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirm(!showConfirm)}
                    className="absolute right-3 top-3 text-gray-500 hover:text-gray-700"
                    disabled={loading}
                  >
                    {showConfirm ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
                {confirmPassword && newPassword !== confirmPassword && (
                  <p className="text-xs text-red-600 mt-1">Les mots de passe ne correspondent pas</p>
                )}
              </div>

              <Button 
                type="submit" 
                className="w-full h-12 bg-[#15803D] hover:bg-[#166534] text-white font-medium"
                disabled={loading || passwordStrength < 2}
              >
                {loading ? "Réinitialisation..." : "Réinitialiser le mot de passe"}
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