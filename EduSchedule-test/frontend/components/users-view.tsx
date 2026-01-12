"use client"

import { useState, useEffect } from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Plus, Search, Filter, MoreVertical, Mail, User as UserIcon, Edit, Trash2, Loader2, AlertCircle } from "lucide-react"
import { AddUserModal } from "./add-user-modal"
import { usersApi, User as ApiUser, CreateUserRequest } from "@/lib/api/users"
import { useToast } from "@/hooks/use-toast"

interface User {
  id: number
  username: string
  email: string
  role: string
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export function UsersView() {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedRole, setSelectedRole] = useState<string>("all")
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const { toast } = useToast()

  // Charger les utilisateurs au montage du composant
  useEffect(() => {
    loadUsers()
  }, [])

  const loadUsers = async () => {
    try {
      setLoading(true)
      setError(null)
      const usersData = await usersApi.getAllUsers()
      setUsers(usersData)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors du chargement des utilisateurs'
      setError(errorMessage)
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const filteredUsers = users.filter((user) => {
    const matchesSearch =
      user.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email.toLowerCase().includes(searchQuery.toLowerCase())
    const matchesRole = selectedRole === "all" || user.role === selectedRole
    return matchesSearch && matchesRole
  })

  const getRoleColor = (role: string) => {
    switch (role) {
      case "ADMIN":
        return "bg-purple-100 text-purple-700"
      case "TEACHER":
        return "bg-blue-100 text-blue-700"
      case "STUDENT":
        return "bg-green-100 text-green-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const getRoleLabel = (role: string) => {
    switch (role) {
      case "ADMIN":
        return "Administrateur"
      case "TEACHER":
        return "Professeur"
      case "STUDENT":
        return "Étudiant"
      default:
        return role
    }
  }

  const getStatusColor = (enabled: boolean) => {
    return enabled ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-700"
  }

  const getStatusLabel = (enabled: boolean) => {
    return enabled ? "Actif" : "Inactif"
  }

  const getInitials = (username: string) => {
    return username.substring(0, 2).toUpperCase()
  }

  const handleAddUser = async (userData: CreateUserRequest) => {
    try {
      const newUser = await usersApi.createUser(userData)
      setUsers(prev => [...prev, newUser])
      toast({
        title: "Succès",
        description: "Utilisateur créé avec succès",
      })
      setIsAddModalOpen(false)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la création de l\'utilisateur'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const handleDeleteUser = async (userId: number) => {
    if (!confirm("Êtes-vous sûr de vouloir supprimer cet utilisateur ?")) {
      return
    }

    try {
      await usersApi.deleteUser(userId)
      setUsers(prev => prev.filter(user => user.id !== userId))
      toast({
        title: "Succès",
        description: "Utilisateur supprimé avec succès",
      })
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la suppression de l\'utilisateur'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  if (loading) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="users" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Utilisateurs" subtitle="Gérez les professeurs, étudiants et administrateurs" />
          <main className="flex-1 flex items-center justify-center">
            <div className="flex items-center gap-2">
              <Loader2 className="w-6 h-6 animate-spin" />
              <span>Chargement des utilisateurs...</span>
            </div>
          </main>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="users" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Utilisateurs" subtitle="Gérez les professeurs, étudiants et administrateurs" />
          <main className="flex-1 flex items-center justify-center">
            <div className="text-center">
              <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Erreur de chargement</h3>
              <p className="text-muted-foreground mb-4">{error}</p>
              <Button onClick={loadUsers}>Réessayer</Button>
            </div>
          </main>
        </div>
      </div>
    )
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
            {["all", "ADMIN", "TEACHER", "STUDENT"].map((role) => (
              <Button
                key={role}
                variant={selectedRole === role ? "default" : "outline"}
                size="sm"
                onClick={() => setSelectedRole(role)}
                className={selectedRole === role ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
              >
                {role === "all" ? "Tous" : getRoleLabel(role)}
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
                      {getInitials(user.username)}
                    </div>
                    <div>
                      <h3 className="font-semibold">{user.username}</h3>
                      <Badge className={`${getRoleColor(user.role)} text-xs mt-1`}>
                        {getRoleLabel(user.role)}
                      </Badge>
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
                    <UserIcon className="w-4 h-4" />
                    <span>ID: {user.id}</span>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-4 border-t">
                  <Badge className={`${getStatusColor(user.enabled)} text-xs`}>
                    {getStatusLabel(user.enabled)}
                  </Badge>
                  <span className="text-sm text-muted-foreground">
                    {new Date(user.createdAt).toLocaleDateString()}
                  </span>
                </div>

                <div className="flex gap-2 mt-4">
                  <Button variant="outline" size="sm" className="flex-1 gap-2 bg-transparent">
                    <Edit className="w-3 h-3" />
                    Modifier
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="gap-2 text-red-600 hover:text-red-700 bg-transparent"
                    onClick={() => handleDeleteUser(user.id)}
                  >
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
