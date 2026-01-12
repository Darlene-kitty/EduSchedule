// API service pour la gestion des écoles
import { getAuthToken } from '../auth'

export interface School {
  id: number
  name: string
  code: string
  description?: string
  active: boolean
  createdAt: string
}

export interface CreateSchoolRequest {
  name: string
  code: string
  description?: string
  active?: boolean
}

export interface UpdateSchoolRequest {
  name: string
  code: string
  description?: string
  active: boolean
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

export const schoolsApi = {
  // Vérifier la santé du service
  async healthCheck(): Promise<string> {
    const response = await fetch(`${API_BASE_URL}/api/v1/schools/health`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.text()
  },

  // Récupérer toutes les écoles
  async getAllSchools(): Promise<School[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/schools`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les écoles actives
  async getActiveSchools(): Promise<School[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/schools/active`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer une école par ID
  async getSchoolById(id: number): Promise<School> {
    const response = await fetch(`${API_BASE_URL}/api/v1/schools/${id}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Créer une nouvelle école
  async createSchool(schoolData: CreateSchoolRequest): Promise<School> {
    const response = await fetch(`${API_BASE_URL}/api/v1/schools`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(schoolData),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Mettre à jour une école (endpoint à implémenter côté backend)
  async updateSchool(id: number, schoolData: UpdateSchoolRequest): Promise<School> {
    const response = await fetch(`${API_BASE_URL}/api/v1/schools/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(schoolData),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Supprimer une école (endpoint à implémenter côté backend)
  async deleteSchool(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/v1/schools/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
  },
}