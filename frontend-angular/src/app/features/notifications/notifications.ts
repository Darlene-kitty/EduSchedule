import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

export interface Notification {
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
  currentDate = ''; currentTime = '';
  activeTab: 'notifications' | 'settings' = 'notifications';

  notifications: Notification[] = [
    {
      id: 1, title: "Conflit d'horaire détecté",
      description: 'Double réservation de la salle B203 pour le 22 octobre à 14:00',
      time: 'Il y a 10 minutes', icon: 'warning', iconBg: '#FEF3C7',
      borderColor: '#F59E0B', bgColor: '#FFFBEB', isNew: true, hasActions: true, type: 'warning'
    },
    {
      id: 2, title: 'Nouvelle réservation',
      description: 'Salle A101 réservée pour un séminaire le 25 octobre',
      time: 'Il y a 1 heure', icon: 'calendar_month', iconBg: '#DBEAFE',
      borderColor: '#3B82F6', bgColor: '#EFF6FF', isNew: true, hasActions: false, type: 'info'
    },
    {
      id: 3, title: 'Emploi du temps validé',
      description: "L'emploi du temps de la semaine prochaine a été approuvé",
      time: 'Il y a 2 heures', icon: 'check_circle', iconBg: '#D1FAE5',
      borderColor: '#E5E7EB', bgColor: 'white', isNew: false, hasActions: false, type: 'success'
    },
    {
      id: 4, title: 'Nouveau groupe créé',
      description: 'Le groupe L3-Informatique-G3 a été créé avec 28 étudiants',
      time: 'Il y a 3 heures', icon: 'group', iconBg: '#F3E8FF',
      borderColor: '#E5E7EB', bgColor: 'white', isNew: false, hasActions: false, type: 'group'
    },
  ];

  get newCount(): number { return this.notifications.filter(n => n.isNew).length; }

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  markAllRead(): void { this.notifications.forEach(n => n.isNew = false); }
  dismiss(id: number): void { this.notifications = this.notifications.filter(n => n.id !== id); }
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