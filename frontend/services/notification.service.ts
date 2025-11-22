/**
 * Service de gestion des notifications
 */

import { apiClient } from '@/lib/api-client'
import { API_CONFIG } from '@/lib/api-config'

export interface Notification {
  id: string
  userId: string
  title: string
  message: string
  type: 'info' | 'warning' | 'error' | 'success'
  read: boolean
  createdAt: Date
}

class NotificationService {
  /**
   * Récupérer toutes les notifications de l'utilisateur connecté
   */
  async getNotifications(): Promise<Notification[]> {
    const response = await apiClient.get<any[]>(API_CONFIG.endpoints.notifications.base)
    return response.map(n => this.mapBackendNotificationToFrontend(n))
  }

  /**
   * Marquer une notification comme lue
   */
  async markAsRead(id: string): Promise<void> {
    await apiClient.patch(API_CONFIG.endpoints.notifications.markAsRead(id))
  }

  /**
   * Marquer toutes les notifications comme lues
   */
  async markAllAsRead(): Promise<void> {
    await apiClient.patch(`${API_CONFIG.endpoints.notifications.base}/mark-all-read`)
  }

  /**
   * Supprimer une notification
   */
  async deleteNotification(id: string): Promise<void> {
    await apiClient.delete(API_CONFIG.endpoints.notifications.byId(id))
  }

  /**
   * Mapper les données notification du backend vers le format frontend
   */
  private mapBackendNotificationToFrontend(backendNotification: any): Notification {
    return {
      id: backendNotification.id,
      userId: backendNotification.userId,
      title: backendNotification.title,
      message: backendNotification.message,
      type: backendNotification.type || 'info',
      read: backendNotification.read || false,
      createdAt: new Date(backendNotification.createdAt),
    }
  }
}

export const notificationService = new NotificationService()
