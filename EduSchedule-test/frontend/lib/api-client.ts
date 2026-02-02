/**
 * Client API centralisé avec gestion des tokens et des erreurs
 */

import { API_CONFIG } from './api-config'
import { DEBUG } from './debug-utils'

export class ApiError extends Error {
  constructor(
    public status: number,
    public message: string,
    public data?: any
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

interface RequestOptions extends RequestInit {
  requiresAuth?: boolean
  params?: Record<string, string | number | boolean>
}

class ApiClient {
  private baseURL: string
  private timeout: number

  constructor() {
    this.baseURL = API_CONFIG.baseURL
    this.timeout = API_CONFIG.timeout
  }

  private getToken(): string | null {
    if (typeof window === 'undefined') return null
    return localStorage.getItem('accessToken')
  }

  private setToken(token: string): void {
    if (typeof window === 'undefined') return
    localStorage.setItem('accessToken', token)
  }

  private removeToken(): void {
    if (typeof window === 'undefined') return
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  private async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      let errorMessage = `HTTP ${response.status}: ${response.statusText}`
      let errorData = null

      try {
        errorData = await response.json()
        errorMessage = errorData.message || errorData.error || errorMessage
      } catch {
        // Si la réponse n'est pas JSON, utiliser le message par défaut
      }

      throw new ApiError(response.status, errorMessage, errorData)
    }

    // Gérer les réponses vides (204 No Content)
    if (response.status === 204) {
      return {} as T
    }

    try {
      return await response.json()
    } catch {
      return {} as T
    }
  }

  private buildURL(endpoint: string, params?: Record<string, string | number | boolean>): string {
    const url = new URL(endpoint, this.baseURL)
    
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        url.searchParams.append(key, String(value))
      })
    }

    return url.toString()
  }

  async request<T>(
    endpoint: string,
    options: RequestOptions = {}
  ): Promise<T> {
    const { requiresAuth = true, params, ...fetchOptions } = options

    let requestHeaders: HeadersInit = {
      'Content-Type': 'application/json',
      ...fetchOptions.headers,
    }

    if (requiresAuth) {
      const token = this.getToken()
      if (token) {
        requestHeaders = {
          ...requestHeaders,
          'Authorization': `Bearer ${token}`
        }
      }
    }

    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), this.timeout)

    try {
      const fullUrl = this.buildURL(endpoint, params)
      DEBUG.api(fetchOptions.method || 'GET', endpoint, fetchOptions.body)
      
      const response = await fetch(fullUrl, {
        ...fetchOptions,
        headers: requestHeaders,
        signal: controller.signal,
      })

      DEBUG.cors(fullUrl, response.headers)
      clearTimeout(timeoutId)
      
      const result = await this.handleResponse<T>(response)
      DEBUG.response(endpoint, response.status, result)
      
      return result
    } catch (error) {
      clearTimeout(timeoutId)

      if (error instanceof ApiError) {
        throw error
      }

      if (error instanceof Error) {
        if (error.name === 'AbortError') {
          throw new ApiError(408, 'Request timeout')
        }
        throw new ApiError(0, error.message)
      }

      throw new ApiError(0, 'Unknown error occurred')
    }
  }

  async get<T>(endpoint: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, { ...options, method: 'GET' })
  }

  async post<T>(endpoint: string, data?: any, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, {
      ...options,
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  async put<T>(endpoint: string, data?: any, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, {
      ...options,
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  async patch<T>(endpoint: string, data?: any, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, {
      ...options,
      method: 'PATCH',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  async delete<T>(endpoint: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, { ...options, method: 'DELETE' })
  }

  // Méthodes utilitaires pour la gestion des tokens
  saveTokens(accessToken: string, refreshToken?: string): void {
    this.setToken(accessToken)
    if (refreshToken && typeof window !== 'undefined') {
      localStorage.setItem('refreshToken', refreshToken)
    }
  }

  clearTokens(): void {
    this.removeToken()
  }

  getAccessToken(): string | null {
    return this.getToken()
  }
}

export const apiClient = new ApiClient()

// Helper function pour obtenir les headers avec authentification
export function getAuthHeaders(): HeadersInit {
  const token = localStorage.getItem('accessToken')
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  }
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  
  return headers
}
