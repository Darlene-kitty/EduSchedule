import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { NotificationsManagementService, Notification as ApiNotification, NotificationPreferences } from '../../core/services/notifications-management.service';
import { AuthService } from '../../core/services/auth.service';
import { WebSocketService, WsMessage } from '../../core/services/websocket.service';
import { Subscription } from 'rxjs';

// Interface locale pour l'affichage UI
export interface UiNotification {
  id: number; title: string; description: string; time: string;
  icon: string; iconBg: string; borderColor: string; bgColor: string;
  isNew: boolean; hasActions: boolean; type: 'warning' | 'info' | 'success' | 'group';
}

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './notifications.html',
  styleUrl: './notifications.css'
})
export class NotificationsComponent implements OnInit, OnDestroy {
  private notifService = inject(NotificationsManagementService);
  private authService  = inject(AuthService);
  private wsService    = inject(WebSocketService);
  private wsSub?: Subscription;

  currentDate = ''; currentTime = '';
  currentUserName = '';
  currentUserInitials = '';
  activeTab: 'notifications' | 'settings' = 'notifications';
  isLoading = false;
  isSavingPrefs = false;
  prefsSaved = false;

  notifications: UiNotification[] = [];

  preferences: NotificationPreferences = {
    emailEnabled: true, pushEnabled: true, scheduleChanges: true,
    conflictAlerts: true, reservationUpdates: true,
    reminderNotifications: true, reminderMinutesBefore: 30
  };

  get newCount(): number { return this.notifications.filter(n => n.isNew).length; }

  private get userId(): number {
    return this.authService.getUser()?.id ?? 0;
  }

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    const user = this.authService.getUser();
    if (user) {
      this.currentUserName = user.firstName && user.lastName
        ? `${user.firstName} ${user.lastName}`
        : (user.username ?? user.email ?? '');
      const name = this.currentUserName.trim();
      const parts = name.split(' ').filter(Boolean);
      this.currentUserInitials = parts.length >= 2
        ? (parts[0][0] + parts[1][0]).toUpperCase()
        : name.substring(0, 2).toUpperCase();
    }
    this.loadNotifications();
    this.loadPreferences();

    // Connexion WebSocket — push temps réel
    this.wsService.connect();
    this.wsSub = this.wsService.allNotifications$.subscribe((msg: WsMessage) => {
      const uiNotif: UiNotification = {
        id: Date.now(),
        title: msg.title || 'Notification',
        description: msg.message || '',
        time: new Date().toLocaleString('fr-FR'),
        icon: this.iconForType(msg.type),
        iconBg: this.bgForType(msg.type),
        borderColor: this.borderForType(msg.type),
        bgColor: 'white',
        isNew: true,
        hasActions: false,
        type: this.uiTypeFor(msg.type)
      };
      this.notifications = [uiNotif, ...this.notifications];
    });
  }

  ngOnDestroy(): void { this.wsSub?.unsubscribe(); }

  loadNotifications(): void {
    this.isLoading = true;
    this.notifService.getNotifications().subscribe({
      next: (data) => {
        this.notifications = (data || []).map(n => this.mapToUi(n));
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur chargement notifications:', err?.error?.message || err);
        this.notifications = [];
        this.isLoading = false;
      }
    });
  }

  loadPreferences(): void {
    if (!this.userId) return;
    this.notifService.getPreferences(this.userId).subscribe({
      next: (prefs) => { this.preferences = prefs; },
      error: () => { /* garder les valeurs par défaut */ }
    });
  }

  savePreferences(): void {
    if (!this.userId) return;
    this.isSavingPrefs = true;
    this.notifService.savePreferences(this.userId, this.preferences).subscribe({
      next: (saved) => {
        this.preferences = saved;
        this.isSavingPrefs = false;
        this.prefsSaved = true;
        setTimeout(() => this.prefsSaved = false, 3000);
      },
      error: () => { this.isSavingPrefs = false; }
    });
  }

  private mapToUi(n: ApiNotification): UiNotification {
    const type = (n.type === 'warning' || n.type === 'error') ? 'warning'
               : n.type === 'success' ? 'success'
               : 'info';
    const iconMap: Record<string, string> = {
      warning: 'warning', error: 'error', success: 'check_circle', info: 'info'
    };
    const bgMap: Record<string, string> = {
      warning: '#FEF3C7', error: '#FEE2E2', success: '#D1FAE5', info: '#DBEAFE'
    };
    const borderMap: Record<string, string> = {
      warning: '#F59E0B', error: '#EF4444', success: '#10B981', info: '#3B82F6'
    };
    return {
      id: n.id,
      title: n.title || n.subject || 'Notification',
      description: n.message,
      time: n.createdAt ? new Date(n.createdAt).toLocaleString('fr-FR') : '',
      icon: iconMap[n.type] || 'info',
      iconBg: bgMap[n.type] || '#DBEAFE',
      borderColor: borderMap[n.type] || '#3B82F6',
      bgColor: 'white',
      isNew: !n.read && n.status !== 'READ',
      hasActions: n.type === 'warning' || n.type === 'error',
      type: type as UiNotification['type']
    };
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  markAllRead(): void {
    this.notifications.filter(n => n.isNew).forEach(n => {
      this.notifService.markAsRead(n.id).subscribe();
      n.isNew = false;
    });
  }

  dismiss(id: number): void {
    this.notifService.deleteNotification(id).subscribe();
    this.notifications = this.notifications.filter(n => n.id !== id);
  }

  getIconColor(type: string): string {
    switch(type) {
      case 'warning': return '#D97706';
      case 'info':    return '#2563EB';
      case 'success': return '#15803D';
      case 'group':   return '#7C3AED';
      default:        return '#6B7280';
    }
  }

  private iconForType(t: string): string {
    if (t?.includes('CONFLICT') || t?.includes('warning')) return 'warning';
    if (t?.includes('approved') || t?.includes('success')) return 'check_circle';
    if (t?.includes('rejected') || t?.includes('cancelled')) return 'cancel';
    return 'notifications';
  }
  private bgForType(t: string): string {
    if (t?.includes('CONFLICT') || t?.includes('warning')) return '#FEF3C7';
    if (t?.includes('approved') || t?.includes('success')) return '#D1FAE5';
    if (t?.includes('rejected') || t?.includes('cancelled')) return '#FEE2E2';
    return '#DBEAFE';
  }
  private borderForType(t: string): string {
    if (t?.includes('CONFLICT') || t?.includes('warning')) return '#F59E0B';
    if (t?.includes('approved') || t?.includes('success')) return '#10B981';
    if (t?.includes('rejected') || t?.includes('cancelled')) return '#EF4444';
    return '#3B82F6';
  }
  private uiTypeFor(t: string): UiNotification['type'] {
    if (t?.includes('CONFLICT') || t?.includes('warning')) return 'warning';
    if (t?.includes('approved') || t?.includes('success')) return 'success';
    return 'info';
  }
}
