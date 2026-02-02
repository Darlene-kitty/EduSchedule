import { ApiResponse } from './types';

export interface CalendarIntegration {
  id?: number;
  userId: string;
  provider: 'GOOGLE' | 'OUTLOOK' | 'APPLE' | 'ICAL';
  calendarId: string;
  accessToken?: string;
  refreshToken?: string;
  tokenExpiresAt?: string;
  enabled: boolean;
  syncEnabled: boolean;
  syncDirection: 'IMPORT_ONLY' | 'EXPORT_ONLY' | 'BIDIRECTIONAL';
  lastSyncAt?: string;
  syncStatus: 'PENDING' | 'SYNCING' | 'SUCCESS' | 'ERROR' | 'DISABLED';
  syncError?: string;
  createdAt?: string;
}

export interface CalendarEvent {
  id?: number;
  userId: string;
  externalEventId?: string;
  scheduleId?: number;
  reservationId?: number;
  title: string;
  description?: string;
  startTime: string;
  endTime: string;
  location?: string;
  attendees?: string;
  isAllDay: boolean;
  recurrenceRule?: string;
  syncStatus: 'PENDING' | 'SYNCED' | 'CONFLICT' | 'ERROR' | 'DELETED';
  lastSyncedAt?: string;
  syncError?: string;
  createdAt?: string;
}

export interface WeeklySchedule {
  userId: string;
  weekStart: string;
  weekEnd: string;
  dailySchedules: {
    [key: string]: CalendarEvent[];
  };
  totalEvents: number;
  totalHours: number;
  eventsByType: { [key: string]: number };
  eventsByLocation: { [key: string]: number };
}

class CalendarApi {
  private baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

  /**
   * Créer une nouvelle intégration calendrier
   */
  async createIntegration(integration: CalendarIntegration): Promise<ApiResponse<CalendarIntegration>> {
    try {
      const response = await fetch(`${this.baseUrl}/api/calendar/integrations`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(integration),
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const data = await response.json();
      return { success: true, data };
    } catch (error) {
      console.error('Erreur lors de la création de l\'intégration:', error);
      return { 
        success: false, 
        error: error instanceof Error ? error.message : 'Erreur inconnue' 
      };
    }
  }

  /**
   * Obtenir les intégrations d'un utilisateur
   */
  async getUserIntegrations(userId: string): Promise<ApiResponse<CalendarIntegration[]>> {
    try {
      const response = await fetch(`${this.baseUrl}/api/calendar/integrations/${userId}`);

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const data = await response.json();
      return { success: true, data };
    } catch (error) {
      console.error('Erreur lors de la récupération des intégrations:', error);
      return { 
        success: false, 
        error: error instanceof Error ? error.message : 'Erreur inconnue' 
      };
    }
  }

  /**
   * Obtenir l'emploi du temps hebdomadaire
   */
  async getWeeklySchedule(userId: string, weekStart?: string): Promise<ApiResponse<WeeklySchedule>> {
    try {
      const url = weekStart 
        ? `${this.baseUrl}/api/calendar/schedule/weekly/${userId}?weekStart=${weekStart}`
        : `${this.baseUrl}/api/calendar/schedule/weekly/${userId}/current`;

      const response = await fetch(url);

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const data = await response.json();
      return { success: true, data };
    } catch (error) {
      console.error('Erreur lors de la récupération de l\'emploi du temps:', error);
      return { 
        success: false, 
        error: error instanceof Error ? error.message : 'Erreur inconnue' 
      };
    }
  }

  /**
   * Obtenir l'emploi du temps hebdomadaire pour une école
   */
  async getSchoolWeeklySchedule(schoolId: string, weekStart: string): Promise<ApiResponse<WeeklySchedule>> {
    try {
      const response = await fetch(
        `${this.baseUrl}/api/calendar/schedule/school/${schoolId}/weekly?weekStart=${weekStart}`
      );

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const data = await response.json();
      return { success: true, data };
    } catch (error) {
      console.error('Erreur lors de la récupération de l\'emploi du temps de l\'école:', error);
      return { 
        success: false, 
        error: error instanceof Error ? error.message : 'Erreur inconnue' 
      };
    }
  }

  /**
   * Obtenir l'emploi du temps hebdomadaire pour une salle
   */
  async getRoomWeeklySchedule(roomId: string, weekStart: string): Promise<ApiResponse<WeeklySchedule>> {
    try {
      const response = await fetch(
        `${this.baseUrl}/api/calendar/schedule/room/${roomId}/weekly?weekStart=${weekStart}`
      );

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const data = await response.json();
      return { success: true, data };
    } catch (error) {
      console.error('Erreur lors de la récupération de l\'emploi du temps de la salle:', error);
      return { 
        success: false, 
        error: error instanceof Error ? error.message : 'Erreur inconnue' 
      };
    }
  }

  /**
   * Obtenir l'emploi du temps hebdomadaire pour un enseignant
   */
  async getTeacherWeeklySchedule(teacherId: string, weekStart: string): Promise<ApiResponse<WeeklySchedule>> {
    try {
      const response = await fetch(
        `${this.baseUrl}/api/calendar/schedule/teacher/${teacherId}/weekly?weekStart=${weekStart}`
      );

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const data = await response.json();
      return { success: true, data };
    } catch (error) {
      console.error('Erreur lors de la récupération de l\'emploi du temps de l\'enseignant:', error);
      return { 
        success: false, 
        error: error instanceof Error ? error.message : 'Erreur inconnue' 
      };
    }
  }

  /**
   * Synchroniser les calendriers d'un utilisateur
   */
  async syncCalendars(userId: string): Promise<ApiResponse<string>> {
    try {
      const response = await fetch(`${this.baseUrl}/api/calendar/sync/${userId}`, {
        method: 'POST',
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const data = await response.text();
      return { success: true, data };
    } catch (error) {
      console.error('Erreur lors de la synchronisation:', error);
      return { 
        success: false, 
        error: error instanceof Error ? error.message : 'Erreur inconnue' 
      };
    }
  }

  /**
   * Exporter un événement vers les calendriers externes
   */
  async exportEvent(event: CalendarEvent): Promise<ApiResponse<string>> {
    try {
      const response = await fetch(`${this.baseUrl}/api/calendar/events/export`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(event),
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const data = await response.text();
      return { success: true, data };
    } catch (error) {
      console.error('Erreur lors de l\'export:', error);
      return { 
        success: false, 
        error: error instanceof Error ? error.message : 'Erreur inconnue' 
      };
    }
  }
}

export const calendarApi = new CalendarApi();