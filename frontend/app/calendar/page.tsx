import { CalendarView } from "@/components/calendar-view"
import { AuthGuard } from "@/components/auth-guard"

export default function CalendarPage() {
  return (
    <AuthGuard requiredRoles={["admin", "teacher", "student"]}>
      <CalendarView />
    </AuthGuard>
  )
}
