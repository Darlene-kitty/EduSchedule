import { apiClient } from '../api-client'

export interface Room {
  id: number
  name: string
  code?: string
  type: string
  capacity: number
  building?: string
  floor?: number
  equipment?: string[]
  available: boolean
  description?: string
  schoolId?: number
  createdAt: string
  updatedAt: string
}

export interface CreateRoomRequest {
  name: string
  code?: string
  type: string
  capacity: number
  building?: string
  floor?: number
  equipment?: string[]
  available?: boolean
  description?: string
  schoolId?: number
}

export interface RoomFilters {
  type?: string
  building?: string
  available?: boolean
  minCapacity?: number
  maxCapacity?: number
  schoolId?: number
  page?: number
  size?: number
}

class RoomsService {
  /**
   * Récupère toutes les salles
   */
  async getAllRooms(filters?: RoomFilters): Promise<Room[]> {
    try {
      const params = new URLSearchParams()
      if (filters?.type) params.append('type', filters.type)
      if (filters?.building) params.append('building', filters.building)
      if (filters?.available !== undefined) params.append('available', filters.available.toString())
      if (filters?.minCapacity) params.append('minCapacity', filters.minCapacity.toString())
      if (filters?.maxCapacity) params.append('maxCapacity', filters.maxCapacity.toString())
      if (filters?.schoolId) params.append('schoolId', filters.schoolId.toString())
      if (filters?.page !== undefined) params.append('page', filters.page.toString())
      if (filters?.size !== undefined) params.append('size', filters.size.toString())

      const queryString = params.toString()
      const url = queryString ? `/api/rooms?${queryString}` : '/api/rooms'
      
      const response = await apiClient.get<{ data: Room[] }>(url)
      return response.data || []
    } catch (error) {
      console.error('Erreur récupération salles:', error)
      // Données simulées en cas d'erreur
      return [
        {
          id: 1,
          name: "Amphithéâtre A",
          code: "AMPHI-A",
          type: "AMPHITHEATRE",
          capacity: 200,
          building: "Bâtiment A",
          floor: 0,
          equipment: ["Projecteur", "Micro", "Écran", "Climatisation"],
          available: true,
          description: "Grand amphithéâtre pour les cours magistraux",
          schoolId: 1,
          createdAt: "2024-01-01T10:00:00",
          updatedAt: "2024-01-01T10:00:00"
        },
        {
          id: 2,
          name: "Salle TP Informatique",
          code: "TP-INFO-1",
          type: "COMPUTER_LAB",
          capacity: 30,
          building: "Bâtiment B",
          floor: 1,
          equipment: ["Ordinateurs", "Projecteur", "Tableau numérique", "Réseau"],
          available: false,
          description: "Salle équipée de 30 postes informatiques",
          schoolId: 1,
          createdAt: "2024-01-01T10:00:00",
          updatedAt: "2024-01-01T10:00:00"
        },
        {
          id: 3,
          name: "Salle de classe 101",
          code: "CLASSE-101",
          type: "CLASSROOM",
          capacity: 40,
          building: "Bâtiment A",
          floor: 1,
          equipment: ["Tableau", "Projecteur"],
          available: true,
          description: "Salle de classe standard",
          schoolId: 1,
          createdAt: "2024-01-01T10:00:00",
          updatedAt: "2024-01-01T10:00:00"
        },
        {
          id: 4,
          name: "Laboratoire de Chimie",
          code: "LAB-CHIMIE",
          type: "LABORATORY",
          capacity: 25,
          building: "Bâtiment C",
          floor: 0,
          equipment: ["Paillasses", "Hotte", "Lavabo", "Équipement sécurité"],
          available: true,
          description: "Laboratoire équipé pour les TP de chimie",
          schoolId: 1,
          createdAt: "2024-01-01T10:00:00",
          updatedAt: "2024-01-01T10:00:00"
        }
      ]
    }
  }

  /**
   * Récupère une salle par ID
   */
  async getRoomById(id: number): Promise<Room> {
    try {
      const response = await apiClient.get<{ data: Room }>(`/api/rooms/${id}`)
      return response.data
    } catch (error) {
      console.error('Erreur récupération salle:', error)
      throw error
    }
  }

  /**
   * Crée une nouvelle salle
   */
  async createRoom(roomData: CreateRoomRequest): Promise<Room> {
    try {
      const response = await apiClient.post<{ data: Room }>('/api/rooms', roomData)
      return response.data
    } catch (error) {
      console.error('Erreur création salle:', error)
      throw error
    }
  }

  /**
   * Met à jour une salle
   */
  async updateRoom(id: number, roomData: Partial<CreateRoomRequest>): Promise<Room> {
    try {
      const response = await apiClient.put<{ data: Room }>(`/api/rooms/${id}`, roomData)
      return response.data
    } catch (error) {
      console.error('Erreur mise à jour salle:', error)
      throw error
    }
  }

  /**
   * Supprime une salle
   */
  async deleteRoom(id: number): Promise<void> {
    try {
      await apiClient.delete(`/api/rooms/${id}`)
    } catch (error) {
      console.error('Erreur suppression salle:', error)
      throw error
    }
  }

  /**
   * Récupère les salles par type
   */
  async getRoomsByType(type: string): Promise<Room[]> {
    try {
      const response = await apiClient.get<{ data: Room[] }>(`/api/rooms/type/${type}`)
      return response.data || []
    } catch (error) {
      console.error('Erreur récupération salles par type:', error)
      throw error
    }
  }

  /**
   * Récupère les salles par bâtiment
   */
  async getRoomsByBuilding(building: string): Promise<Room[]> {
    try {
      const response = await apiClient.get<{ data: Room[] }>(`/api/rooms/building/${building}`)
      return response.data || []
    } catch (error) {
      console.error('Erreur récupération salles par bâtiment:', error)
      throw error
    }
  }

  /**
   * Récupère les salles disponibles
   */
  async getAvailableRooms(): Promise<Room[]> {
    try {
      const response = await apiClient.get<{ data: Room[] }>('/api/rooms/available')
      return response.data || []
    } catch (error) {
      console.error('Erreur récupération salles disponibles:', error)
      throw error
    }
  }

  /**
   * Vérifie la disponibilité d'une salle
   */
  async checkRoomAvailability(roomId: number, startTime: string, endTime: string): Promise<boolean> {
    try {
      const response = await apiClient.get<{ data: { available: boolean } }>(
        `/api/rooms/${roomId}/availability?startTime=${startTime}&endTime=${endTime}`
      )
      return response.data.available
    } catch (error) {
      console.error('Erreur vérification disponibilité:', error)
      throw error
    }
  }

  /**
   * Récupère les créneaux disponibles d'une salle
   */
  async getRoomAvailableSlots(roomId: number, date: string): Promise<any[]> {
    try {
      const response = await apiClient.get<{ data: any[] }>(`/api/rooms/${roomId}/available-slots?date=${date}`)
      return response.data || []
    } catch (error) {
      console.error('Erreur récupération créneaux disponibles:', error)
      throw error
    }
  }

  /**
   * Récupère les réservations d'une salle
   */
  async getRoomReservations(roomId: number, startDate?: string, endDate?: string): Promise<any[]> {
    try {
      const params = new URLSearchParams()
      if (startDate) params.append('startDate', startDate)
      if (endDate) params.append('endDate', endDate)
      
      const queryString = params.toString()
      const url = queryString ? `/api/rooms/${roomId}/reservations?${queryString}` : `/api/rooms/${roomId}/reservations`
      
      const response = await apiClient.get<{ data: any[] }>(url)
      return response.data || []
    } catch (error) {
      console.error('Erreur récupération réservations salle:', error)
      throw error
    }
  }

  /**
   * Récupère les statistiques d'utilisation d'une salle
   */
  async getRoomUsageStatistics(roomId: number, period: string = 'month'): Promise<any> {
    try {
      const response = await apiClient.get<{ data: any }>(`/api/rooms/${roomId}/statistics?period=${period}`)
      return response.data
    } catch (error) {
      console.error('Erreur récupération statistiques salle:', error)
      throw error
    }
  }

  /**
   * Récupère les statistiques globales des salles
   */
  async getRoomsStatistics(): Promise<any> {
    try {
      const response = await apiClient.get<{ data: any }>('/api/rooms/statistics')
      return response.data
    } catch (error) {
      console.error('Erreur récupération statistiques salles:', error)
      throw error
    }
  }

  /**
   * Recherche des salles selon des critères
   */
  async searchRooms(criteria: {
    capacity?: number
    type?: string
    equipment?: string[]
    startTime?: string
    endTime?: string
  }): Promise<Room[]> {
    try {
      const response = await apiClient.post<{ data: Room[] }>('/api/rooms/search', criteria)
      return response.data || []
    } catch (error) {
      console.error('Erreur recherche salles:', error)
      throw error
    }
  }
}

export const roomsApi = new RoomsService()
export default roomsApi