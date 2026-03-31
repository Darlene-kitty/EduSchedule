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
    return this.api.get<NotificationPreferences>(`/v1/notifications/preferences/${userId}`).pipe(
      catchError(() => of({
        emailEnabled: true, pushEnabled: true, scheduleChanges: true,
        conflictAlerts: true, reservationUpdates: true,
        reminderNotifications: true, reminderMinutesBefore: 30
      }))
    );
  }

  savePreferences(userId: number, prefs: NotificationPreferences): Observable<NotificationPreferences> {
    return this.api.put<NotificationPreferences>(`/v1/notifications/preferences/${userId}`, prefs);
  }
}
