// API service pour la gestion des réservations
import { apiClient } from '../api-client'

export interface Reservation {
  id: number
  resourceId: number
  userId: number
  startDateTime: string
  endDateTime: string
  purpose: string
  status: ReservationStatus
  type: ReservationType
  approvedBy?: number
  approvedAt?: string
  cancelledBy?: number
  cancelledAt?: string
  cancellationReason?: string
  createdAt: string
  updatedAt: string
}

export enum ReservationStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  CANCELLED = 'CANCELLED',
  REJECTED = 'REJECTED'
}

export enum ReservationType {
  COURSE = 'COURSE',
  MEETING = 'MEETING',
  EVENT = 'EVENT',
  MAINTENANCE = 'MAINTENANCE'
}

export interface CreateReservationRequest {
  resourceId: number
  userId: number
  startDateTime: string
  endDateTime: string
  purpose: string
  type: ReservationType
}

export interface UpdateReservationRequest {
  resourceId: number
  startDateTime: string
  endDateTime: string
  purpose: string
  type: ReservationType
}

export interface ConflictCheckRequest {
  resourceId: number
  startDateTime: string
  endDateTime: string
  excludeReservationId?: number
}

export interface ReservationFilters {
  resourceId?: number
  userId?: number
  status?: ReservationStatus
  type?: ReservationType
  startDate?: string
  endDate?: string
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

// Headers avec authentification
const getAuthHeaders = () => {
  const token = apiClient.getAccessToken()
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : '',
    'Origin': 'http://localhost:3000'
  }
}

export const reservationsApi = {
  // Créer une nouvelle réservation
  async createReservation(reservationData: CreateReservationRequest): Promise<Reservation> {
    const response = await fetch(`${API_BASE_URL}/api/reservations`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(reservationData),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer une réservation par ID
  async getReservationById(id: number): Promise<Reservation> {
    const response = await fetch(`${API_BASE_URL}/api/reservations/${id}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Mettre à jour une réservation
  async updateReservation(id: number, reservationData: UpdateReservationRequest): Promise<Reservation> {
    const response = await fetch(`${API_BASE_URL}/api/reservations/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(reservationData),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Approuver une réservation
  async approveReservation(id: number, approvedBy: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/reservations/${id}/approve?approvedBy=${approvedBy}`, {
      method: 'POST',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
  },

  // Annuler une réservation
  async cancelReservation(id: number, cancelledBy: number, reason?: string): Promise<void> {
    let url = `${API_BASE_URL}/api/reservations/${id}/cancel?cancelledBy=${cancelledBy}`
    if (reason) {
      url += `&reason=${encodeURIComponent(reason)}`
    }
    
    const response = await fetch(url, {
      method: 'POST',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
  },

  // Récupérer les réservations par utilisateur
  async getReservationsByUser(userId: number): Promise<Reservation[]> {
    const response = await fetch(`${API_BASE_URL}/api/reservations/user/${userId}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les réservations par ressource
  async getReservationsByResource(resourceId: number): Promise<Reservation[]> {
    const response = await fetch(`${API_BASE_URL}/api/reservations/resource/${resourceId}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les réservations par plage de dates
  async getReservationsByDateRange(startDate: string, endDate: string): Promise<Reservation[]> {
    const response = await fetch(`${API_BASE_URL}/api/reservations/date-range?startDate=${startDate}&endDate=${endDate}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Rechercher des réservations avec filtres
  async searchReservations(filters: ReservationFilters): Promise<Reservation[]> {
    const params = new URLSearchParams()
    
    if (filters.resourceId) params.append('resourceId', filters.resourceId.toString())
    if (filters.userId) params.append('userId', filters.userId.toString())
    if (filters.status) params.append('status', filters.status)
    if (filters.type) params.append('type', filters.type)
    if (filters.startDate) params.append('startDate', filters.startDate)
    if (filters.endDate) params.append('endDate', filters.endDate)
    
    const response = await fetch(`${API_BASE_URL}/api/reservations/search?${params.toString()}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    const result = await response.json()
    return result.content || result // Gérer les réponses paginées et non paginées
  },

  // Récupérer les réservations en attente
  async getPendingReservations(): Promise<Reservation[]> {
    const response = await fetch(`${API_BASE_URL}/api/reservations/pending`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Vérifier les conflits de réservation
  async checkConflicts(conflictData: ConflictCheckRequest): Promise<Reservation[]> {
    const response = await fetch(`${API_BASE_URL}/api/reservations/check-conflicts`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(conflictData),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },
}