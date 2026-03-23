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
}
