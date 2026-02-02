/**
 * Service de gestion des ressources (salles, équipements, etc.)
 */

import { apiClient } from '@/lib/api-client'
import { API_CONFIG } from '@/lib/api-config'

export interface Resource {
  id: string
  name: string
  type: 'salle' | 'equipment' | 'other'
  capacity?: number
  location?: string
  description?: string
  available: boolean
  createdAt?: Date
  updatedAt?: Date
}

export interface Salle {
  id: string
  nom: string
  capacite: number
  batiment?: string
  etage?: number
  equipements?: string[]
  disponible: boolean
  createdAt?: Date
  updatedAt?: Date
}

export interface CreateResourceRequest {
  name: string
  type: string
  capacity?: number
  location?: string
  description?: string
}

export interface UpdateResourceRequest {
  name?: string
  type?: string
  capacity?: number
  location?: string
  description?: string
  available?: boolean
}

export interface CreateSalleRequest {
  nom: string
  capacite: number
  batiment?: string
  etage?: number
  equipements?: string[]
}

export interface UpdateSalleRequest {
  nom?: string
  capacite?: number
  batiment?: string
  etage?: number
  equipements?: string[]
  disponible?: boolean
}

class ResourceService {
  /**
   * Récupérer toutes les ressources
   */
  async getResources(): Promise<Resource[]> {
    const response = await apiClient.get<any[]>(API_CONFIG.endpoints.resources.base)
    return response.map(r => this.mapBackendResourceToFrontend(r))
  }

  /**
   * Récupérer une ressource par ID
   */
  async getResourceById(id: string): Promise<Resource> {
    const response = await apiClient.get<any>(API_CONFIG.endpoints.resources.byId(id))
    return this.mapBackendResourceToFrontend(response)
  }

  /**
   * Créer une nouvelle ressource
   */
  async createResource(data: CreateResourceRequest): Promise<Resource> {
    const response = await apiClient.post<any>(API_CONFIG.endpoints.resources.base, data)
    return this.mapBackendResourceToFrontend(response)
  }

  /**
   * Mettre à jour une ressource
   */
  async updateResource(id: string, data: UpdateResourceRequest): Promise<Resource> {
    const response = await apiClient.put<any>(API_CONFIG.endpoints.resources.byId(id), data)
    return this.mapBackendResourceToFrontend(response)
  }

  /**
   * Supprimer une ressource
   */
  async deleteResource(id: string): Promise<void> {
    await apiClient.delete(API_CONFIG.endpoints.resources.byId(id))
  }

  // === Gestion des Salles ===

  /**
   * Récupérer toutes les salles
   */
  async getSalles(): Promise<Salle[]> {
    return apiClient.get<Salle[]>(API_CONFIG.endpoints.resources.salles)
  }

  /**
   * Récupérer une salle par ID
   */
  async getSalleById(id: string): Promise<Salle> {
    return apiClient.get<Salle>(API_CONFIG.endpoints.resources.salleById(id))
  }

  /**
   * Créer une nouvelle salle
   */
  async createSalle(data: CreateSalleRequest): Promise<Salle> {
    return apiClient.post<Salle>(API_CONFIG.endpoints.resources.salles, data)
  }

  /**
   * Mettre à jour une salle
   */
  async updateSalle(id: string, data: UpdateSalleRequest): Promise<Salle> {
    return apiClient.put<Salle>(API_CONFIG.endpoints.resources.salleById(id), data)
  }

  /**
   * Supprimer une salle
   */
  async deleteSalle(id: string): Promise<void> {
    await apiClient.delete(API_CONFIG.endpoints.resources.salleById(id))
  }

  /**
   * Rechercher des salles disponibles
   */
  async searchAvailableSalles(minCapacity?: number): Promise<Salle[]> {
    const params: Record<string, string | number> = {}
    if (minCapacity) {
      params.minCapacity = minCapacity
    }
    params.disponible = 'true'

    return apiClient.get<Salle[]>(API_CONFIG.endpoints.resources.salles, { params })
  }

  /**
   * Mapper les données ressource du backend vers le format frontend
   */
  private mapBackendResourceToFrontend(backendResource: any): Resource {
    return {
      id: backendResource.id,
      name: backendResource.name || backendResource.nom,
      type: backendResource.type || 'other',
      capacity: backendResource.capacity || backendResource.capacite,
      location: backendResource.location || backendResource.batiment,
      description: backendResource.description,
      available: backendResource.available ?? backendResource.disponible ?? true,
      createdAt: backendResource.createdAt ? new Date(backendResource.createdAt) : undefined,
      updatedAt: backendResource.updatedAt ? new Date(backendResource.updatedAt) : undefined,
    }
  }
}

export const resourceService = new ResourceService()
