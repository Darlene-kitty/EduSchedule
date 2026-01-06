/**
 * Service de gestion des cours
 */

import { apiClient } from '@/lib/api-client'
import { API_CONFIG } from '@/lib/api-config'

export interface Course {
  id: string
  code: string
  name: string
  description?: string
  credits?: number
  teacherId?: string
  teacherName?: string
  department?: string
  semester?: string
  createdAt?: Date
  updatedAt?: Date
}

export interface CreateCourseRequest {
  code: string
  name: string
  description?: string
  credits?: number
  teacherId?: string
  department?: string
  semester?: string
}

export interface UpdateCourseRequest {
  code?: string
  name?: string
  description?: string
  credits?: number
  teacherId?: string
  department?: string
  semester?: string
}

class CourseService {
  /**
   * Récupérer tous les cours
   */
  async getCourses(): Promise<Course[]> {
    return apiClient.get<Course[]>(API_CONFIG.endpoints.courses.base)
  }

  /**
   * Récupérer un cours par ID
   */
  async getCourseById(id: string): Promise<Course> {
    return apiClient.get<Course>(API_CONFIG.endpoints.courses.byId(id))
  }

  /**
   * Créer un nouveau cours
   */
  async createCourse(data: CreateCourseRequest): Promise<Course> {
    return apiClient.post<Course>(API_CONFIG.endpoints.courses.base, data)
  }

  /**
   * Mettre à jour un cours
   */
  async updateCourse(id: string, data: UpdateCourseRequest): Promise<Course> {
    return apiClient.put<Course>(API_CONFIG.endpoints.courses.byId(id), data)
  }

  /**
   * Supprimer un cours
   */
  async deleteCourse(id: string): Promise<void> {
    await apiClient.delete(API_CONFIG.endpoints.courses.byId(id))
  }

  /**
   * Rechercher des cours
   */
  async searchCourses(query: string): Promise<Course[]> {
    return apiClient.get<Course[]>(API_CONFIG.endpoints.courses.base, {
      params: { q: query }
    })
  }
}

export const courseService = new CourseService()
