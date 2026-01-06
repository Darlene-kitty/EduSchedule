import { ResourcesView } from "@/components/resources-view"
import { AuthGuard } from "@/components/auth-guard"

export default function ResourcesPage() {
  return (
    <AuthGuard requiredRoles={["admin"]}>
      <ResourcesView />
    </AuthGuard>
  )
}
