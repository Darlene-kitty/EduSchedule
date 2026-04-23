import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface Notification {
  id: number;
  title?: string;
  subject?: string;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
  read?: boolean;
  status?: string;
  timestamp?: string;
  createdAt?: string;
  recipient?: string;
}

export interface NotificationPreferences {
  userId?: number;
  emailEnabled: boolean;
  pushEnabled: boolean;
  scheduleChanges: boolean;
  conflictAlerts: boolean;
  reservationUpdates: boolean;
  reminderNotifications: boolean;
  reminderMinutesBefore: number;
}

export interface ReminderPayload {
  /** IDs des destinataires (étudiants + enseignant) */
  recipientIds: number[];
  /** Titre du rappel */
  subject: string;
  /** Corps du message */
  message: string;
  /** Type d'événement déclencheur */
  eventType: 'SCHEDULE_CHANGE' | 'COURSE_REMINDER' | 'ROOM_CHANGE' | 'CANCELLATION' | 'EVENT_REMINDER';
  /** ID de la séance ou de l'événement concerné */
  eventId?: number;
  /** Priorité : LOW | NORMAL | HIGH */
  priority?: 'LOW' | 'NORMAL' | 'HIGH';
}

interface ApiWrapped<T> { success: boolean; data: T; }

@Injectable({
  providedIn: 'root'
})
export class NotificationsManagementService {
  private api = inject(ApiService);

  getNotifications(): Observable<Notification[]> {
    return this.api.get<ApiWrapped<Notification[]>>('/v1/notifications').pipe(
      map(res => res?.data ?? (res as any)),
      catchError(() => of([]))
    );
  }

  getNotificationById(id: number): Observable<Notification> {
    return this.api.get<ApiWrapped<Notification>>(`/v1/notifications/${id}`).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  addNotification(notification: Omit<Notification, 'id'>): Observable<Notification> {
    return this.api.post<ApiWrapped<Notification>>('/v1/notifications', notification).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  markAsRead(id: number): Observable<void> {
    return this.api.post<void>(`/v1/notifications/${id}/read`, {});
  }

  deleteNotification(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/notifications/${id}`);
  }

  getPreferences(userId: number): Observable<NotificationPreferences> {
    return this.api.get<NotificationPreferences>(`/notifications/advanced/preferences/${userId}`).pipe(
      catchError(() => of({
        emailEnabled: true, pushEnabled: true, scheduleChanges: true,
        conflictAlerts: true, reservationUpdates: true,
        reminderNotifications: true, reminderMinutesBefore: 30
      }))
    );
  }

  savePreferences(userId: number, prefs: NotificationPreferences): Observable<NotificationPreferences> {
    return this.api.post<NotificationPreferences>(`/notifications/advanced/preferences/${userId}`, prefs);
  }

  /**
   * Envoie un rappel en masse à une liste de destinataires.
   * Utilise POST /api/notifications/advanced/bulk-send
   */
  sendBulkReminder(payload: ReminderPayload): Observable<{ success: boolean; sentCount: number }> {
    return this.api.post<any>('/notifications/advanced/bulk-send', {
      recipientIds: payload.recipientIds,
      subject:      payload.subject,
      message:      payload.message,
      eventType:    payload.eventType,
      eventId:      payload.eventId,
      priority:     payload.priority ?? 'NORMAL',
    }).pipe(
      map(res => ({ success: res?.success ?? true, sentCount: res?.sentCount ?? payload.recipientIds.length })),
      catchError(() => of({ success: false, sentCount: 0 }))
    );
  }

  /**
   * Notifie un changement d'emploi du temps.
   * Utilise POST /api/notifications/advanced/schedule-change
   */
  sendScheduleChangeNotification(scheduleId: number, changeType: string): Observable<boolean> {
    return this.api.post<any>('/notifications/advanced/schedule-change', {
      scheduleId,
      changeType,
      eventType: changeType,
      eventTimestamp: new Date().toISOString(),
    }).pipe(
      map(res => res?.success ?? true),
      catchError(() => of(false))
    );
  }
}
