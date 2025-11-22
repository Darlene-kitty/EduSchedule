import type { UserRole } from "@/contexts/auth-context"

export const rolePermissions: Record<UserRole, string[]> = {
  admin: [
    "view_all_schedules",
    "edit_all_schedules",
    "delete_schedules",
    "manage_users",
    "manage_courses",
    "manage_resources",
    "manage_reservations",
    "view_conflicts",
    "resolve_conflicts",
    "view_reports",
    "manage_notifications",
  ],
  teacher: [
    "view_own_schedule",
    "edit_own_courses",
    "manage_own_reservations",
    "view_resources",
    "view_notifications",
    "create_course",
  ],
  student: ["view_schedule", "view_own_schedule", "view_calendar", "view_notifications"],
}

export function hasPermission(role: UserRole, permission: string): boolean {
  return rolePermissions[role]?.includes(permission) ?? false
}

export function hasAnyPermission(role: UserRole, permissions: string[]): boolean {
  return permissions.some((p) => hasPermission(role, p))
}

export function hasAllPermissions(role: UserRole, permissions: string[]): boolean {
  return permissions.every((p) => hasPermission(role, p))
}
