"use client"

import type React from "react"
import { useState, useEffect } from "react"
import { Calendar, ArrowLeft, Mail, Sparkles, Shield, CheckCircle2, RefreshCw } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import Link from "next/link"
import { authService } from "@/services/auth.service"
import { useToast } from "@/hooks/use-toast"

export function ForgotPasswordView() {
  const [email, setEmail] = useState("")
  const [submitted, setSubmitted] = useState(false)
  const [loading, setLoading] = useState(false)
  const [mounted, setMounted] = useState(false)
  const { toast } = useToast()

  useEffect(() => {
    setMounted(true)
  }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!email.trim()) {
      toast({
        title: "Erreur",
        description: "Veuillez entrer votre adresse email",
        variant: "destructive",
      })
      return
    }

    setLoading(true)
    
    try {
      await authService.forgotPassword(email)
      setSubmitted(true)
      toast({
        title: "Email envoyé",
        description: "Si cet email existe dans notre système, vous recevrez un lien de réinitialisation.",
        variant: "default",
      })
    } catch (error: any) {
      console.error('Forgot password error:', error)
      toast({
        title: "Erreur",
        description: error.message || "Une erreur est survenue lors de l'envoi de l'email",
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

      <div className="relative z-10 min-h-screen flex items-center justify-center p-4">
        <div className="w-full max-w-md animate-fade-in-up">
          {/* Header */}
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-[#15803D] to-[#22C55E] rounded-3xl shadow-lg mb-6">
              <Calendar className="w-10 h-10 text-white" />
            </div>
            <h1 className="text-2xl font-bold text-gray-900 mb-2">EduSchedule - IUSJC</h1>
            <p className="text-gray-600">Système de Gestion des Emplois du Temps</p>
          </div>

          <div className="bg-white/80 backdrop-blur-sm rounded-3xl shadow-2xl border border-white/20 p-8 lg:p-10">
            {!submitted ? (
              <>
                <div className="mb-8">
                  <div className="flex items-center mb-4">
                    <Shield className="w-6 h-6 text-[#15803D] mr-2" />
                    <h2 className="text-2xl lg:text-3xl font-bold text-gray-900">Mot de passe oublié</h2>
                  </div>
                  <p className="text-gray-600">Pas de souci ! Nous allons vous aider à récupérer votre accès.</p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                  <div className="space-y-2">
                    <Label htmlFor="email" className="text-gray-900 font-medium flex items-center">
                      <Mail className="w-4 h-4 mr-2" />
                      Adresse email
                    </Label>
                    <p className="text-sm text-gray-600 mb-3">
                      Entrez l'email associé à votre compte. Nous vous enverrons un lien sécurisé pour réinitialiser votre mot de passe.
                    </p>
                    <Input
                      id="email"
                      type="email"
                      placeholder="nom@exemple.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      className="h-12 pl-4 pr-4 bg-gray-50/50 border-gray-200 focus:bg-white focus:border-[#15803D] focus:ring-[#15803D]/20 transition-all duration-200"
                      required
                      disabled={loading}
                    />
                  </div>

                  <Button 
                    type="submit" 
                    className="w-full h-12 bg-gradient-to-r from-[#15803D] to-[#22C55E] hover:from-[#166534] hover:to-[#16A34A] text-white font-medium shadow-lg hover:shadow-xl transition-all duration-200 group"
                    disabled={loading}
                  >
                    {loading ? (
                      <div className="flex items-center">
                        <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></div>
                        Envoi en cours...
                      </div>
                    ) : (
                      <div className="flex items-center justify-center">
                        <Mail className="w-5 h-5 mr-2" />
                        Envoyer le lien de réinitialisation
                      </div>
                    )}
                  </Button>
                </form>

                {/* Security info */}
                <div className="mt-6 p-4 bg-gradient-to-r from-blue-50 to-green-50 rounded-2xl border border-blue-100">
                  <div className="flex items-start space-x-3">
                    <Shield className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" />
                    <div>
                      <p className="text-sm font-medium text-gray-900 mb-1">Sécurité garantie</p>
                      <p className="text-xs text-gray-600">
                        Le lien de réinitialisation expire dans 24 heures et ne peut être utilisé qu'une seule fois.
                      </p>
                    </div>
                  </div>
                </div>
              </>
            ) : (
              <div className="space-y-6 text-center">
                <div className="flex items-center justify-center">
                  <div className="w-20 h-20 bg-gradient-to-br from-green-100 to-green-200 rounded-full flex items-center justify-center">
                    <CheckCircle2 className="w-10 h-10 text-green-600" />
                  </div>
                </div>
                
                <div>
                  <div className="flex items-center justify-center mb-4">
                    <Sparkles className="w-6 h-6 text-[#15803D] mr-2" />
                    <h3 className="text-2xl font-bold text-gray-900">Email envoyé avec succès !</h3>
                  </div>
                  <p className="text-gray-600 mb-6 leading-relaxed">
                    Si l'email <span className="font-semibold text-[#15803D]">{email}</span> existe dans notre système, 
                    vous recevrez un lien de réinitialisation dans quelques minutes.
                  </p>
                </div>

                {/* Instructions */}
                <div className="bg-gradient-to-r from-gray-50 to-blue-50 rounded-2xl p-6 text-left">
                  <h4 className="font-semibold text-gray-900 mb-3">Étapes suivantes :</h4>
                  <div className="space-y-2 text-sm text-gray-600">
                    <div className="flex items-center">
                      <div className="w-2 h-2 bg-[#15803D] rounded-full mr-3"></div>
                      Vérifiez votre boîte de réception
                    </div>
                    <div className="flex items-center">
                      <div className="w-2 h-2 bg-[#15803D] rounded-full mr-3"></div>
                      Consultez aussi votre dossier spam/courrier indésirable
                    </div>
                    <div className="flex items-center">
                      <div className="w-2 h-2 bg-[#15803D] rounded-full mr-3"></div>
                      Cliquez sur le lien dans l'email reçu
                    </div>
                    <div className="flex items-center">
                      <div className="w-2 h-2 bg-[#15803D] rounded-full mr-3"></div>
                      Créez votre nouveau mot de passe
                    </div>
                  </div>
                </div>

                <Button
                  onClick={() => {
                    setSubmitted(false)
                    setEmail("")
                  }}
                  variant="outline"
                  className="w-full h-12 border-gray-300 text-gray-900 hover:bg-gray-50 transition-all duration-200 group"
                >
                  <RefreshCw className="w-4 h-4 mr-2 group-hover:rotate-180 transition-transform duration-300" />
                  Essayer un autre email
                </Button>
              </div>
            )}

            <div className="mt-8 pt-6 border-t border-gray-200">
              <Link
                href="/login"
                className="flex items-center gap-2 text-[#15803D] hover:text-[#166534] font-medium justify-center transition-colors duration-200 group"
              >
                <ArrowLeft className="w-4 h-4 group-hover:-translate-x-1 transition-transform duration-200" />
                Retour à la connexion
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
