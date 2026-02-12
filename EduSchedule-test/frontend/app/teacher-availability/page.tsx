import { TeacherAvailabilityView } from "@/components/teacher-availability-view"
import { AuthGuard } from "@/components/auth-guard"

export default function TeacherAvailabilityPage() {
  return (
    <AuthGuard requiredRoles={["admin", "teacher"]}>
      <TeacherAvailabilityView />
    </AuthGuard>
  )
}