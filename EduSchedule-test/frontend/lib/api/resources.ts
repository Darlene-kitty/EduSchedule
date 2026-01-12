// API service pour la gestion des ressources (salles)
import { apiClient } from '../api-client'

export interface Resource {
  id: number
  nom: string
  code: string
  type: string
  capacite: number
  batiment?: string
  etage?: number
  disponible: boolean
  createdAt?: string
}

export interface CreateResourceRequest {
  nom: string
  code: string
  type: string
  capacite: number
  batiment?: string
  etage?: number
  disponible?: boolean
}

export interface UpdateResourceRequest {
  nom: string
  code: string
  type: string
  capacite: number
  batiment?: string
  etage?: number
  disponible: boolean
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

// Headers avec authentification
const getAuthHeaders = () => {
  const token = apiClient.getAccessToken()
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : '',
    'Origin': 'http://localhost:3000'
  }
}

export const resourcesApi = {
  // Vérifier la santé du service
  async healthCheck(): Promise<string> {
    const response = await fetch(`${API_BASE_URL}/api/v1/salles/health`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.text()
  },

  // Récupérer toutes les ressources (salles)
  async getAllResources(): Promise<Resource[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/salles`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer les ressources disponibles
  async getAvailableResources(): Promise<Resource[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/salles/disponibles`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Récupérer une ressource par ID
  async getResourceById(id: number): Promise<Resource> {
    const response = await fetch(`${API_BASE_URL}/api/v1/salles/${id}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Créer une nouvelle ressource
  async createResource(resourceData: CreateResourceRequest): Promise<Resource> {
    const response = await fetch(`${API_BASE_URL}/api/v1/salles`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(resourceData),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Mettre à jour une ressource
  async updateResource(id: number, resourceData: UpdateResourceRequest): Promise<Resource> {
    const response = await fetch(`${API_BASE_URL}/api/v1/salles/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(resourceData),
    })
    
    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Erreur ${response.status}: ${response.statusText}`)
    }
    
    return response.json()
  },

  // Supprimer une ressource
  async deleteResource(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/v1/salles/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    })
    
    if (!response.ok) {
      throw new Error(`Erreur ${response.status}: ${response.statusText}`)
    }
  },
}