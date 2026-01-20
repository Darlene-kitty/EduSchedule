// API service pour la gestion des cours
import { apiClient } from '../api-client'

export interface Course {
  id: number
  name: string
  code: string
  type: string
  hoursPerWeek: number
  filiereId: number
  teacherId?: number
  createdAt: string
  department?: string
  level?: string
  semester?: string
}

export interface CreateCourseRequest {
  name: string
  code: string
  type: string
  hoursPerWeek: number
  filiereId: number
  teacherId?: number
  department?: string
  level?: string
  semester?: string
}

export interface UpdateCourseRequest {
  name: string
  code: string
  type: string
  hoursPerWeek: number
  filiereId: number
  teacherId?: number
  department?: string
  level?: string
  semester?: string
}

export interface CourseFilters {
  department?: string
  level?: string
  semester?: string
  teacherId?: number
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

// Headers avec authentification
const getAuthHeaders = () => {
  const token = getAuthToken()
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : '',
    'Origin': 'http://localhost:3000'
  }
}

export const coursesApi = {
  // Vérifier la santé du service
  async healthCheck(): Promise<string> {
    const response = await fetch(`${API_BASE_URL}/api/v1/courses/health`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.text()
  },

  // Récupérer tous les cours
  async getAllCourses(filters?: CourseFilters): Promise<Course[]> {
    let url = `${API_BASE_URL}/api/v1/courses`
    
    if (filters) {
      const params = new URLSearchParams()
      if (filters.department) params.append('department', filters.department)
      if (filters.level) params.append('level', filters.level)
      if (filters.semester) params.append('semester', filters.semester)
      if (filters.teacherId) params.append('teacherId', filters.teacherId.toString())
      
      if (params.toString()) {
        url += `?${params.toString()}`
      }
    }
    
    const response = await fetch(url, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer un cours par ID
  async getCourseById(id: number): Promise<Course> {
    const response = await fetch(`${API_BASE_URL}/api/v1/courses/${id}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer un cours par code
  async getCourseByCode(code: string): Promise<Course> {
    const response = await fetch(`${API_BASE_URL}/api/v1/courses/code/${code}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les cours par département
  async getCoursesByDepartment(department: string): Promise<Course[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/courses/department/${department}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les cours par enseignant
  async getCoursesByTeacher(teacherId: number): Promise<Course[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/courses/teacher/${teacherId}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Rechercher des cours
  async searchCourses(query: string): Promise<Course[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/courses/search?query=${encodeURIComponent(query)}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Créer un nouveau cours
  async createCourse(courseData: CreateCourseRequest): Promise<Course> {
    const response = await fetch(`${API_BASE_URL}/api/v1/courses`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(courseData),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Mettre à jour un cours
  async updateCourse(id: number, courseData: UpdateCourseRequest): Promise<Course> {
    const response = await fetch(`${API_BASE_URL}/api/v1/courses/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(courseData),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Supprimer un cours
  async deleteCourse(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/v1/courses/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
  },
}