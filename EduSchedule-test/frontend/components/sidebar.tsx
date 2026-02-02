"use client"

import {
  Calendar,
  LayoutDashboard,
  Users,
  Clock,
  BookOpen,
  Package,
  AlertTriangle,
  BarChart3,
  Bell,
  LogOut,
  User,
  UserCheck,
  Settings,
  School,
  Brain,
  TrendingUp,
} from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { useAuth } from "@/contexts/auth-context"
import { useToast } from "@/contexts/toast-context"
import { PermissionGate } from "@/components/permission-gate"

const menuItems = [
  {
    icon: LayoutDashboard,
    label: "Tableau de bord",
    href: "/dashboard",
    key: "dashboard",
    roles: ["admin", "teacher", "student"],
  },
  { 
    icon: User, 
    label: "Mon Profil", 
    href: "/profile", 
    key: "profile", 
    roles: ["admin", "teacher", "student"] 
  },
  
  // Section Gestion des utilisateurs
  { 
    icon: Users, 
    label: "Utilisateurs", 
    href: "/users", 
    key: "users", 
    roles: ["admin"] 
  },
  { 
    icon: School, 
    label: "Écoles", 
    href: "/schools", 
    key: "schools", 
    roles: ["admin"] 
  },
  
  // Section Planning et Emplois du temps
  {
    icon: Clock,
    label: "Emplois du temps",
    href: "/schedule",
    key: "schedule",
    roles: ["admin", "teacher", "student"],
  },
  {
    icon: Calendar,
    label: "Calendrier",
    href: "/calendar",
    key: "calendar",
    roles: ["admin", "teacher", "student"],
  },
  { 
    icon: UserCheck, 
    label: "Disponibilités", 
    href: "/teacher-availability", 
    key: "teacher-availability", 
    roles: ["admin", "teacher"] 
  },
  
  // Section Réservations et Ressources
  {
    icon: Calendar,
    label: "Réservation salles",
    href: "/reservations",
    key: "reservations",
    roles: ["admin", "teacher"],
  },
  { 
    icon: Package, 
    label: "Ressources", 
    href: "/resources", 
    key: "resources", 
    roles: ["admin", "teacher"] 
  },
  
  // Section Cours et Formation
  { 
    icon: BookOpen, 
    label: "Cours & Groupes", 
    href: "/courses", 
    key: "courses", 
    roles: ["admin", "teacher"] 
  },
  
  // Section Gestion et Monitoring
  { 
    icon: AlertTriangle, 
    label: "Conflits", 
    href: "/conflicts", 
    key: "conflicts", 
    roles: ["admin"], 
    badge: 3 
  },
  {
    icon: Bell,
    label: "Notifications",
    href: "/notifications",
    key: "notifications",
    roles: ["admin", "teacher", "student"],
    badge: 2,
  },
  
  // Section Analytics et Rapports
  { 
    icon: BarChart3, 
    label: "Rapports", 
    href: "/reports", 
    key: "reports", 
    roles: ["admin"] 
  },
  { 
    icon: TrendingUp, 
    label: "Analytics", 
    href: "/analytics", 
    key: "analytics", 
    roles: ["admin"] 
  },
  { 
    icon: Brain, 
    label: "IA Prédictive", 
    href: "/ai-insights", 
    key: "ai-insights", 
    roles: ["admin"] 
  },
]

interface SidebarProps {
  activePage?: string
}

export function Sidebar({ activePage = "dashboard" }: SidebarProps) {
  const router = useRouter()
  const { user, logout } = useAuth()
  const { addToast } = useToast()

  const handleLogout = async () => {
    try {
      await logout()
      addToast("Déconnecté avec succès", "success")
      router.push("/login")
    } catch (error) {
      addToast("Erreur lors de la déconnexion", "error")
    }
  }

  if (!user) return null

  return (
    <aside className="w-64 bg-[#1F2937] text-white flex flex-col">
      <div className="p-6 flex items-center gap-3">
        <div className="w-12 h-12 bg-[#FBBF24] rounded-xl flex items-center justify-center">
          <Calendar className="w-6 h-6 text-[#1F2937]" />
        </div>
        <div>
          <h1 className="font-bold text-lg">EduSchedule</h1>
          <p className="text-sm text-gray-400">Gestion intelligente</p>
        </div>
      </div>

      <div className="px-4 py-3 bg-[#374151] mx-4 rounded-lg flex items-center gap-3">
        <img src={user.avatar || "/placeholder.svg"} alt={user.name} className="w-10 h-10 rounded-full" />
        <div className="flex-1">
          <p className="font-semibold text-sm">{user.name}</p>
          <p className="text-xs text-gray-400 capitalize">{user.role}</p>
        </div>
      </div>

      <nav className="flex-1 px-3 py-6 space-y-1 overflow-y-auto">
        {menuItems.map((item) => (
          <PermissionGate key={item.key} requiredRoles={item.roles as any}>
            <Link
              href={item.href}
              className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors relative ${
                activePage === item.key ? "bg-[#15803D] text-white" : "text-gray-300 hover:bg-[#374151]"
              }`}
            >
              <item.icon className="w-5 h-5" />
              <span className="text-sm font-medium">{item.label}</span>
              {item.badge && (
                <span className="ml-auto bg-red-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
                  {item.badge}
                </span>
              )}
            </Link>
          </PermissionGate>
        ))}
      </nav>

      <button
        onClick={handleLogout}
        className="mx-3 mb-6 flex items-center gap-3 px-3 py-2.5 rounded-lg text-gray-300 hover:bg-[#374151] transition-colors w-full"
      >
        <LogOut className="w-5 h-5" />
        <span className="text-sm font-medium">Déconnexion</span>
      </button>
    </aside>
  )
}
