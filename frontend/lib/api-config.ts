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
const API_BASE_URL = 'http://localhost:8080'
const API_TIMEOUT = 30000

export const API_CONFIG = {
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  endpoints: {
    // User Service
    auth: {
      login: '/user-service/api/auth/login',
      register: '/user-service/api/auth/register',
      logout: '/user-service/api/auth/logout',
      refresh: '/user-service/api/auth/refresh',
      forgotPassword: '/user-service/api/auth/forgot-password',
      resetPassword: '/user-service/api/auth/reset-password',
      verifyEmail: '/user-service/api/auth/verify-email',
    },
    users: {
      base: '/user-service/api/users',
      byId: (id: string) => `/user-service/api/users/${id}`,
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
      base: '/reporting-service/api/reports',
      generate: '/reporting-service/api/reports/generate',
    },
  },
} as const

export type ApiEndpoints = typeof API_CONFIG.endpoints
