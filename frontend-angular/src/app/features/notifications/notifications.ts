import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

export interface Notification {
  id: string;
  type: 'success' | 'warning' | 'info' | 'error';
  title: string;
  message: string;
  time: string;
  read: boolean;
  category: 'Cours' | 'Réservation' | 'Conflit' | 'Système';
}

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './notifications.html',
  styleUrl: './notifications.css'
})
export class NotificationsComponent {
  filter = 'all';
  filters = ['all', 'unread', 'Cours', 'Réservation', 'Conflit', 'Système'];

  notifications: Notification[] = [
    { id: '1', type: 'warning', title: 'Conflit détecté', message: 'Conflit de réservation pour la salle A101 le lundi à 10h', time: 'Il y a 5 min', read: false, category: 'Conflit' },
    { id: '2', type: 'success', title: 'Cours ajouté', message: 'Le cours de Mathématiques a été ajouté avec succès pour L1-G1', time: 'Il y a 12 min', read: false, category: 'Cours' },
    { id: '3', type: 'info', title: 'Nouvelle réservation', message: 'Prof. Bernard a réservé l\'Amphi A pour une conférence', time: 'Il y a 23 min', read: true, category: 'Réservation' },
    { id: '4', type: 'success', title: 'Utilisateur ajouté', message: 'Prof. Dubois a été ajouté au système', time: 'Il y a 1h', read: true, category: 'Système' },
    { id: '5', type: 'error', title: 'Réservation annulée', message: 'La réservation de la salle B203 a été annulée', time: 'Il y a 2h', read: true, category: 'Réservation' },
    { id: '6', type: 'info', title: 'Emploi du temps validé', message: 'L\'emploi du temps pour L2-G2 a été validé', time: 'Il y a 3h', read: true, category: 'Cours' },
    { id: '7', type: 'warning', title: 'Maintenance programmée', message: 'Maintenance du système prévue dimanche de 2h à 4h', time: 'Il y a 5h', read: true, category: 'Système' },
  ];

  get filteredNotifications(): Notification[] {
    return this.notifications.filter(n => {
      if (this.filter === 'all') return true;
      if (this.filter === 'unread') return !n.read;
      return n.category === this.filter;
    });
  }

  get unreadCount(): number { return this.notifications.filter(n => !n.read).length; }
  get readCount(): number { return this.notifications.length - this.unreadCount; }

  setFilter(f: string): void { this.filter = f; }

  markAllRead(): void {
    this.notifications = this.notifications.map(n => ({ ...n, read: true }));
  }

  markRead(id: string): void {
    this.notifications = this.notifications.map(n => n.id === id ? { ...n, read: true } : n);
  }

  deleteNotification(id: string): void {
    this.notifications = this.notifications.filter(n => n.id !== id);
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'success': return 'check_circle';
      case 'warning': return 'warning';
      case 'error':   return 'cancel';
      case 'info':    return 'info';
      default:        return 'notifications';
    }
  }

  getTypeClass(type: string): string {
    switch (type) {
      case 'success': return 'type-green';
      case 'warning': return 'type-yellow';
      case 'error':   return 'type-red';
      case 'info':    return 'type-blue';
      default:        return 'type-gray';
    }
  }

  getCategoryIcon(category: string): string {
    switch (category) {
      case 'Cours':       return 'menu_book';
      case 'Réservation': return 'calendar_today';
      case 'Conflit':     return 'warning';
      case 'Système':     return 'settings';
      default:            return 'notifications';
    }
  }

  getFilterLabel(f: string): string {
    if (f === 'all') return 'Toutes';
    if (f === 'unread') return 'Non lues';
    return f;
  }
}