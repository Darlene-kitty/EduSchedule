import { AdvancedDashboard } from "@/components/advanced-dashboard"
import { AuthGuard } from "@/components/auth-guard"
import { Sidebar } from "@/components/sidebar"
import { Header } from "@/components/header"

export default function AnalyticsPage() {
  return (
    <AuthGuard requiredRoles={["admin"]} redirectTo="/unauthorized">
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="analytics" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header 
            title="Analytics Avancées" 
            subtitle="Analyse détaillée et optimisation des ressources" 
          />
          <main className="flex-1 overflow-y-auto p-6">
            <AdvancedDashboard />
          </main>
        </div>
      </div>
    </AuthGuard>
  )
}