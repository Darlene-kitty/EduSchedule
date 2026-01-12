/**
 * Service de gestion de profil - Connexion avec le backend ProfileController
 */

import { apiClient } from '@/lib/api-client'
import { API_CONFIG } from '@/lib/api-config'
import type { User } from '@/contexts/auth-context'

export interface ProfileUpdateRequest {
  username?: string
  email?: string
}

export interface PasswordChangeRequest {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

export interface ProfileData {
  name: string
  email: string
  phone?: string
  address?: string
  bio?: string
  department?: string
  specialization?: string
}

export interface UserPreferences {
  emailNotifications: boolean
  pushNotifications: boolean
  language: string
  theme: string
}

export interface UserManagementDTO {
  id: number
  username: string
  email: string
  role: string
  enabled: boolean
  createdAt?: string
  updatedAt?: string
}

class ProfileService {
  /**
   * Récupérer le profil de l'utilisateur connecté
   */
  async getCurrentProfile(): Promise<User> {
    const response = await apiClient.get<any>(API_CONFIG.endpoints.profile.base)
    return this.mapBackendUserToFrontend(response)
  }

  /**
   * Mettre à jour le profil de l'utilisateur connecté
   */
  async updateProfile(data: ProfileUpdateRequest): Promise<User> {
    const response = await apiClient.put<any>(
      API_CONFIG.endpoints.profile.base,
      data
    )
    return this.mapBackendUserToFrontend(response)
  }

  /**
   * Changer le mot de passe de l'utilisateur connecté
   */
  async changePassword(data: PasswordChangeRequest): Promise<void> {
    await apiClient.post(
      API_CONFIG.endpoints.profile.changePassword,
      data
    )
  }

  /**
   * Récupérer tous les utilisateurs (admin seulement)
   */
  async getAllUsers(): Promise<UserManagementDTO[]> {
    const response = await apiClient.get<UserManagementDTO[]>(
      API_CONFIG.endpoints.profile.users
    )
    return response
  }

  /**
   * Mettre à jour un utilisateur (admin seulement)
   */
  async updateUser(id: string, data: ProfileUpdateRequest): Promise<UserManagementDTO> {
    const response = await apiClient.put<UserManagementDTO>(
      API_CONFIG.endpoints.profile.userById(id),
      data
    )
    return response
  }

  /**
   * Supprimer un utilisateur (admin seulement)
   */
  async deleteUser(id: string): Promise<void> {
    await apiClient.delete(API_CONFIG.endpoints.profile.userById(id))
  }

  /**
   * Mapper les données utilisateur du backend vers le format frontend
   */
  private mapBackendUserToFrontend(backendUser: any): User {
    const roleMap: Record<string, 'admin' | 'teacher' | 'student'> = {
      'ADMIN': 'admin',
      'TEACHER': 'teacher',
      'STUDENT': 'student',
      'ENSEIGNANT': 'teacher',
      'ETUDIANT': 'student',
      'ROLE_ADMIN': 'admin',
      'ROLE_TEACHER': 'teacher',
      'ROLE_STUDENT': 'student',
    }

    const role = roleMap[backendUser.role?.toUpperCase()] || 'student'
    const name = backendUser.username || backendUser.email?.split('@')[0] || 'Utilisateur'

    return {
      id: backendUser.id?.toString(),
      email: backendUser.email,
      name,
      role,
      avatar: backendUser.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${backendUser.email}`,
      createdAt: backendUser.createdAt ? new Date(backendUser.createdAt) : undefined,
    }
  }
}

export const profileService = new ProfileService()