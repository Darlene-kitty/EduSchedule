/**
 * Service d'authentification - Connexion avec le backend user-service
 */

import { apiClient } from '@/lib/api-client'
import { API_CONFIG } from '@/lib/api-config'
import type { User, UserRole } from '@/contexts/auth-context'

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  user: {
    id: string
    email: string
    nom: string
    prenom: string
    role: string
  }
}

export interface RegisterRequest {
  email: string
  password: string
  nom: string
  prenom: string
  role: string
}

export interface RegisterResponse {
  id: string
  email: string
  nom: string
  prenom: string
  role: string
  message: string
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
    apiClient.saveTokens(response.accessToken, response.refreshToken)

    // Transformer la réponse backend en format User du frontend
    return this.mapBackendUserToFrontend(response.user)
  }

  /**
   * Inscription utilisateur
   */
  async register(data: RegisterRequest): Promise<User> {
    const response = await apiClient.post<RegisterResponse>(
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
    const response = await apiClient.get<any>(API_CONFIG.endpoints.users.profile)
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

    const response = await apiClient.post<{ accessToken: string; refreshToken: string }>(
      API_CONFIG.endpoints.auth.refresh,
      { refreshToken },
      { requiresAuth: false }
    )

    apiClient.saveTokens(response.accessToken, response.refreshToken)
    return response.accessToken
  }

  /**
   * Mapper les données utilisateur du backend vers le format frontend
   */
  private mapBackendUserToFrontend(backendUser: any): User {
    const roleMap: Record<string, UserRole> = {
      'ADMIN': 'admin',
      'TEACHER': 'teacher',
      'ENSEIGNANT': 'teacher',
    }

    const role = roleMap[backendUser.role?.toUpperCase()] || 'teacher'
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

export const authService = new AuthService()
