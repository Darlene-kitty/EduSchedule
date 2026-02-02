// API service pour la gestion des notifications
import { getAuthHeaders } from '../api-client'

export interface Notification {
  id: number
  recipient: string
  subject: string
  message: string
  type: string
  status: string
  sentAt?: string
  createdAt: string
}

export interface CreateNotificationRequest {
  recipient: string
  subject: string
  message: string
  type?: string
}

export interface SendEmailRequest {
  to: string
  subject: string
  message: string
  type?: string
}

export interface TestEmailRequest {
  recipient?: string
  subject?: string
  message?: string
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export const notificationsApi = {
  // Récupérer toutes les notifications
  async getAllNotifications(): Promise<Notification[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/notifications`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer une notification par ID
  async getNotificationById(id: number): Promise<Notification> {
    const response = await fetch(`${API_BASE_URL}/api/v1/notifications/${id}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les notifications par destinataire
  async getNotificationsByRecipient(recipient: string): Promise<Notification[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/notifications/recipient/${encodeURIComponent(recipient)}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les notifications par statut
  async getNotificationsByStatus(status: string): Promise<Notification[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/notifications/status/${status}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Créer une nouvelle notification
  async createNotification(notificationData: CreateNotificationRequest): Promise<Notification> {
    const response = await fetch(`${API_BASE_URL}/api/v1/notifications`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(notificationData),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Envoyer une notification existante
  async sendNotification(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/v1/notifications/${id}/send`, {
      method: 'POST',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
  },

  // Envoyer un email directement
  async sendDirectEmail(emailData: SendEmailRequest): Promise<{ status: string; message: string; notificationId?: string; recipient?: string }> {
    const response = await fetch(`${API_BASE_URL}/api/v1/notifications/send`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(emailData),
    })
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: 'Erreur inconnue' }))
      throw new Error(errorData.message || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Envoyer un email de test
  async sendTestEmail(testData?: TestEmailRequest): Promise<{ status: string; message: string; notificationId?: string; recipient?: string }> {
    const response = await fetch(`${API_BASE_URL}/api/v1/notifications/test-email`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(testData || {}),
    })
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: 'Erreur inconnue' }))
      throw new Error(errorData.message || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },
}