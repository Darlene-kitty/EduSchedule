/**
 * Configuration centralisée pour les appels API
 * Les chemins correspondent aux routes définies dans l'API Gateway (port 8080)
 *
 * Routes Gateway:
 *  /api/auth/**              → user-service
 *  /api/users/**             → user-service
 *  /api/resources/**         → resource-service
 *  /api/rooms/**             → room-service
 *  /api/v1/courses/**        → course-service
 *  /api/reservations/**      → reservation-service
 *  /api/schedules/**         → scheduling-service
 *  /api/notifications/**     → notification-service
 *  /api/reports/**           → reporting-service
 *  /api/predictive-analytics/** / /api/ai/** → ai-service
 *  /api/calendar/**          → calendar-service
 *  /api/events/**            → event-service
 *  /api/teacher-availability/** → teacher-availability-service
 *  /api/schools/**           → school-service
 *  /api/maintenance/**       → maintenance-service
 *  /api/ent/**               → ent-integration-service
 */

const API_BASE_URL =
  typeof window !== 'undefined' && window.location.hostname.includes('github.dev')
    ? window.location.origin.replace(/300[01]/, '8080')
    : process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'

const API_TIMEOUT = parseInt(process.env.NEXT_PUBLIC_API_TIMEOUT || '30000', 10)

export const API_CONFIG = {
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  endpoints: {
    // ── User Service ──────────────────────────────────────────────────────────
    auth: {
      login: '/api/auth/login',
      register: '/api/auth/register',
      logout: '/api/auth/logout',
      refresh: '/api/auth/refresh',
      me: '/api/auth/me',
      forgotPassword: '/api/auth/forgot-password',
      resetPassword: '/api/auth/reset-password',
      verifyEmail: '/api/auth/verify-email',
    },
    users: {
      base: '/api/users',
      byId: (id: string) => `/api/users/${id}`,
    },
    profile: {
      base: '/api/users/me',
      changePassword: '/api/users/me/change-password',
    },

    // ── Resource Service ──────────────────────────────────────────────────────
    resources: {
      base: '/api/resources',
      byId: (id: string) => `/api/resources/${id}`,
    },

    // ── Room Service ──────────────────────────────────────────────────────────
    rooms: {
      base: '/api/rooms',
      byId: (id: string) => `/api/rooms/${id}`,
      optimization: '/api/room-optimization',
    },

    // ── Course Service ────────────────────────────────────────────────────────
    courses: {
      base: '/api/v1/courses',
      byId: (id: string) => `/api/v1/courses/${id}`,
    },

    // ── Reservation Service ───────────────────────────────────────────────────
    reservations: {
      base: '/api/reservations',
      byId: (id: string) => `/api/reservations/${id}`,
    },

    // ── Scheduling Service ────────────────────────────────────────────────────
    schedules: {
      base: '/api/schedules',
      byId: (id: string) => `/api/schedules/${id}`,
      conflicts: '/api/schedules/conflicts',
    },

    // ── Notification Service ──────────────────────────────────────────────────
    notifications: {
      base: '/api/notifications',
      byId: (id: string) => `/api/notifications/${id}`,
      markAsRead: (id: string) => `/api/notifications/${id}/read`,
    },

    // ── Reporting Service ─────────────────────────────────────────────────────
    reports: {
      base: '/api/reports',
      generate: '/api/reports/generate',
    },

    // ── AI / Analytics Service ────────────────────────────────────────────────
    analytics: {
      base: '/api/predictive-analytics',
      dashboardStats: '/api/predictive-analytics/dashboard-stats',
      roomOccupancy: '/api/predictive-analytics/room-occupancy',
      hourlyOccupancy: '/api/predictive-analytics/hourly-occupancy',
      weeklyData: '/api/predictive-analytics/weekly-data',
      roomTypeDistribution: '/api/predictive-analytics/room-type-distribution',
      export: '/api/predictive-analytics/export',
    },
    ai: {
      base: '/api/ai',
      suggest: '/api/ai/suggest',
      optimize: '/api/ai/optimize',
    },

    // ── Calendar Service ──────────────────────────────────────────────────────
    calendar: {
      base: '/api/calendar',
      byId: (id: string) => `/api/calendar/${id}`,
      sync: '/api/calendar/sync',
    },

    // ── Event Service ─────────────────────────────────────────────────────────
    events: {
      base: '/api/events',
      byId: (id: string) => `/api/events/${id}`,
    },

    // ── Teacher Availability Service ──────────────────────────────────────────
    teacherAvailability: {
      base: '/api/teacher-availability',
      byId: (id: string) => `/api/teacher-availability/${id}`,
    },

    // ── School Service ────────────────────────────────────────────────────────
    schools: {
      base: '/api/schools',
      byId: (id: string) => `/api/schools/${id}`,
    },

    // ── Maintenance Service ───────────────────────────────────────────────────
    maintenance: {
      base: '/api/maintenance',
      byId: (id: string) => `/api/maintenance/${id}`,
    },

    // ── ENT Integration Service ───────────────────────────────────────────────
    ent: {
      base: '/api/ent',
      sync: '/api/ent/sync',
      status: '/api/ent/status',
    },
  },
} as const

export type ApiEndpoints = typeof API_CONFIG.endpoints
