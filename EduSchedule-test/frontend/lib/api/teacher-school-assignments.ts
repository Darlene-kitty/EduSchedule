// API service pour la gestion des affectations enseignant-école

import { getAuthHeaders } from '../api-client'

export interface TeacherSchoolAssignment {
  id?: number
  teacherId: number
  schoolId: number
  schoolName: string
  schoolAddress?: string
  workingDays: string[] // ['MONDAY', 'TUESDAY', etc.]
  startTime?: string // Format HH:mm
  endTime?: string // Format HH:mm
  travelTimeMinutes: number
  isPrimarySchool: boolean
  isActive: boolean
}

export interface CreateAssignmentRequest {
  teacherId: number
  schoolId: number
  schoolName: string
  schoolAddress?: string
  workingDays: string[]
  startTime?: string
  endTime?: string
  travelTimeMinutes?: number
  isPrimarySchool?: boolean
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export const teacherSchoolAssignmentApi = {
  // Vérifier la santé du service
  async healthCheck(): Promise<string> {
    const response = await fetch(`${API_BASE_URL}/api/v1/teacher-school-assignments/health`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return response.text()
  },

  // Créer une affectation enseignant-école
  async createAssignment(assignmentData: CreateAssignmentRequest): Promise<TeacherSchoolAssignment> {
    const response = await fetch(`${API_BASE_URL}/api/v1/teacher-school-assignments`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(assignmentData),
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return response.json()
  },

  // Récupérer toutes les écoles d'un enseignant
  async getTeacherSchools(teacherId: number): Promise<TeacherSchoolAssignment[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/teacher-school-assignments/teacher/${teacherId}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return response.json()
  },

  // Récupérer tous les enseignants d'une école
  async getSchoolTeachers(schoolId: number): Promise<TeacherSchoolAssignment[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/teacher-school-assignments/school/${schoolId}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return response.json()
  },

  // Récupérer une affectation spécifique enseignant-école
  async getTeacherSchoolAssignment(teacherId: number, schoolId: number): Promise<TeacherSchoolAssignment | null> {
    const response = await fetch(`${API_BASE_URL}/api/v1/teacher-school-assignments/teacher/${teacherId}/school/${schoolId}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (response.status === 404) {
      return null
    }
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return response.json()
  },

  // Récupérer les écoles d'un enseignant pour un jour spécifique
  async getTeacherSchoolsForDay(teacherId: number, dayOfWeek: string): Promise<TeacherSchoolAssignment[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/teacher-school-assignments/teacher/${teacherId}/day/${dayOfWeek}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return response.json()
  },

  // Récupérer l'école principale d'un enseignant
  async getPrimarySchool(teacherId: number): Promise<TeacherSchoolAssignment | null> {
    const response = await fetch(`${API_BASE_URL}/api/v1/teacher-school-assignments/teacher/${teacherId}/primary`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (response.status === 404) {
      return null
    }
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return response.json()
  },

  // Vérifier si un enseignant est affecté à une école
  async checkTeacherSchoolAssignment(teacherId: number, schoolId: number): Promise<boolean> {
    const response = await fetch(`${API_BASE_URL}/api/v1/teacher-school-assignments/teacher/${teacherId}/school/${schoolId}/check`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return response.json()
  },

  // Calculer le temps de déplacement entre deux écoles
  async calculateTravelTime(fromSchoolId: number, toSchoolId: number): Promise<number> {
    const params = new URLSearchParams({
      fromSchoolId: fromSchoolId.toString(),
      toSchoolId: toSchoolId.toString(),
    })

    const response = await fetch(`${API_BASE_URL}/api/v1/teacher-school-assignments/travel-time?${params}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return response.json()
  },

  // Mettre à jour une affectation
  async updateAssignment(id: number, assignmentData: Partial<CreateAssignmentRequest>): Promise<TeacherSchoolAssignment> {
    const response = await fetch(`${API_BASE_URL}/api/v1/teacher-school-assignments/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(assignmentData),
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return response.json()
  },

  // Définir l'école principale d'un enseignant
  async setPrimarySchool(teacherId: number, schoolId: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/v1/teacher-school-assignments/teacher/${teacherId}/primary-school/${schoolId}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
  },

  // Supprimer une affectation (désactivation)
  async deleteAssignment(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/v1/teacher-school-assignments/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
  },
}