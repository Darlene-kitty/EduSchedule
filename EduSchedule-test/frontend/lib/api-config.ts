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
  ? window.location.origin.replace(/300[01]/, '8080')
  : (process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080')
const API_TIMEOUT = parseInt(process.env.NEXT_PUBLIC_API_TIMEOUT || '30000', 10)

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
      me: '/api/auth/me',
    },
    profile: {
      base: '/api/profile',
      changePassword: '/api/profile/change-password',
      users: '/api/profile/users',
      userById: (id: string) => `/api/profile/users/${id}`,
    },
    // Resource Service
    resources: {
      base: '/api/v1/resources',
      byId: (id: string) => `/api/v1/resources/${id}`,
      salles: '/api/v1/salles',
      salleById: (id: string) => `/api/v1/salles/${id}`,
    },
    // Course Service
    courses: {
      base: '/api/v1/cours',
      byId: (id: string) => `/api/v1/cours/${id}`,
    },
    // Reservation Service
    reservations: {
      base: '/api/v1/reservations',
      byId: (id: string) => `/api/v1/reservations/${id}`,
    },
    // Scheduling Service
    schedules: {
      base: '/api/v1/emplois',
      byId: (id: string) => `/api/v1/emplois/${id}`,
      conflicts: '/api/v1/emplois/conflicts',
    },
    // Notification Service
    notifications: {
      base: '/api/v1/notifications',
      byId: (id: string) => `/api/v1/notifications/${id}`,
      markAsRead: (id: string) => `/api/v1/notifications/${id}/read`,
    },
    // Reporting Service
    reports: {
      base: '/api/v1/reports',
      generate: '/api/v1/reports/generate',
    },
    // Analytics Service
    analytics: {
      base: '/api/v1/analytics',
      dashboardStats: '/api/v1/analytics/dashboard-stats',
      roomOccupancy: '/api/v1/analytics/room-occupancy',
      hourlyOccupancy: '/api/v1/analytics/hourly-occupancy',
      weeklyData: '/api/v1/analytics/weekly-data',
      roomTypeDistribution: '/api/v1/analytics/room-type-distribution',
      export: '/api/v1/analytics/export',
    },
  },
} as const

export type ApiEndpoints = typeof API_CONFIG.endpoints
