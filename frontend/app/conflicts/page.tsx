import { ConflictsView } from "@/components/conflicts-view"
import { AuthGuard } from "@/components/auth-guard"

export default function ConflictsPage() {
  return (
    <AuthGuard requiredRoles={["admin"]}>
      <ConflictsView />
    </AuthGuard>
  )
}
