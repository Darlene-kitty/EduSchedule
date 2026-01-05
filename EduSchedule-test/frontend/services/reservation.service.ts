/**
 * Service de gestion des réservations
 */

import { apiClient } from '@/lib/api-client'
import { API_CONFIG } from '@/lib/api-config'

export interface Reservation {
  id: string
  resourceId: string
  resourceName?: string
  userId: string
  userName?: string
  startTime: Date
  endTime: Date
  purpose?: string
  status: 'pending' | 'confirmed' | 'cancelled'
  createdAt?: Date
  updatedAt?: Date
}

export interface CreateReservationRequest {
  resourceId: string
  startTime: string
  endTime: string
  purpose?: string
}

export interface UpdateReservationRequest {
  startTime?: string
  endTime?: string
  purpose?: string
  status?: 'pending' | 'confirmed' | 'cancelled'
}

class ReservationService {
  /**
   * Récupérer toutes les réservations
   */
  async getReservations(): Promise<Reservation[]> {
    const response = await apiClient.get<any[]>(API_CONFIG.endpoints.reservations.base)
    return response.map(r => this.mapBackendReservationToFrontend(r))
  }

  /**
   * Récupérer une réservation par ID
   */
  async getReservationById(id: string): Promise<Reservation> {
    const response = await apiClient.get<any>(API_CONFIG.endpoints.reservations.byId(id))
    return this.mapBackendReservationToFrontend(response)
  }

  /**
   * Créer une nouvelle réservation
   */
  async createReservation(data: CreateReservationRequest): Promise<Reservation> {
    const response = await apiClient.post<any>(API_CONFIG.endpoints.reservations.base, data)
    return this.mapBackendReservationToFrontend(response)
  }

  /**
   * Mettre à jour une réservation
   */
  async updateReservation(id: string, data: UpdateReservationRequest): Promise<Reservation> {
    const response = await apiClient.put<any>(API_CONFIG.endpoints.reservations.byId(id), data)
    return this.mapBackendReservationToFrontend(response)
  }

  /**
   * Supprimer une réservation
   */
  async deleteReservation(id: string): Promise<void> {
    await apiClient.delete(API_CONFIG.endpoints.reservations.byId(id))
  }

  /**
   * Récupérer les réservations d'un utilisateur
   */
  async getUserReservations(userId: string): Promise<Reservation[]> {
    const response = await apiClient.get<any[]>(API_CONFIG.endpoints.reservations.base, {
      params: { userId }
    })
    return response.map(r => this.mapBackendReservationToFrontend(r))
  }

  /**
   * Récupérer les réservations d'une ressource
   */
  async getResourceReservations(resourceId: string): Promise<Reservation[]> {
    const response = await apiClient.get<any[]>(API_CONFIG.endpoints.reservations.base, {
      params: { resourceId }
    })
    return response.map(r => this.mapBackendReservationToFrontend(r))
  }

  /**
   * Mapper les données réservation du backend vers le format frontend
   */
  private mapBackendReservationToFrontend(backendReservation: any): Reservation {
    return {
      id: backendReservation.id,
      resourceId: backendReservation.resourceId,
      resourceName: backendReservation.resourceName,
      userId: backendReservation.userId,
      userName: backendReservation.userName,
      startTime: new Date(backendReservation.startTime),
      endTime: new Date(backendReservation.endTime),
      purpose: backendReservation.purpose,
      status: backendReservation.status || 'pending',
      createdAt: backendReservation.createdAt ? new Date(backendReservation.createdAt) : undefined,
      updatedAt: backendReservation.updatedAt ? new Date(backendReservation.updatedAt) : undefined,
    }
  }
}

export const reservationService = new ReservationService()
