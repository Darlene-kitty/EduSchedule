/**
 * Service de gestion des emplois du temps
 */

import { apiClient } from '@/lib/api-client'
import { API_CONFIG } from '@/lib/api-config'

export interface Schedule {
  id: string
  courseId: string
  courseName?: string
  teacherId?: string
  teacherName?: string
  roomId?: string
  roomName?: string
  dayOfWeek: number // 0-6 (Dimanche-Samedi)
  startTime: string // Format HH:mm
  endTime: string // Format HH:mm
  startDate?: Date
  endDate?: Date
  recurrence?: 'weekly' | 'biweekly' | 'monthly'
  createdAt?: Date
  updatedAt?: Date
}

export interface CreateScheduleRequest {
  courseId: string
  teacherId?: string
  roomId?: string
  dayOfWeek: number
  startTime: string
  endTime: string
  startDate?: string
  endDate?: string
  recurrence?: 'weekly' | 'biweekly' | 'monthly'
}

export interface UpdateScheduleRequest {
  courseId?: string
  teacherId?: string
  roomId?: string
  dayOfWeek?: number
  startTime?: string
  endTime?: string
  startDate?: string
  endDate?: string
  recurrence?: 'weekly' | 'biweekly' | 'monthly'
}

export interface Conflict {
  id: string
  type: 'room' | 'teacher' | 'student'
  scheduleId1: string
  scheduleId2: string
  description: string
  severity: 'low' | 'medium' | 'high'
  resolved: boolean
  createdAt?: Date
}

class ScheduleService {
  /**
   * Récupérer tous les emplois du temps
   */
  async getSchedules(): Promise<Schedule[]> {
    const response = await apiClient.get<any[]>(API_CONFIG.endpoints.schedules.base)
    return response.map(s => this.mapBackendScheduleToFrontend(s))
  }

  /**
   * Récupérer un emploi du temps par ID
   */
  async getScheduleById(id: string): Promise<Schedule> {
    const response = await apiClient.get<any>(API_CONFIG.endpoints.schedules.byId(id))
    return this.mapBackendScheduleToFrontend(response)
  }

  /**
   * Créer un nouvel emploi du temps
   */
  async createSchedule(data: CreateScheduleRequest): Promise<Schedule> {
    const response = await apiClient.post<any>(API_CONFIG.endpoints.schedules.base, data)
    return this.mapBackendScheduleToFrontend(response)
  }

  /**
   * Mettre à jour un emploi du temps
   */
  async updateSchedule(id: string, data: UpdateScheduleRequest): Promise<Schedule> {
    const response = await apiClient.put<any>(API_CONFIG.endpoints.schedules.byId(id), data)
    return this.mapBackendScheduleToFrontend(response)
  }

  /**
   * Supprimer un emploi du temps
   */
  async deleteSchedule(id: string): Promise<void> {
    await apiClient.delete(API_CONFIG.endpoints.schedules.byId(id))
  }

  /**
   * Récupérer les conflits d'emploi du temps
   */
  async getConflicts(): Promise<Conflict[]> {
    return apiClient.get<Conflict[]>(API_CONFIG.endpoints.schedules.conflicts)
  }

  /**
   * Résoudre un conflit
   */
  async resolveConflict(conflictId: string, resolution: any): Promise<void> {
    await apiClient.post(`${API_CONFIG.endpoints.schedules.conflicts}/${conflictId}/resolve`, resolution)
  }

  /**
   * Mapper les données emploi du temps du backend vers le format frontend
   */
  private mapBackendScheduleToFrontend(backendSchedule: any): Schedule {
    return {
      id: backendSchedule.id,
      courseId: backendSchedule.courseId,
      courseName: backendSchedule.courseName,
      teacherId: backendSchedule.teacherId,
      teacherName: backendSchedule.teacherName,
      roomId: backendSchedule.roomId,
      roomName: backendSchedule.roomName,
      dayOfWeek: backendSchedule.dayOfWeek,
      startTime: backendSchedule.startTime,
      endTime: backendSchedule.endTime,
      startDate: backendSchedule.startDate ? new Date(backendSchedule.startDate) : undefined,
      endDate: backendSchedule.endDate ? new Date(backendSchedule.endDate) : undefined,
      recurrence: backendSchedule.recurrence,
      createdAt: backendSchedule.createdAt ? new Date(backendSchedule.createdAt) : undefined,
      updatedAt: backendSchedule.updatedAt ? new Date(backendSchedule.updatedAt) : undefined,
    }
  }
}

export const scheduleService = new ScheduleService()
