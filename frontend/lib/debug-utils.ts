/**
 * Utilitaires de débogage pour le développement
 */

import { API_CONFIG } from './api-config'

export const DEBUG = {
  enabled: process.env.NODE_ENV === 'development',
  
  log: (...args: any[]) => {
    if (DEBUG.enabled) {
      console.log('[DEBUG]', ...args)
    }
  },
  
  error: (...args: any[]) => {
    if (DEBUG.enabled) {
      console.error('[ERROR]', ...args)
    }
  },
  
  api: (method: string, url: string, data?: any) => {
    if (DEBUG.enabled) {
      console.group(`[API] ${method} ${url}`)
      console.log('Base URL:', API_CONFIG.baseURL)
      console.log('Full URL:', `${API_CONFIG.baseURL}${url}`)
      if (data) {
        console.log('Data:', data)
      }
      console.groupEnd()
    }
  },
  
  response: (url: string, status: number, data?: any) => {
    if (DEBUG.enabled) {
      console.group(`[RESPONSE] ${url} - ${status}`)
      if (data) {
        console.log('Data:', data)
      }
      console.groupEnd()
    }
  },
  
  cors: (url: string, headers: Headers) => {
    if (DEBUG.enabled) {
      console.group(`[CORS] ${url}`)
      console.log('Access-Control-Allow-Origin:', headers.get('Access-Control-Allow-Origin'))
      console.log('Access-Control-Allow-Methods:', headers.get('Access-Control-Allow-Methods'))
      console.log('Access-Control-Allow-Headers:', headers.get('Access-Control-Allow-Headers'))
      console.groupEnd()
    }
  },
  
  config: () => {
    if (DEBUG.enabled) {
      console.group('[CONFIG]')
      console.log('API Base URL:', API_CONFIG.baseURL)
      console.log('API Timeout:', API_CONFIG.timeout)
      console.log('Environment:', process.env.NODE_ENV)
      console.log('Window Location:', typeof window !== 'undefined' ? window.location.href : 'SSR')
      console.groupEnd()
    }
  }
}

// Exposer les utilitaires de debug dans la console du navigateur
if (typeof window !== 'undefined' && DEBUG.enabled) {
  (window as any).DEBUG = DEBUG
  console.log('💡 Utilitaires de debug disponibles: window.DEBUG')
  console.log('   - DEBUG.config() : Afficher la configuration')
  console.log('   - DEBUG.log() : Logger un message')
  console.log('   - DEBUG.api() : Logger un appel API')
}
