import { ReservationsView } from "@/components/reservations-view"
import { AuthGuard } from "@/components/auth-guard"

export default function ReservationsPage() {
  return (
    <AuthGuard requiredRoles={["admin", "teacher"]}>
      <ReservationsView />
    </AuthGuard>
  )
}
