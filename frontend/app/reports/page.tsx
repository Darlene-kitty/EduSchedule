import { ReportsView } from "@/components/reports-view"
import { AuthGuard } from "@/components/auth-guard"

export default function ReportsPage() {
  return (
    <AuthGuard requiredRoles={["admin"]}>
      <ReportsView />
    </AuthGuard>
  )
}
