import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { StorageService } from './storage.service';

export interface Notification {
  id: number;
  title: string;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
  read: boolean;
  timestamp: string;
  link?: string;
  icon?: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationsManagementService {
  private storageService = inject(StorageService);
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  notifications$ = this.notificationsSubject.asObservable();

  constructor() {
    this.loadNotificationsFromStorage();
  }

  private loadNotificationsFromStorage(): void {
    const notifications = this.storageService.getItem<Notification[]>('notificationsList') || this.getDefaultNotifications();
    this.notificationsSubject.next(notifications);
  }

  private getDefaultNotifications(): Notification[] {
    return [
      {
        id: 1,
        title: 'Nouveau cours ajouté',
        message: 'Le cours "Mathématiques Avancées" a été ajouté à votre emploi du temps',
        type: 'success',
        read: false,
        timestamp: new Date(Date.now() - 3600000).toISOString(),
        icon: 'book'
      },
      {
        id: 2,
        title: 'Conflit détecté',
        message: 'Un conflit a été détecté dans votre emploi du temps pour lundi 10h',
        type: 'warning',
        read: false,
        timestamp: new Date(Date.now() - 7200000).toISOString(),
        icon: 'warning'
      },
      {
        id: 3,
        title: 'Réservation approuvée',
        message: 'Votre réservation de la salle A101 a été approuvée',
        type: 'success',
        read: true,
        timestamp: new Date(Date.now() - 86400000).toISOString(),
        icon: 'check_circle'
      }
    ];
  }

  private saveNotifications(notifications: Notification[]): void {
    this.storageService.setItem('notificationsList', notifications);
    this.notificationsSubject.next(notifications);
  }

  getNotifications(): Observable<Notification[]> {
    return this.notifications$;
  }

  getUnreadCount(): number {
    return this.notificationsSubject.value.filter(n => !n.read).length;
  }

  addNotification(notification: Omit<Notification, 'id' | 'timestamp'>): Observable<Notification> {
    return new Observable(observer => {
      setTimeout(() => {
        const notifications = this.notificationsSubject.value;
        const newNotification: Notification = {
          ...notification,
          id: Date.now(),
          timestamp: new Date().toISOString()
        };
        
        const updatedNotifications = [newNotification, ...notifications];
        this.saveNotifications(updatedNotifications);
        
        observer.next(newNotification);
        observer.complete();
      }, 100);
    });
  }

  markAsRead(id: number): Observable<Notification> {
    return new Observable(observer => {
      setTimeout(() => {
        const notifications = this.notificationsSubject.value;
        const index = notifications.findIndex(n => n.id === id);
        
        if (index !== -1) {
          const updatedNotification = { ...notifications[index], read: true };
          const updatedNotifications = [...notifications];
          updatedNotifications[index] = updatedNotification;
          
          this.saveNotifications(updatedNotifications);
          observer.next(updatedNotification);
        } else {
          observer.error(new Error('Notification non trouvée'));
        }
        
        observer.complete();
      }, 100);
    });
  }

  markAllAsRead(): Observable<void> {
    return new Observable(observer => {
      setTimeout(() => {
        const notifications = this.notificationsSubject.value.map(n => ({ ...n, read: true }));
        this.saveNotifications(notifications);
        observer.next();
        observer.complete();
      }, 100);
    });
  }

  deleteNotification(id: number): Observable<void> {
    return new Observable(observer => {
      setTimeout(() => {
        const notifications = this.notificationsSubject.value;
        const updatedNotifications = notifications.filter(n => n.id !== id);
        
        this.saveNotifications(updatedNotifications);
        observer.next();
        observer.complete();
      }, 100);
    });
  }

  clearAll(): Observable<void> {
    return new Observable(observer => {
      setTimeout(() => {
        this.saveNotifications([]);
        observer.next();
        observer.complete();
      }, 100);
    });
  }
}
