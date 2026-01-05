"use client"

import { Button } from "@/components/ui/button"
import { useRouter } from "next/navigation"
import { Shield } from "lucide-react"

export default function UnauthorizedPage() {
  const router = useRouter()

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md text-center">
        <div className="inline-flex items-center justify-center w-20 h-20 bg-red-100 rounded-full mb-6">
          <Shield className="w-10 h-10 text-red-600" />
        </div>
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Accès Refusé</h1>
        <p className="text-gray-600 mb-8">Vous n'avez pas les permissions nécessaires pour accéder à cette page.</p>

        <div className="flex gap-3">
          <Button
            onClick={() => router.back()}
            className="flex-1 bg-white border border-gray-300 text-gray-900 hover:bg-gray-50"
          >
            Retour
          </Button>
          <Button onClick={() => router.push("/")} className="flex-1 bg-[#15803D] hover:bg-[#166534] text-white">
            Accueil
          </Button>
        </div>
      </div>
    </div>
  )
}
