/**
 * Configuration centralisée pour les appels API
 * 
 * Pour changer l'URL de l'API:
 * 1. Modifier API_BASE_URL ci-dessous
 * 2. Ou créer un fichier .env.local avec NEXT_PUBLIC_API_BASE_URL
 * 
 * Note: Les variables NEXT_PUBLIC_* sont accessibles côté client dans Next.js
 */

// Configuration par défaut (peut être surchargée via .env.local)
// Pour GitHub Codespaces, utilisez l'URL du port forwarding
const API_BASE_URL = typeof window !== 'undefined' && window.location.hostname.includes('github.dev')
  ? window.location.origin.replace('3000', '8080')
  : 'http://localhost:8080'
const API_TIMEOUT = 30000

export const API_CONFIG = {
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  endpoints: {
    // User Service (via API Gateway)
    auth: {
      login: '/api/auth/login',
      register: '/api/auth/register',
      logout: '/api/auth/logout',
      refresh: '/api/auth/refresh',
      forgotPassword: '/api/auth/forgot-password',
      resetPassword: '/api/auth/reset-password',
      verifyEmail: '/api/auth/verify-email',
    },
    users: {
      base: '/api/users',
      byId: (id: string) => `/api/users/${id}`,
      profile: '/user-service/api/users/profile',
    },
    // Resource Service
    resources: {
      base: '/resource-service/api/resources',
      byId: (id: string) => `/resource-service/api/resources/${id}`,
      salles: '/resource-service/api/salles',
      salleById: (id: string) => `/resource-service/api/salles/${id}`,
    },
    // Course Service
    courses: {
      base: '/course-service/api/courses',
      byId: (id: string) => `/course-service/api/courses/${id}`,
    },
    // Reservation Service
    reservations: {
      base: '/reservation-service/api/reservations',
      byId: (id: string) => `/reservation-service/api/reservations/${id}`,
    },
    // Scheduling Service
    schedules: {
      base: '/scheduling-service/api/schedules',
      byId: (id: string) => `/scheduling-service/api/schedules/${id}`,
      conflicts: '/scheduling-service/api/schedules/conflicts',
    },
    // Notification Service
    notifications: {
      base: '/notification-service/api/notifications',
      byId: (id: string) => `/notification-service/api/notifications/${id}`,
      markAsRead: (id: string) => `/notification-service/api/notifications/${id}/read`,
    },
    // Reporting Service
    reports: {
      base: 't/reporting-service/api/reports',
      generate: '/reporting-service/api/reports/generate',
    },
  },
} as const

export type ApiEndpoints = typeof API_CONFIG.endpoints
