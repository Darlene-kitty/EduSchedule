// API service pour la gestion multi-écoles
import { getAuthHeaders } from '../api-client'

export interface TeacherSchoolAssignment {
  id: number
  teacherId: number
  schoolId: number
  workingDays: DayOfWeek[]
  startTime?: string
  endTime?: string
  travelTimeMinutes: number
  maxHoursPerDay: number
  maxHoursPerWeek: number
  priority: number
  contractType: ContractType
  effectiveFrom: string
  effectiveTo?: string
  notes?: string
  active: boolean
  createdAt: string
  updatedAt: string
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

export enum ContractType {
  PERMANENT = 'PERMANENT',
  TEMPORARY = 'TEMPORARY',
  PART_TIME = 'PART_TIME',
  HOURLY = 'HOURLY',
  SUBSTITUTE = 'SUBSTITUTE'
}

export interface CreateAssignmentRequest {
  teacherId: number
  schoolId: number
  workingDays: DayOfWeek[]
  startTime?: string
  endTime?: string
  travelTimeMinutes?: number
  maxHoursPerDay?: number
  maxHoursPerWeek?: number
  priority?: number
  contractType: ContractType
  effectiveFrom: string
  effectiveTo?: string
  notes?: string
}

export interface InterSchoolConflict {
  scheduleId: number
  teacherId: number
  schoolId: number
  schoolName?: string
  conflictStartTime: string
  conflictEndTime: string
  courseName?: string
  roomName?: string
  travelTimeMinutes: number
  requiredArrivalTime: string
  conflictType: string
  severity: string
  description?: string
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export const multiSchoolApi = {
  // Créer une nouvelle assignation
  async createAssignment(assignmentData: CreateAssignmentRequest): Promise<TeacherSchoolAssignment> {
    const response = await fetch(`${API_BASE_URL}/api/multi-school/assignments`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(assignmentData),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les assignations d'un enseignant
  async getTeacherAssignments(teacherId: number): Promise<TeacherSchoolAssignment[]> {
    const response = await fetch(`${API_BASE_URL}/api/multi-school/assignments/teacher/${teacherId}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les enseignants multi-écoles
  async getMultiSchoolTeachers(): Promise<number[]> {
    const response = await fetch(`${API_BASE_URL}/api/multi-school/teachers/multi-school`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Vérifier si un enseignant travaille dans plusieurs écoles
  async isMultiSchoolTeacher(teacherId: number): Promise<boolean> {
    const response = await fetch(`${API_BASE_URL}/api/multi-school/teacher/${teacherId}/is-multi-school`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Calculer le temps de déplacement
  async calculateTravelTime(teacherId: number, fromSchoolId: number, toSchoolId: number): Promise<number> {
    const params = new URLSearchParams({
      teacherId: teacherId.toString(),
      fromSchoolId: fromSchoolId.toString(),
      toSchoolId: toSchoolId.toString()
    })
    
    const response = await fetch(`${API_BASE_URL}/api/multi-school/travel-time?${params}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Vérifier les conflits inter-écoles
  async checkInterSchoolConflicts(teacherId: number, startTime: string, endTime: string, schoolId: number): Promise<InterSchoolConflict[]> {
    const params = new URLSearchParams({
      teacherId: teacherId.toString(),
      startTime,
      endTime,
      schoolId: schoolId.toString()
    })
    
    const response = await fetch(`${API_BASE_URL}/api/multi-school/conflicts/check?${params}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer le total d'heures hebdomadaires
  async getTotalWeeklyHours(teacherId: number): Promise<number> {
    const response = await fetch(`${API_BASE_URL}/api/multi-school/teacher/${teacherId}/total-hours`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer l'assignation principale
  async getPrimaryAssignment(teacherId: number): Promise<TeacherSchoolAssignment> {
    const response = await fetch(`${API_BASE_URL}/api/multi-school/teacher/${teacherId}/primary-assignment`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Mettre à jour une assignation
  async updateAssignment(id: number, assignmentData: CreateAssignmentRequest): Promise<TeacherSchoolAssignment> {
    const response = await fetch(`${API_BASE_URL}/api/multi-school/assignments/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(assignmentData),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Supprimer une assignation
  async deleteAssignment(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/multi-school/assignments/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
  },
}