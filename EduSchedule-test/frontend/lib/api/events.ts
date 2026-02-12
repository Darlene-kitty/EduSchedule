import { apiClient } from '../api-client'

export interface Event {
  id: number
  title: string
  description?: string
  type: string
  startDate: string
  endDate: string
  location?: string
  maxParticipants?: number
  status: string
  organizerId?: number
  createdAt: string
  updatedAt: string
}

export interface CreateEventRequest {
  title: string
  description?: string
  type: string
  startDate: string
  endDate: string
  location?: string
  maxParticipants?: number
  status?: string
  organizerId?: number
}

export interface EventFilters {
  type?: string
  status?: string
  startDate?: string
  endDate?: string
  organizerId?: number
  page?: number
  size?: number
}

class EventsService {
  /**
   * Récupère tous les événements
   */
  async getAllEvents(filters?: EventFilters): Promise<Event[]> {
    try {
      const params = new URLSearchParams()
      if (filters?.type) params.append('type', filters.type)
      if (filters?.status) params.append('status', filters.status)
      if (filters?.startDate) params.append('startDate', filters.startDate)
      if (filters?.endDate) params.append('endDate', filters.endDate)
      if (filters?.organizerId) params.append('organizerId', filters.organizerId.toString())
      if (filters?.page !== undefined) params.append('page', filters.page.toString())
      if (filters?.size !== undefined) params.append('size', filters.size.toString())

      const queryString = params.toString()
      const url = queryString ? `/api/events?${queryString}` : '/api/events'
      
      const response = await apiClient.get<{ data: Event[] }>(url)
      return response.data || []
    } catch (error) {
      console.error('Erreur récupération événements:', error)
      // Données simulées en cas d'erreur
      return [
        {
          id: 1,
          title: "Conférence Intelligence Artificielle",
          description: "Conférence sur les dernières avancées en IA",
          type: "CONFERENCE",
          startDate: "2024-03-20T14:00:00",
          endDate: "2024-03-20T17:00:00",
          location: "Amphithéâtre A",
          maxParticipants: 200,
          status: "SCHEDULED",
          organizerId: 1,
          createdAt: "2024-03-01T10:00:00",
          updatedAt: "2024-03-01T10:00:00"
        },
        {
          id: 2,
          title: "Atelier Développement Web",
          description: "Atelier pratique sur React et Node.js",
          type: "WORKSHOP",
          startDate: "2024-03-22T09:00:00",
          endDate: "2024-03-22T12:00:00",
          location: "Salle TP Info",
          maxParticipants: 30,
          status: "SCHEDULED",
          organizerId: 2,
          createdAt: "2024-03-02T10:00:00",
          updatedAt: "2024-03-02T10:00:00"
        },
        {
          id: 3,
          title: "Séminaire Cybersécurité",
          description: "Séminaire sur les enjeux de la cybersécurité",
          type: "SEMINAR",
          startDate: "2024-03-25T10:00:00",
          endDate: "2024-03-25T12:00:00",
          location: "Salle 101",
          maxParticipants: 50,
          status: "SCHEDULED",
          organizerId: 3,
          createdAt: "2024-03-03T10:00:00",
          updatedAt: "2024-03-03T10:00:00"
        }
      ]
    }
  }

  /**
   * Récupère un événement par ID
   */
  async getEventById(id: number): Promise<Event> {
    try {
      const response = await apiClient.get<{ data: Event }>(`/api/events/${id}`)
      return response.data
    } catch (error) {
      console.error('Erreur récupération événement:', error)
      throw error
    }
  }

  /**
   * Crée un nouvel événement
   */
  async createEvent(eventData: CreateEventRequest): Promise<Event> {
    try {
      const response = await apiClient.post<{ data: Event }>('/api/events', eventData)
      return response.data
    } catch (error) {
      console.error('Erreur création événement:', error)
      throw error
    }
  }

  /**
   * Met à jour un événement
   */
  async updateEvent(id: number, eventData: Partial<CreateEventRequest>): Promise<Event> {
    try {
      const response = await apiClient.put<{ data: Event }>(`/api/events/${id}`, eventData)
      return response.data
    } catch (error) {
      console.error('Erreur mise à jour événement:', error)
      throw error
    }
  }

  /**
   * Supprime un événement
   */
  async deleteEvent(id: number): Promise<void> {
    try {
      await apiClient.delete(`/api/events/${id}`)
    } catch (error) {
      console.error('Erreur suppression événement:', error)
      throw error
    }
  }

  /**
   * Récupère les événements par organisateur
   */
  async getEventsByOrganizer(organizerId: number): Promise<Event[]> {
    try {
      const response = await apiClient.get<{ data: Event[] }>(`/api/events/organizer/${organizerId}`)
      return response.data || []
    } catch (error) {
      console.error('Erreur récupération événements organisateur:', error)
      throw error
    }
  }

  /**
   * Récupère les événements par type
   */
  async getEventsByType(type: string): Promise<Event[]> {
    try {
      const response = await apiClient.get<{ data: Event[] }>(`/api/events/type/${type}`)
      return response.data || []
    } catch (error) {
      console.error('Erreur récupération événements par type:', error)
      throw error
    }
  }

  /**
   * Récupère les événements à venir
   */
  async getUpcomingEvents(days: number = 30): Promise<Event[]> {
    try {
      const response = await apiClient.get<{ data: Event[] }>(`/api/events/upcoming?days=${days}`)
      return response.data || []
    } catch (error) {
      console.error('Erreur récupération événements à venir:', error)
      throw error
    }
  }

  /**
   * Inscrit un utilisateur à un événement
   */
  async registerForEvent(eventId: number, userId: number): Promise<void> {
    try {
      await apiClient.post(`/api/events/${eventId}/register`, { userId })
    } catch (error) {
      console.error('Erreur inscription événement:', error)
      throw error
    }
  }

  /**
   * Désinscrit un utilisateur d'un événement
   */
  async unregisterFromEvent(eventId: number, userId: number): Promise<void> {
    try {
      await apiClient.delete(`/api/events/${eventId}/register/${userId}`)
    } catch (error) {
      console.error('Erreur désinscription événement:', error)
      throw error
    }
  }

  /**
   * Récupère les participants d'un événement
   */
  async getEventParticipants(eventId: number): Promise<any[]> {
    try {
      const response = await apiClient.get<{ data: any[] }>(`/api/events/${eventId}/participants`)
      return response.data || []
    } catch (error) {
      console.error('Erreur récupération participants:', error)
      throw error
    }
  }

  /**
   * Récupère les statistiques des événements
   */
  async getEventStatistics(): Promise<any> {
    try {
      const response = await apiClient.get<{ data: any }>('/api/events/statistics')
      return response.data
    } catch (error) {
      console.error('Erreur récupération statistiques événements:', error)
      throw error
    }
  }
}

export const eventsApi = new EventsService()
export default eventsApi