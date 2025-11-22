import { DashboardView } from "@/components/dashboard-view"
import { AuthGuard } from "@/components/auth-guard"

export default function DashboardPage() {
  return (
    <AuthGuard requiredRoles={["admin", "teacher", "student"]}>
      <DashboardView />
    </AuthGuard>
  )
}
