/**
 * Service d'authentification - Connexion avec le backend user-service
 */

import { apiClient } from '@/lib/api-client'
import { API_CONFIG } from '@/lib/api-config'
import type { User, UserRole } from '@/contexts/auth-context'

export interface LoginRequest {
  username: string  // Backend attend 'username', pas 'email'
  password: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  type: string
  userId: number
  username: string
  email: string
  role: string
}

export interface RegisterRequest {
  username: string  // Backend attend 'username'
  email: string
  password: string
  role: string
}

export interface UserDTO {
  id: number
  username: string
  email: string
  role: string
  enabled: boolean
  createdAt?: string
  updatedAt?: string
}

export interface ForgotPasswordRequest {
  email: string
}

export interface ResetPasswordRequest {
  token: string
  newPassword: string
}

export interface VerifyEmailRequest {
  token: string
}

class AuthService {
  /**
   * Connexion utilisateur
   */
  async login(credentials: LoginRequest): Promise<User> {
    const response = await apiClient.post<LoginResponse>(
      API_CONFIG.endpoints.auth.login,
      credentials,
      { requiresAuth: false }
    )

    // Sauvegarder les tokens
    apiClient.saveTokens(response.token, response.refreshToken)

    // Transformer la réponse backend en format User du frontend
    return this.mapBackendUserToFrontend({
      id: response.userId.toString(),
      email: response.email,
      username: response.username,
      role: response.role
    })
  }

  /**
   * Inscription utilisateur
   */
  async register(data: RegisterRequest): Promise<User> {
    const response = await apiClient.post<UserDTO>(
      API_CONFIG.endpoints.auth.register,
      data,
      { requiresAuth: false }
    )

    // Transformer la réponse backend en format User du frontend
    return this.mapBackendUserToFrontend(response)
  }

  /**
   * Déconnexion utilisateur
   */
  async logout(): Promise<void> {
    try {
      await apiClient.post(API_CONFIG.endpoints.auth.logout)
    } finally {
      // Toujours nettoyer les tokens localement
      apiClient.clearTokens()
    }
  }

  /**
   * Demande de réinitialisation de mot de passe
   */
  async forgotPassword(email: string): Promise<void> {
    await apiClient.post(
      API_CONFIG.endpoints.auth.forgotPassword,
      { email },
      { requiresAuth: false }
    )
  }

  /**
   * Réinitialisation du mot de passe
   */
  async resetPassword(token: string, newPassword: string): Promise<void> {
    await apiClient.post(
      API_CONFIG.endpoints.auth.resetPassword,
      { token, newPassword },
      { requiresAuth: false }
    )
  }

  /**
   * Vérification de l'email
   */
  async verifyEmail(token: string): Promise<void> {
    await apiClient.post(
      API_CONFIG.endpoints.auth.verifyEmail,
      { token },
      { requiresAuth: false }
    )
  }

  /**
   * Récupérer le profil de l'utilisateur connecté
   */
  async getProfile(): Promise<User> {
    const response = await apiClient.get<any>(API_CONFIG.endpoints.users.me)
    return this.mapBackendUserToFrontend(response)
  }

  /**
   * Rafraîchir le token d'accès
   */
  async refreshToken(): Promise<string> {
    const refreshToken = typeof window !== 'undefined' 
      ? localStorage.getItem('refreshToken') 
      : null

    if (!refreshToken) {
      throw new Error('No refresh token available')
    }

    const response = await apiClient.post<{ token: string; refreshToken: string }>(
      API_CONFIG.endpoints.auth.refresh,
      { refreshToken },
      { requiresAuth: false }
    )

    apiClient.saveTokens(response.token, response.refreshToken)
    return response.token
  }

  /**
   * Mapper les données utilisateur du backend vers le format frontend
   */
  private mapBackendUserToFrontend(backendUser: any): User {
    const roleMap: Record<string, UserRole> = {
      'ADMIN': 'admin',
      'TEACHER': 'teacher',
      'ENSEIGNANT': 'teacher',
      'ROLE_ADMIN': 'admin',
      'ROLE_TEACHER': 'teacher',
    }

    const role = roleMap[backendUser.role?.toUpperCase()] || 'teacher'
    const name = backendUser.username || backendUser.email

    return {
      id: backendUser.id?.toString() || backendUser.userId?.toString(),
      email: backendUser.email,
      name,
      role,
      avatar: backendUser.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${backendUser.email}`,
      createdAt: backendUser.createdAt ? new Date(backendUser.createdAt) : undefined,
    }
  }
}

export const authService = new AuthService()
