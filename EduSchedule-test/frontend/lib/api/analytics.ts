import { apiClient } from '../api-client'
import { API_CONFIG } from '../api-config'

export interface DashboardStats {
  totalRooms: number
  activeReservations: number
  occupancyRate: number
  efficiencyScore: number
  trends: {
    reservations: number
    occupancy: number
    efficiency: number
  }
}

export interface RoomOccupancy {
  resourceId: number
  resourceName: string
  occupancyRate: number
  totalReservations: number
  averageCapacityUsage: number
  efficiencyScore: number
  status: 'excellent' | 'good' | 'average' | 'poor'
}

export interface OptimizationSuggestion {
  type: string
  description: string
  resourceId: number
  resourceName: string
  currentEfficiency: number
  potentialImprovement: number
}

export interface OptimizationResult {
  success: boolean
  recommendedRoom?: {
    resourceId: number
    name: string
    type: string
    capacity: number
    location: string
    equipments: string[]
  }
  alternativeRooms?: Array<{
    resourceId: number
    name: string
    type: string
    capacity: number
    location: string
    equipments: string[]
  }>
  message: string
}

export interface ReservationRequest {
  startTime: string
  endTime: string
  expectedAttendees: number
  type: 'COURSE' | 'EXAM' | 'MEETING' | 'EVENT' | 'MAINTENANCE' | 'OTHER'
  requiredEquipments?: string[]
}

export interface OccupancyData {
  time: string
  amphitheater: number
  classroom: number
  lab: number
}

export interface WeeklyData {
  day: string
  reservations: number
  occupancy: number
}

export interface RoomTypeData {
  name: string
  value: number
  count: number
}

class AnalyticsService {
  /**
   * Récupère les statistiques du tableau de bord
   */
  async getDashboardStats(period: string = 'week'): Promise<DashboardStats> {
    try {
      // Utiliser l'API Gateway au lieu d'appeler directement le service
      const response = await apiClient.get<DashboardStats>(`/api/v1/analytics/dashboard-stats?period=${period}`)
      return response
    } catch (error) {
      console.error('Erreur récupération stats dashboard:', error)
      // Données simulées en cas d'erreur
      return {
        totalRooms: 26,
        activeReservations: 142,
        occupancyRate: 76,
        efficiencyScore: 82,
        trends: { reservations: 12, occupancy: 5, efficiency: 8 }
      }
    }
  }

  /**
   * Récupère les données d'occupation par salle
   */
  async getRoomOccupancy(period: string = 'week'): Promise<RoomOccupancy[]> {
    try {
      const response = await apiClient.get<RoomOccupancy[]>(`/api/v1/analytics/room-occupancy?period=${period}`)
      return response
    } catch (error) {
      console.error('Erreur récupération occupation salles:', error)
      // Données simulées en cas d'erreur
      return [
        {
          resourceId: 1,
          resourceName: "Amphithéâtre A",
          occupancyRate: 85,
          totalReservations: 24,
          averageCapacityUsage: 78,
          efficiencyScore: 88,
          status: 'excellent'
        },
        {
          resourceId: 2,
          resourceName: "Salle TP Info",
          occupancyRate: 92,
          totalReservations: 31,
          averageCapacityUsage: 85,
          efficiencyScore: 91,
          status: 'excellent'
        },
        {
          resourceId: 3,
          resourceName: "Salle 101",
          occupancyRate: 45,
          totalReservations: 12,
          averageCapacityUsage: 35,
          efficiencyScore: 42,
          status: 'poor'
        }
      ]
    }
  }

  /**
   * Récupère les suggestions d'optimisation
   */
  async getOptimizationSuggestions(startDate: string, endDate: string): Promise<OptimizationSuggestion[]> {
    try {
      const response = await apiClient.get<{ data: OptimizationSuggestion[] }>(`/reservations/optimization/optimize-usage?startDate=${startDate}&endDate=${endDate}`)
      return response.data
    } catch (error) {
      console.error('Erreur récupération suggestions:', error)
      // Données simulées en cas d'erreur
      return [
        {
          type: 'UNDERUTILIZED',
          description: 'Salle sous-utilisée - considérer une réaffectation',
          resourceId: 3,
          resourceName: 'Salle 101',
          currentEfficiency: 42,
          potentialImprovement: 25
        },
        {
          type: 'OVERUTILIZED',
          description: 'Salle sur-utilisée - considérer des alternatives',
          resourceId: 2,
          resourceName: 'Salle TP Info',
          currentEfficiency: 91,
          potentialImprovement: -15
        }
      ]
    }
  }

  /**
   * Trouve la salle optimale pour une demande
   */
  async findOptimalRoom(request: ReservationRequest): Promise<OptimizationResult> {
    try {
      const response = await apiClient.post<{ data: OptimizationResult }>('/reservations/optimization/find-optimal-room', request)
      return response.data
    } catch (error) {
      console.error('Erreur recherche salle optimale:', error)
      throw error
    }
  }

  /**
   * Calcule le score d'efficacité d'une salle
   */
  async getRoomEfficiencyScore(resourceId: number, startDate: string, endDate: string): Promise<{
    resourceId: number
    efficiencyScore: number
    rating: string
    period: { startDate: string; endDate: string }
  }> {
    try {
      const response = await apiClient.get<{ data: {
        resourceId: number
        efficiencyScore: number
        rating: string
        period: { startDate: string; endDate: string }
      } }>(`/reservations/optimization/efficiency-score/${resourceId}?startDate=${startDate}&endDate=${endDate}`)
      return response.data
    } catch (error) {
      console.error('Erreur calcul score efficacité:', error)
      throw error
    }
  }

  /**
   * Récupère les recommandations d'optimisation
   */
  async getOptimizationRecommendations(startDate: string, endDate: string): Promise<{
    period: { startDate: string; endDate: string }
    totalSuggestions: number
    suggestionsByType: Record<string, OptimizationSuggestion[]>
    summary: {
      underutilized: number
      overutilized: number
      reorganization: number
    }
  }> {
    try {
      const response = await apiClient.get<{ data: {
        period: { startDate: string; endDate: string }
        totalSuggestions: number
        suggestionsByType: Record<string, OptimizationSuggestion[]>
        summary: {
          underutilized: number
          overutilized: number
          reorganization: number
        }
      } }>(`/reservations/optimization/recommendations?startDate=${startDate}&endDate=${endDate}`)
      return response.data
    } catch (error) {
      console.error('Erreur récupération recommandations:', error)
      throw error
    }
  }

  /**
   * Récupère les données d'occupation par heure
   */
  async getHourlyOccupancy(date: string): Promise<OccupancyData[]> {
    try {
      const response = await apiClient.get<OccupancyData[]>(`/api/v1/analytics/hourly-occupancy?date=${date}`)
      return response
    } catch (error) {
      console.error('Erreur récupération occupation horaire:', error)
      // Données simulées en cas d'erreur
      return [
        { time: '08:00', amphitheater: 85, classroom: 60, lab: 40 },
        { time: '09:00', amphitheater: 95, classroom: 80, lab: 70 },
        { time: '10:00', amphitheater: 90, classroom: 85, lab: 85 },
        { time: '11:00', amphitheater: 75, classroom: 90, lab: 80 },
        { time: '12:00', amphitheater: 30, classroom: 40, lab: 20 },
        { time: '13:00', amphitheater: 20, classroom: 30, lab: 15 },
        { time: '14:00', amphitheater: 80, classroom: 75, lab: 90 },
        { time: '15:00', amphitheater: 85, classroom: 80, lab: 95 },
        { time: '16:00', amphitheater: 70, classroom: 70, lab: 85 },
        { time: '17:00', amphitheater: 50, classroom: 60, lab: 70 },
      ]
    }
  }

  /**
   * Récupère les données hebdomadaires
   */
  async getWeeklyData(startDate: string): Promise<WeeklyData[]> {
    try {
      const response = await apiClient.get<WeeklyData[]>(`/api/v1/analytics/weekly-data?startDate=${startDate}`)
      return response
    } catch (error) {
      console.error('Erreur récupération données hebdomadaires:', error)
      // Données simulées en cas d'erreur
      return [
        { day: 'Lun', reservations: 45, occupancy: 78 },
        { day: 'Mar', reservations: 52, occupancy: 82 },
        { day: 'Mer', reservations: 48, occupancy: 75 },
        { day: 'Jeu', reservations: 55, occupancy: 85 },
        { day: 'Ven', reservations: 42, occupancy: 70 },
        { day: 'Sam', reservations: 15, occupancy: 25 },
        { day: 'Dim', reservations: 8, occupancy: 15 },
      ]
    }
  }

  /**
   * Récupère la répartition par type de salle
   */
  async getRoomTypeDistribution(): Promise<RoomTypeData[]> {
    try {
      const response = await apiClient.get<RoomTypeData[]>('/api/v1/analytics/room-type-distribution')
      return response
    } catch (error) {
      console.error('Erreur récupération répartition types:', error)
      // Données simulées en cas d'erreur
      return [
        { name: 'Amphithéâtres', value: 35, count: 8 },
        { name: 'Salles de classe', value: 45, count: 12 },
        { name: 'Laboratoires', value: 20, count: 6 },
      ]
    }
  }

  /**
   * Récupère l'analyse comparative des salles
   */
  async getComparativeAnalysis(startDate: string, endDate: string): Promise<any> {
    try {
      const response = await apiClient.get<{ data: any }>(`/reservations/optimization/comparative-analysis?startDate=${startDate}&endDate=${endDate}`)
      return response.data
    } catch (error) {
      console.error('Erreur récupération analyse comparative:', error)
      throw error
    }
  }

  /**
   * Exporte les données d'analyse
   */
  async exportAnalytics(format: 'pdf' | 'excel', period: string): Promise<Blob> {
    try {
      // Pour les téléchargements de fichiers, nous devons utiliser fetch directement
      const token = localStorage.getItem('accessToken')
      const headers: HeadersInit = {}
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`
      }

      const response = await fetch(`${API_CONFIG.baseURL}/api/v1/analytics/export?format=${format}&period=${period}`, {
        method: 'GET',
        headers
      })

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }

      return await response.blob()
    } catch (error) {
      console.error('Erreur export analytics:', error)
      throw error
    }
  }
}

export const analyticsService = new AnalyticsService()