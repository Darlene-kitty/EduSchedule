/**
 * EXEMPLE: Composant Users View avec intégration API réelle
 * 
 * Ce fichier montre comment mettre à jour les composants existants
 * pour utiliser les vrais services API au lieu des données mockées.
 */

"use client"

import { useState, useEffect } from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Plus, Search, Filter, MoreVertical, Mail, Phone, Edit, Trash2 } from "lucide-react"
import { AddUserModal } from "./add-user-modal"
import { EditUserModal } from "./edit-user-modal"
import { DeleteConfirmationModal } from "./delete-confirmation-modal"
import { useQuery, useMutation } from "@/hooks/use-api"
import { userService } from "@/services"
import type { User } from "@/contexts/auth-context"

export function UsersViewExample() {
  const [searchQuery, setSearchQuery] = useState("")
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [isEditModalOpen, setIsEditModalOpen] = useState(false)
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false)
  const [selectedUser, setSelectedUser] = useState<User | null>(null)

  // Récupérer la liste des utilisateurs
  const { 
    data: usersResponse, 
    isLoading, 
    execute: fetchUsers 
  } = useQuery(userService.getUsers)

  // Mutation pour créer un utilisateur
  const { execute: createUser, isLoading: isCreating } = useMutation(
    userService.createUser,
    {
      successMessage: "Utilisateur créé avec succès",
      onSuccess: () => {
        setIsAddModalOpen(false)
        fetchUsers() // Recharger la liste
      }
    }
  )

  // Mutation pour mettre à jour un utilisateur
  const { execute: updateUser, isLoading: isUpdating } = useMutation(
    userService.updateUser,
    {
      successMessage: "Utilisateur mis à jour avec succès",
      onSuccess: () => {
        setIsEditModalOpen(false)
        setSelectedUser(null)
        fetchUsers()
      }
    }
  )

  // Mutation pour supprimer un utilisateur
  const { execute: deleteUser, isLoading: isDeleting } = useMutation(
    userService.deleteUser,
    {
      successMessage: "Utilisateur supprimé avec succès",
      onSuccess: () => {
        setIsDeleteModalOpen(false)
        setSelectedUser(null)
        fetchUsers()
      }
    }
  )

  // Charger les utilisateurs au montage du composant
  useEffect(() => {
    fetchUsers(0, 50) // page 0, 50 éléments
  }, [])

  // Filtrer les utilisateurs selon la recherche
  const filteredUsers = usersResponse?.content?.filter((user: any) =>
    user.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    user.email?.toLowerCase().includes(searchQuery.toLowerCase())
  ) || []

  const handleAddUser = async (data: any) => {
    await createUser(data)
  }

  const handleEditUser = async (data: any) => {
    if (selectedUser) {
      await updateUser(selectedUser.id, data)
    }
  }

  const handleDeleteUser = async () => {
    if (selectedUser) {
      await deleteUser(selectedUser.id)
    }
  }

  const getRoleBadgeColor = (role: string) => {
    switch (role.toLowerCase()) {
      case "admin":
        return "bg-red-100 text-red-800"
      case "teacher":
        return "bg-blue-100 text-blue-800"
      case "student":
        return "bg-green-100 text-green-800"
      default:
        return "bg-gray-100 text-gray-800"
    }
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header />
        <main className="flex-1 overflow-y-auto p-6">
          <div className="max-w-7xl mx-auto space-y-6">
            {/* En-tête */}
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-3xl font-bold text-gray-900">Utilisateurs</h1>
                <p className="text-gray-600 mt-1">
                  Gérez les utilisateurs du système
                </p>
              </div>
              <Button onClick={() => setIsAddModalOpen(true)}>
                <Plus className="h-4 w-4 mr-2" />
                Ajouter un utilisateur
              </Button>
            </div>

            {/* Barre de recherche et filtres */}
            <Card className="p-4">
              <div className="flex items-center gap-4">
                <div className="flex-1 relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                  <Input
                    placeholder="Rechercher par nom ou email..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="pl-10"
                  />
                </div>
                <Button variant="outline">
                  <Filter className="h-4 w-4 mr-2" />
                  Filtres
                </Button>
              </div>
            </Card>

            {/* Liste des utilisateurs */}
            {isLoading ? (
              <div className="text-center py-12">
                <p className="text-gray-500">Chargement des utilisateurs...</p>
              </div>
            ) : filteredUsers.length === 0 ? (
              <div className="text-center py-12">
                <p className="text-gray-500">Aucun utilisateur trouvé</p>
              </div>
            ) : (
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {filteredUsers.map((user: User) => (
                  <Card key={user.id} className="p-6 hover:shadow-lg transition-shadow">
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex items-center gap-3">
                        <div className="w-12 h-12 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white font-semibold">
                          {user.name.split(' ').map(n => n[0]).join('').toUpperCase()}
                        </div>
                        <div>
                          <h3 className="font-semibold text-gray-900">{user.name}</h3>
                          <Badge className={getRoleBadgeColor(user.role)}>
                            {user.role === 'admin' ? 'Administrateur' : 
                             user.role === 'teacher' ? 'Professeur' : 'Étudiant'}
                          </Badge>
                        </div>
                      </div>
                      <Button variant="ghost" size="sm">
                        <MoreVertical className="h-4 w-4" />
                      </Button>
                    </div>

                    <div className="space-y-2 mb-4">
                      <div className="flex items-center gap-2 text-sm text-gray-600">
                        <Mail className="h-4 w-4" />
                        <span>{user.email}</span>
                      </div>
                    </div>

                    <div className="flex gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        className="flex-1"
                        onClick={() => {
                          setSelectedUser(user)
                          setIsEditModalOpen(true)
                        }}
                      >
                        <Edit className="h-4 w-4 mr-1" />
                        Modifier
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        className="flex-1 text-red-600 hover:text-red-700"
                        onClick={() => {
                          setSelectedUser(user)
                          setIsDeleteModalOpen(true)
                        }}
                      >
                        <Trash2 className="h-4 w-4 mr-1" />
                        Supprimer
                      </Button>
                    </div>
                  </Card>
                ))}
              </div>
            )}
          </div>
        </main>
      </div>

      {/* Modals */}
      <AddUserModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onAdd={handleAddUser}
      />

      {selectedUser && (
        <>
          <EditUserModal
            isOpen={isEditModalOpen}
            onClose={() => {
              setIsEditModalOpen(false)
              setSelectedUser(null)
            }}
            onEdit={handleEditUser}
            user={selectedUser}
          />

          <DeleteConfirmationModal
            isOpen={isDeleteModalOpen}
            onClose={() => {
              setIsDeleteModalOpen(false)
              setSelectedUser(null)
            }}
            onConfirm={handleDeleteUser}
            title="Supprimer l'utilisateur"
            description={`Êtes-vous sûr de vouloir supprimer ${selectedUser.name} ? Cette action est irréversible.`}
          />
        </>
      )}
    </div>
  )
}
