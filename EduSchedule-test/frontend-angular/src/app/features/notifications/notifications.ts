import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { NotificationsManagementService, Notification as ApiNotification } from '../../core/services/notifications-management.service';

// Interface locale pour l'affichage UI
export interface UiNotification {
  id: number; title: string; description: string; time: string;
  icon: string; iconBg: string; borderColor: string; bgColor: string;
  isNew: boolean; hasActions: boolean; type: 'warning' | 'info' | 'success' | 'group';
}

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, MatIconModule, SidebarComponent],
  templateUrl: './notifications.html',
  styleUrl: './notifications.css'
})
export class NotificationsComponent implements OnInit {
  private notifService = inject(NotificationsManagementService);

  currentDate = ''; currentTime = '';
  activeTab: 'notifications' | 'settings' = 'notifications';
  isLoading = false;

  notifications: UiNotification[] = [];

  get newCount(): number { return this.notifications.filter(n => n.isNew).length; }

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.notifService.getNotifications().subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.notifications = data.map(n => this.mapToUi(n));
        } else {
          // fallback données démo si backend vide
          this.notifications = this.getDemoNotifications();
        }
        this.isLoading = false;
      },
      error: () => {
        this.notifications = this.getDemoNotifications();
        this.isLoading = false;
      }
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

  private getDemoNotifications(): UiNotification[] {
    return [
      { id: 1, title: "Conflit d'horaire détecté", description: 'Double réservation de la salle B203 pour le 22 octobre à 14:00', time: 'Il y a 10 minutes', icon: 'warning', iconBg: '#FEF3C7', borderColor: '#F59E0B', bgColor: '#FFFBEB', isNew: true, hasActions: true, type: 'warning' },
      { id: 2, title: 'Nouvelle réservation', description: 'Salle A101 réservée pour un séminaire le 25 octobre', time: 'Il y a 1 heure', icon: 'calendar_month', iconBg: '#DBEAFE', borderColor: '#3B82F6', bgColor: '#EFF6FF', isNew: true, hasActions: false, type: 'info' },
      { id: 3, title: 'Emploi du temps validé', description: "L'emploi du temps de la semaine prochaine a été approuvé", time: 'Il y a 2 heures', icon: 'check_circle', iconBg: '#D1FAE5', borderColor: '#E5E7EB', bgColor: 'white', isNew: false, hasActions: false, type: 'success' },
    ];
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
}
