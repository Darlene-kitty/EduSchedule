// API service pour la gestion des emplois du temps
import { getAuthHeaders } from '../api-client'

export interface Schedule {
  id: number
  title: string
  description?: string
  startTime: string
  endTime: string
  room?: string
  teacher?: string
  course?: string
  groupName?: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface CreateScheduleRequest {
  title: string
  description?: string
  startTime: string
  endTime: string
  room?: string
  teacher?: string
  course?: string
  groupName?: string
  status?: string
}

export interface UpdateScheduleRequest {
  title: string
  description?: string
  startTime: string
  endTime: string
  room?: string
  teacher?: string
  course?: string
  groupName?: string
  status?: string
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export const schedulesApi = {
  // Créer un nouvel emploi du temps
  async createSchedule(scheduleData: CreateScheduleRequest): Promise<Schedule> {
    const response = await fetch(`${API_BASE_URL}/api/schedules`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(scheduleData),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer tous les emplois du temps
  async getAllSchedules(): Promise<Schedule[]> {
    const response = await fetch(`${API_BASE_URL}/api/schedules`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer un emploi du temps par ID
  async getScheduleById(id: number): Promise<Schedule> {
    const response = await fetch(`${API_BASE_URL}/api/schedules/${id}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les emplois du temps par enseignant
  async getSchedulesByTeacher(teacher: string): Promise<Schedule[]> {
    const response = await fetch(`${API_BASE_URL}/api/schedules/teacher/${encodeURIComponent(teacher)}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les emplois du temps par groupe
  async getSchedulesByGroup(groupName: string): Promise<Schedule[]> {
    const response = await fetch(`${API_BASE_URL}/api/schedules/group/${encodeURIComponent(groupName)}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les emplois du temps par salle
  async getSchedulesByRoom(room: string): Promise<Schedule[]> {
    const response = await fetch(`${API_BASE_URL}/api/schedules/room/${encodeURIComponent(room)}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les emplois du temps par plage de dates
  async getSchedulesByDateRange(startDate: string, endDate: string): Promise<Schedule[]> {
    const response = await fetch(`${API_BASE_URL}/api/schedules/range?startDate=${startDate}&endDate=${endDate}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Mettre à jour un emploi du temps
  async updateSchedule(id: number, scheduleData: UpdateScheduleRequest): Promise<Schedule> {
    const response = await fetch(`${API_BASE_URL}/api/schedules/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(scheduleData),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Supprimer un emploi du temps
  async deleteSchedule(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/schedules/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
  },
}