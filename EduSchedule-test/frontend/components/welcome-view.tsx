"use client"

import { Calendar, Clock, Users, BookOpen, ArrowRight, CheckCircle } from "lucide-react"
import { Button } from "@/components/ui/button"
import Link from "next/link"
import { useRouter } from "next/navigation"

export function WelcomeView() {
  const router = useRouter()

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#15803D] via-[#166534] to-[#14532D]">
      {/* Header */}
      <header className="absolute top-0 left-0 right-0 z-10 p-6">
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-[#FBBF24] rounded-xl flex items-center justify-center">
              <Calendar className="w-7 h-7 text-[#1F2937]" />
            </div>
            <div>
              <h1 className="text-white font-bold text-xl">EduSchedule</h1>
              <p className="text-green-100 text-xs">IUSJC - Saint Jean</p>
            </div>
          </div>
          <Link href="/login">
            <Button variant="outline" className="bg-white/10 text-white border-white/20 hover:bg-white/20">
              Se connecter
            </Button>
          </Link>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative pt-32 pb-20 px-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <div className="inline-block mb-6">
              <span className="bg-[#FBBF24] text-[#1F2937] px-4 py-2 rounded-full text-sm font-semibold">
                🎓 Institut Universitaire Saint Jean - Cameroun
              </span>
            </div>
            <h1 className="text-5xl md:text-6xl font-bold text-white mb-6 leading-tight">
              Bienvenue sur
              <br />
              <span className="text-[#FBBF24]">EduSchedule</span>
            </h1>
            <p className="text-xl text-green-100 mb-8 max-w-2xl mx-auto">
              Votre plateforme de gestion intelligente des emplois du temps, salles et ressources pédagogiques
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Button
                onClick={() => router.push("/login")}
                size="lg"
                className="bg-[#FBBF24] text-[#1F2937] hover:bg-[#F59E0B] font-semibold text-lg h-14 px-8"
              >
                Accéder à la plateforme
                <ArrowRight className="ml-2 w-5 h-5" />
              </Button>
              <Button
                onClick={() => router.push("/register")}
                size="lg"
                variant="outline"
                className="bg-white/10 text-white border-white/20 hover:bg-white/20 font-semibold text-lg h-14 px-8"
              >
                Créer un compte
              </Button>
            </div>
          </div>

          {/* Features Grid */}
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 mt-20">
            <FeatureCard
              icon={<Calendar className="w-8 h-8" />}
              title="Emplois du Temps"
              description="Planification automatique et optimisée des cours"
            />
            <FeatureCard
              icon={<Clock className="w-8 h-8" />}
              title="Gestion en Temps Réel"
              description="Mises à jour instantanées et notifications"
            />
            <FeatureCard
              icon={<Users className="w-8 h-8" />}
              title="Collaboration"
              description="Coordination entre enseignants et administration"
            />
            <FeatureCard
              icon={<BookOpen className="w-8 h-8" />}
              title="Ressources"
              description="Gestion des salles et équipements"
            />
          </div>
        </div>
      </section>

      {/* Benefits Section */}
      <section className="bg-white py-20 px-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Pourquoi EduSchedule ?
            </h2>
            <p className="text-xl text-gray-600 max-w-2xl mx-auto">
              Une solution complète pour simplifier la gestion académique de l'IUSJC
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            <BenefitCard
              title="Pour les Administrateurs"
              benefits={[
                "Création rapide des emplois du temps",
                "Détection automatique des conflits",
                "Rapports et statistiques détaillés",
                "Gestion centralisée des ressources",
              ]}
            />
            <BenefitCard
              title="Pour les Enseignants"
              benefits={[
                "Consultation de l'emploi du temps",
                "Réservation de salles simplifiée",
                "Notifications en temps réel",
                "Accès mobile et desktop",
              ]}
            />
            <BenefitCard
              title="Pour l'Institution"
              benefits={[
                "Optimisation des ressources",
                "Réduction des conflits",
                "Meilleure communication",
                "Gain de temps considérable",
              ]}
            />
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="bg-gradient-to-r from-[#15803D] to-[#166534] py-20 px-6">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-4xl font-bold text-white mb-6">
            Prêt à commencer ?
          </h2>
          <p className="text-xl text-green-100 mb-8">
            Rejoignez les enseignants et administrateurs qui utilisent déjà EduSchedule
          </p>
          <Button
            onClick={() => router.push("/login")}
            size="lg"
            className="bg-[#FBBF24] text-[#1F2937] hover:bg-[#F59E0B] font-semibold text-lg h-14 px-12"
          >
            Se connecter maintenant
            <ArrowRight className="ml-2 w-5 h-5" />
          </Button>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-[#1F2937] py-8 px-6">
        <div className="max-w-7xl mx-auto text-center">
          <p className="text-gray-400 text-sm">
            © 2025 Institut Universitaire Saint Jean - Cameroun. Tous droits réservés.
          </p>
          <p className="text-gray-500 text-xs mt-2">
            EduSchedule - Système de Gestion des Emplois du Temps
          </p>
        </div>
      </footer>
    </div>
  )
}

function FeatureCard({
  icon,
  title,
  description,
}: {
  icon: React.ReactNode
  title: string
  description: string
}) {
  return (
    <div className="bg-white/10 backdrop-blur-sm border border-white/20 rounded-2xl p-6 hover:bg-white/15 transition-all">
      <div className="w-14 h-14 bg-[#FBBF24] rounded-xl flex items-center justify-center mb-4 text-[#1F2937]">
        {icon}
      </div>
      <h3 className="text-white font-bold text-lg mb-2">{title}</h3>
      <p className="text-green-100 text-sm">{description}</p>
    </div>
  )
}

function BenefitCard({ title, benefits }: { title: string; benefits: string[] }) {
  return (
    <div className="bg-gray-50 rounded-2xl p-8 hover:shadow-lg transition-shadow">
      <h3 className="text-2xl font-bold text-gray-900 mb-6">{title}</h3>
      <ul className="space-y-4">
        {benefits.map((benefit, index) => (
          <li key={index} className="flex items-start gap-3">
            <CheckCircle className="w-5 h-5 text-[#15803D] flex-shrink-0 mt-0.5" />
            <span className="text-gray-700">{benefit}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}
