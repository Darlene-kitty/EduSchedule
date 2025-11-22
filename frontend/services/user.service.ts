/**
 * Service de gestion des utilisateurs
 */

import { apiClient } from '@/lib/api-client'
import { API_CONFIG } from '@/lib/api-config'
import type { User, UserRole } from '@/contexts/auth-context'

export interface CreateUserRequest {
  email: string
  password: string
  nom: string
  prenom: string
  role: string
}

export interface UpdateUserRequest {
  email?: string
  nom?: string
  prenom?: string
  role?: string
}

export interface UserListResponse {
  content: any[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

class UserService {
  /**
   * Récupérer tous les utilisateurs (avec pagination)
   */
  async getUsers(page = 0, size = 10): Promise<UserListResponse> {
    return apiClient.get<UserListResponse>(API_CONFIG.endpoints.users.base, {
      params: { page, size }
    })
  }

  /**
   * Récupérer un utilisateur par ID
   */
  async getUserById(id: string): Promise<User> {
    const response = await apiClient.get<any>(API_CONFIG.endpoints.users.byId(id))
    return this.mapBackendUserToFrontend(response)
  }

  /**
   * Créer un nouvel utilisateur
   */
  async createUser(data: CreateUserRequest): Promise<User> {
    const response = await apiClient.post<any>(API_CONFIG.endpoints.users.base, data)
    return this.mapBackendUserToFrontend(response)
  }

  /**
   * Mettre à jour un utilisateur
   */
  async updateUser(id: string, data: UpdateUserRequest): Promise<User> {
    const response = await apiClient.put<any>(API_CONFIG.endpoints.users.byId(id), data)
    return this.mapBackendUserToFrontend(response)
  }

  /**
   * Supprimer un utilisateur
   */
  async deleteUser(id: string): Promise<void> {
    await apiClient.delete(API_CONFIG.endpoints.users.byId(id))
  }

  /**
   * Rechercher des utilisateurs
   */
  async searchUsers(query: string, role?: UserRole): Promise<User[]> {
    const params: Record<string, string> = { q: query }
    if (role) {
      params.role = role.toUpperCase()
    }

    const response = await apiClient.get<any[]>(API_CONFIG.endpoints.users.base, { params })
    return response.map(user => this.mapBackendUserToFrontend(user))
  }

  /**
   * Mapper les données utilisateur du backend vers le format frontend
   */
  private mapBackendUserToFrontend(backendUser: any): User {
    const roleMap: Record<string, UserRole> = {
      'ADMIN': 'admin',
      'TEACHER': 'teacher',
      'ENSEIGNANT': 'teacher',
      'STUDENT': 'student',
      'ETUDIANT': 'student',
    }

    const role = roleMap[backendUser.role?.toUpperCase()] || 'student'
    const name = backendUser.nom && backendUser.prenom 
      ? `${backendUser.prenom} ${backendUser.nom}`
      : backendUser.name || backendUser.email

    return {
      id: backendUser.id,
      email: backendUser.email,
      name,
      role,
      avatar: backendUser.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${backendUser.email}`,
      createdAt: backendUser.createdAt ? new Date(backendUser.createdAt) : undefined,
    }
  }
}

export const userService = new UserService()
