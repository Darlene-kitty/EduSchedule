"use client"

import type React from "react"
import { useState } from "react"
import { Calendar, ArrowLeft } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import Link from "next/link"

export function VerifyEmailView() {
  const [email, setEmail] = useState("")
  const [verificationCode, setVerificationCode] = useState("")
  const [codesSent, setCodesSent] = useState(false)
  const [verified, setVerified] = useState(false)
  const [timeLeft, setTimeLeft] = useState(600)

  const handleSendCode = (e: React.FormEvent) => {
    e.preventDefault()
    setCodesSent(true)
    setTimeLeft(600) // 10 minutes
  }

  const handleVerify = (e: React.FormEvent) => {
    e.preventDefault()
    if (verificationCode.length === 6) {
      setVerified(true)
    } else {
      alert("Le code doit contenir 6 chiffres")
    }
  }

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins}:${secs.toString().padStart(2, "0")}`
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
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Vérifier votre email</h2>
            <p className="text-gray-600">Confirmez votre adresse email pour accéder au système</p>
          </div>

          {!verified ? (
            <>
              {!codesSent ? (
                <form onSubmit={handleSendCode} className="space-y-5">
                  <div>
                    <Label htmlFor="email" className="text-gray-900 font-medium">
                      Adresse email
                    </Label>
                    <p className="text-sm text-gray-600 mt-1 mb-3">
                      Entrez votre adresse email. Nous vous enverrons un code de vérification.
                    </p>
                    <Input
                      id="email"
                      type="email"
                      placeholder="nom@exemple.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      className="h-12"
                      required
                    />
                  </div>

                  <Button type="submit" className="w-full h-12 bg-[#15803D] hover:bg-[#166534] text-white font-medium">
                    Envoyer le code
                  </Button>
                </form>
              ) : (
                <form onSubmit={handleVerify} className="space-y-5">
                  <div>
                    <Label htmlFor="code" className="text-gray-900 font-medium">
                      Code de vérification
                    </Label>
                    <p className="text-sm text-gray-600 mt-1 mb-3">
                      Entrez le code à 6 chiffres envoyé à <strong>{email}</strong>
                    </p>
                    <Input
                      id="code"
                      type="text"
                      placeholder="000000"
                      value={verificationCode}
                      onChange={(e) => setVerificationCode(e.target.value.slice(0, 6))}
                      className="h-12 text-center text-2xl tracking-widest font-mono"
                      maxLength={6}
                      required
                    />
                  </div>

                  <div className="text-center text-sm text-gray-600">
                    Code expire dans: <span className="font-semibold text-[#15803D]">{formatTime(timeLeft)}</span>
                  </div>

                  <Button type="submit" className="w-full h-12 bg-[#15803D] hover:bg-[#166534] text-white font-medium">
                    Vérifier le code
                  </Button>

                  <button
                    type="button"
                    onClick={() => setCodesSent(false)}
                    className="w-full text-[#15803D] hover:text-[#166534] font-medium"
                  >
                    Changer d'email
                  </button>
                </form>
              )}
            </>
          ) : (
            <div className="space-y-4 text-center">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto">
                <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">Email vérifié!</h3>
                <p className="text-gray-600 mb-6">Votre email a été confirmé avec succès.</p>
              </div>

              <Link href="/login">
                <Button className="w-full h-12 bg-[#15803D] hover:bg-[#166534] text-white font-medium">
                  Aller à la connexion
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
