// API service pour la gestion des disponibilités des enseignants
import { getAuthHeaders } from '../api-client'

// Structure compatible avec Teacher Availability Service
export interface TeacherAvailability {
  id?: number
  teacherId: number
  teacherName?: string
  effectiveDate: string // LocalDate format: YYYY-MM-DD
  endDate?: string
  availableSlots: TimeSlot[]
  status: AvailabilityStatus
  notes?: string
  maxHoursPerDay?: number
  maxHoursPerWeek?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: number
  totalWeeklyHours?: number
  hasConflicts?: boolean
  conflictMessages?: string[]
}

export interface TimeSlot {
  id?: number
  startTime: string // LocalTime format: HH:mm
  endTime: string   // LocalTime format: HH:mm
  dayOfWeek: DayOfWeek
  isRecurring?: boolean
}

export enum DayOfWeek {
  MONDAY = 'MONDAY',
  TUESDAY = 'TUESDAY',
  WEDNESDAY = 'WEDNESDAY',
  THURSDAY = 'THURSDAY',
  FRIDAY = 'FRIDAY',
  SATURDAY = 'SATURDAY',
  SUNDAY = 'SUNDAY'
}

export enum AvailabilityStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED'
}

export enum AvailabilityType {
  AVAILABLE = 'AVAILABLE',
  PREFERRED = 'PREFERRED',
  UNAVAILABLE = 'UNAVAILABLE'
}

export interface CreateAvailabilityRequest {
  teacherId: number
  effectiveDate: string // YYYY-MM-DD
  endDate?: string
  availableSlots: TimeSlot[]
  status?: AvailabilityStatus
  notes?: string
  maxHoursPerDay?: number
  maxHoursPerWeek?: number
}

export interface CheckAvailabilityResponse {
  teacherId: number
  startTime: string
  endTime: string
  isAvailable: boolean
  checkedAt: string
  conflicts?: string[]
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export const teacherAvailabilityApi = {
  // Créer une nouvelle disponibilité
  async createAvailability(request: CreateAvailabilityRequest): Promise<TeacherAvailability> {
    const response = await fetch(`${API_BASE_URL}/api/teacher-availability`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(request),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les disponibilités d'un enseignant
  async getTeacherAvailabilities(teacherId: number): Promise<TeacherAvailability[]> {
    const response = await fetch(`${API_BASE_URL}/api/teacher-availability/teacher/${teacherId}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer la disponibilité active d'un enseignant
  async getActiveAvailability(teacherId: number, date?: string): Promise<TeacherAvailability | null> {
    let url = `${API_BASE_URL}/api/teacher-availability/teacher/${teacherId}/active`
    if (date) {
      url += `?date=${date}`
    }
    
    const response = await fetch(url, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (response.status === 404) {
      return null
    }
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les créneaux disponibles pour une date
  async getAvailableSlots(teacherId: number, date: string): Promise<TimeSlot[]> {
    const response = await fetch(`${API_BASE_URL}/api/teacher-availability/teacher/${teacherId}/slots?date=${date}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Vérifier la disponibilité d'un enseignant
  async checkAvailability(teacherId: number, startTime: string, endTime: string): Promise<CheckAvailabilityResponse> {
    const response = await fetch(`${API_BASE_URL}/api/teacher-availability/teacher/${teacherId}/check?startTime=${startTime}&endTime=${endTime}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les disponibilités dans une période
  async getAvailabilitiesInPeriod(teacherId: number, startDate: string, endDate: string): Promise<TeacherAvailability[]> {
    const response = await fetch(`${API_BASE_URL}/api/teacher-availability/teacher/${teacherId}/period?startDate=${startDate}&endDate=${endDate}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Mettre à jour une disponibilité
  async updateAvailability(id: number, request: CreateAvailabilityRequest): Promise<TeacherAvailability> {
    const response = await fetch(`${API_BASE_URL}/api/teacher-availability/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(request),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Supprimer une disponibilité
  async deleteAvailability(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/teacher-availability/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
  },

  // Ajouter un créneau à une disponibilité
  async addTimeSlot(availabilityId: number, timeSlot: TimeSlot): Promise<TeacherAvailability> {
    const response = await fetch(`${API_BASE_URL}/api/teacher-availability/${availabilityId}/slots`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(timeSlot),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Supprimer un créneau d'une disponibilité
  async removeTimeSlot(availabilityId: number, timeSlotId: number): Promise<TeacherAvailability> {
    const response = await fetch(`${API_BASE_URL}/api/teacher-availability/${availabilityId}/slots/${timeSlotId}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Vérifier les conflits
  async checkConflicts(availability: TeacherAvailability): Promise<{ hasConflicts: boolean; conflicts: string[]; checkedAt: string }> {
    const response = await fetch(`${API_BASE_URL}/api/teacher-availability/conflicts/check`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(availability),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les statistiques
  async getStatistics(): Promise<{ totalActiveAvailabilities: number; teachersWithAvailabilities: number; generatedAt: string }> {
    const response = await fetch(`${API_BASE_URL}/api/teacher-availability/stats`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },
}

// Utilitaires
export const availabilityUtils = {
  getDayOfWeekLabel(day: DayOfWeek): string {
    const labels = {
      MONDAY: 'Lundi',
      TUESDAY: 'Mardi',
      WEDNESDAY: 'Mercredi',
      THURSDAY: 'Jeudi',
      FRIDAY: 'Vendredi',
      SATURDAY: 'Samedi',
      SUNDAY: 'Dimanche'
    }
    return labels[day]
  },

  getStatusLabel(status: AvailabilityStatus): string {
    const labels = {
      ACTIVE: 'Actif',
      INACTIVE: 'Inactif',
      SUSPENDED: 'Suspendu'
    }
    return labels[status]
  },

  getStatusColor(status: AvailabilityStatus): string {
    const colors = {
      ACTIVE: 'bg-green-100 text-green-700',
      INACTIVE: 'bg-gray-100 text-gray-700',
      SUSPENDED: 'bg-yellow-100 text-yellow-700'
    }
    return colors[status]
  },

  getAvailabilityTypeLabel(type: AvailabilityType): string {
    const labels = {
      AVAILABLE: 'Disponible',
      PREFERRED: 'Préféré',
      UNAVAILABLE: 'Indisponible'
    }
    return labels[type]
  },

  getAvailabilityTypeColor(type: AvailabilityType): string {
    const colors = {
      AVAILABLE: 'bg-green-100 text-green-700',
      PREFERRED: 'bg-blue-100 text-blue-700',
      UNAVAILABLE: 'bg-red-100 text-red-700'
    }
    return colors[type]
  },

  formatTimeSlot(startTime: string, endTime: string): string {
    return `${startTime} - ${endTime}`
  },

  calculateDuration(startTime: string, endTime: string): number {
    const start = new Date(`2000-01-01T${startTime}`)
    const end = new Date(`2000-01-01T${endTime}`)
    return (end.getTime() - start.getTime()) / (1000 * 60) // minutes
  },

  // Créer un créneau d'1 heure
  createOneHourSlot(startTime: string, dayOfWeek: DayOfWeek): TimeSlot {
    const start = new Date(`2000-01-01T${startTime}`)
    const end = new Date(start.getTime() + 60 * 60 * 1000) // +1 heure
    const endTime = end.toTimeString().substring(0, 5)
    
    return {
      startTime,
      endTime,
      dayOfWeek,
      isRecurring: true
    }
  },

  // Vérifier si c'est l'heure du déjeuner (12h-14h)
  isLunchTime(startTime: string, endTime: string): boolean {
    const start = startTime.replace(':', '')
    const end = endTime.replace(':', '')
    return (start >= '1200' && start < '1400') || (end > '1200' && end <= '1400')
  },

  // Générer les créneaux d'1 heure pour une journée (8h-18h, sauf 12h-14h)
  generateDaySlots(dayOfWeek: DayOfWeek): TimeSlot[] {
    const slots: TimeSlot[] = []
    
    // Matin: 8h-12h
    for (let hour = 8; hour < 12; hour++) {
      slots.push(this.createOneHourSlot(`${hour.toString().padStart(2, '0')}:00`, dayOfWeek))
    }
    
    // Après-midi: 14h-18h
    for (let hour = 14; hour < 18; hour++) {
      slots.push(this.createOneHourSlot(`${hour.toString().padStart(2, '0')}:00`, dayOfWeek))
    }
    
    return slots
  },

  // Formater une date pour l'API (YYYY-MM-DD)
  formatDateForApi(date: Date): string {
    return date.toISOString().split('T')[0]
  },

  // Parser une date de l'API
  parseDateFromApi(dateString: string): Date {
    return new Date(dateString + 'T00:00:00')
  }
}