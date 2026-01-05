import { UsersView } from "@/components/users-view"
import { AuthGuard } from "@/components/auth-guard"

export default function UsersPage() {
  return (
    <AuthGuard requiredRoles={["admin"]}>
      <UsersView />
    </AuthGuard>
  )
}
