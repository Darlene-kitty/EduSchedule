import { CoursesView } from "@/components/courses-view"
import { AuthGuard } from "@/components/auth-guard"

export default function CoursesPage() {
  return (
    <AuthGuard requiredRoles={["admin", "teacher"]}>
      <CoursesView />
    </AuthGuard>
  )
}
