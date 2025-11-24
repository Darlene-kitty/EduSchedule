import { ScheduleView } from "@/components/schedule-view"
import { AuthGuard } from "@/components/auth-guard"

export default function SchedulePage() {
  return (
    <AuthGuard requiredRoles={["admin", "teacher"]}>
      <ScheduleView />
    </AuthGuard>
  )
}
