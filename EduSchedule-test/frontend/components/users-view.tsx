"use client"

import { useState } from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Plus, Search, Filter, MoreVertical, Mail, Phone, Edit, Trash2 } from "lucide-react"
import { AddUserModal } from "./add-user-modal"

interface User {
  id: string
  name: string
  email: string
  phone: string
  role: "Administrateur" | "Professeur" | "Étudiant"
  status: "Actif" | "Inactif"
  avatar: string
  courses?: number
}

const users: User[] = [
  {
    id: "1",
    name: "Dr. Martin",
    email: "martin@edu.fr",
    phone: "+33 6 12 34 56 78",
    role: "Professeur",
    status: "Actif",
    avatar: "DM",
    courses: 8,
  },
  {
    id: "2",
    name: "Prof. Bernard",
    email: "bernard@edu.fr",
    phone: "+33 6 23 45 67 89",
    role: "Professeur",
    status: "Actif",
    avatar: "PB",
    courses: 6,
  },
  {
    id: "3",
    name: "Dr. Laurent",
    email: "laurent@edu.fr",
    phone: "+33 6 34 56 78 90",
    role: "Professeur",
    status: "Actif",
    avatar: "DL",
    courses: 7,
  },
  {
    id: "4",
    name: "Admin Système",
    email: "admin@edu.fr",
    phone: "+33 6 45 67 89 01",
    role: "Administrateur",
    status: "Actif",
    avatar: "AS",
  },
  {
    id: "5",
    name: "Prof. Dubois",
    email: "dubois@edu.fr",
    phone: "+33 6 56 78 90 12",
    role: "Professeur",
    status: "Actif",
    avatar: "PD",
    courses: 5,
  },
  {
    id: "6",
    name: "Dr. Sophie",
    email: "sophie@edu.fr",
    phone: "+33 6 67 89 01 23",
    role: "Professeur",
    status: "Actif",
    avatar: "DS",
    courses: 9,
  },
  {
    id: "7",
    name: "Prof. Richard",
    email: "richard@edu.fr",
    phone: "+33 6 78 90 12 34",
    role: "Professeur",
    status: "Inactif",
    avatar: "PR",
    courses: 3,
  },
  {
    id: "8",
    name: "Marie Dupont",
    email: "marie.d@student.edu.fr",
    phone: "+33 6 89 01 23 45",
    role: "Étudiant",
    status: "Actif",
    avatar: "MD",
  },
]

export function UsersView() {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedRole, setSelectedRole] = useState<string>("all")
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)

  const filteredUsers = users.filter((user) => {
    const matchesSearch =
      user.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email.toLowerCase().includes(searchQuery.toLowerCase())
    const matchesRole = selectedRole === "all" || user.role === selectedRole
    return matchesSearch && matchesRole
  })

  const getRoleColor = (role: string) => {
    switch (role) {
      case "Administrateur":
        return "bg-purple-100 text-purple-700"
      case "Professeur":
        return "bg-blue-100 text-blue-700"
      case "Étudiant":
        return "bg-green-100 text-green-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const getStatusColor = (status: string) => {
    return status === "Actif" ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-700"
  }

  const handleAddUser = (userData: any) => {
    console.log("[v0] New user added:", userData)
    // Here you would typically add the user to your state/database
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="users" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header title="Utilisateurs" subtitle="Gérez les professeurs, étudiants et administrateurs" />

        <main className="flex-1 overflow-y-auto p-6">
          {/* Actions Bar */}
          <div className="flex flex-col sm:flex-row gap-4 mb-6">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Rechercher un utilisateur..."
                className="pl-10"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <div className="flex gap-2">
              <Button variant="outline" className="gap-2 bg-transparent">
                <Filter className="w-4 h-4" />
                Filtrer
              </Button>
              <Button className="gap-2 bg-[#15803D] hover:bg-[#15803D]/90" onClick={() => setIsAddModalOpen(true)}>
                <Plus className="w-4 h-4" />
                Ajouter un utilisateur
              </Button>
            </div>
          </div>

          {/* Role Filter Tabs */}
          <div className="flex gap-2 mb-6 overflow-x-auto">
            {["all", "Administrateur", "Professeur", "Étudiant"].map((role) => (
              <Button
                key={role}
                variant={selectedRole === role ? "default" : "outline"}
                size="sm"
                onClick={() => setSelectedRole(role)}
                className={selectedRole === role ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
              >
                {role === "all" ? "Tous" : role}
              </Button>
            ))}
          </div>

          {/* Users Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredUsers.map((user) => (
              <Card key={user.id} className="p-6 hover:shadow-lg transition-shadow">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 bg-[#15803D] rounded-full flex items-center justify-center font-bold text-white">
                      {user.avatar}
                    </div>
                    <div>
                      <h3 className="font-semibold">{user.name}</h3>
                      <Badge className={`${getRoleColor(user.role)} text-xs mt-1`}>{user.role}</Badge>
                    </div>
                  </div>
                  <Button variant="ghost" size="icon">
                    <MoreVertical className="w-4 h-4" />
                  </Button>
                </div>

                <div className="space-y-2 mb-4">
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Mail className="w-4 h-4" />
                    <span className="truncate">{user.email}</span>
                  </div>
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Phone className="w-4 h-4" />
                    <span>{user.phone}</span>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-4 border-t">
                  <Badge className={`${getStatusColor(user.status)} text-xs`}>{user.status}</Badge>
                  {user.courses !== undefined && (
                    <span className="text-sm text-muted-foreground">{user.courses} cours</span>
                  )}
                </div>

                <div className="flex gap-2 mt-4">
                  <Button variant="outline" size="sm" className="flex-1 gap-2 bg-transparent">
                    <Edit className="w-3 h-3" />
                    Modifier
                  </Button>
                  <Button variant="outline" size="sm" className="gap-2 text-red-600 hover:text-red-700 bg-transparent">
                    <Trash2 className="w-3 h-3" />
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        </main>
      </div>

      <AddUserModal open={isAddModalOpen} onOpenChange={setIsAddModalOpen} onSubmit={handleAddUser} />
    </div>
  )
}
