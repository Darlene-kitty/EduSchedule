import { NotificationsView } from "@/components/notifications-view"
import { AuthGuard } from "@/components/auth-guard"

export default function NotificationsPage() {
  return (
    <AuthGuard requiredRoles={["admin", "teacher", "student"]}>
      <NotificationsView />
    </AuthGuard>
  )
}
