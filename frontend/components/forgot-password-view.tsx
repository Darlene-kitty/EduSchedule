"use client"

import type React from "react"
import { useState } from "react"
import { Calendar, ArrowLeft } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import Link from "next/link"

export function ForgotPasswordView() {
  const [email, setEmail] = useState("")
  const [submitted, setSubmitted] = useState(false)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    // Simulate sending reset email
    setSubmitted(true)
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
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Mot de passe oublié</h2>
            <p className="text-gray-600">Réinitialisez votre mot de passe en quelques étapes</p>
          </div>

          {!submitted ? (
            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <Label htmlFor="email" className="text-gray-900 font-medium">
                  Adresse email
                </Label>
                <p className="text-sm text-gray-600 mt-1 mb-3">
                  Entrez l'email associé à votre compte. Nous vous enverrons un lien pour réinitialiser votre mot de
                  passe.
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
                Envoyer le lien de réinitialisation
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
                <h3 className="text-lg font-semibold text-gray-900 mb-2">Email envoyé avec succès!</h3>
                <p className="text-gray-600 mb-6">
                  Vérifiez votre boîte email à <strong>{email}</strong> pour le lien de réinitialisation.
                </p>
                <p className="text-sm text-gray-600 mb-6">
                  Le lien expire dans 24 heures. Si vous ne le recevez pas, vérifiez votre dossier spam.
                </p>
              </div>

              <Button
                onClick={() => setSubmitted(false)}
                variant="outline"
                className="w-full h-12 border-gray-300 text-gray-900"
              >
                Essayer un autre email
              </Button>
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
