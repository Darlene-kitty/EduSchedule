import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

interface OccupancyBuilding {
  name: string;
  occupied: number;
  total: number;
  percentage: number;
}

interface UpcomingCourse {
  subject: string;
  time: string;
  room: string;
  professor: string;
  students: number;
  color: string;
}

interface RecentActivity {
  type: 'success' | 'warning' | 'error';
  message: string;
  time: string;
  icon: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatIconModule, SidebarComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  currentDate = '';
  currentTime = '';

  stats = [
    { label: 'Utilisateurs actifs', value: '248', change: '+12%', icon: 'people', color: 'stat-blue' },
    { label: 'Réservations actives', value: '36', change: '+8%', icon: 'calendar_today', color: 'stat-green' },
    { label: 'Total salles', value: '30', change: '0%', icon: 'meeting_room', color: 'stat-purple' },
    { label: "Taux d'occupation", value: '73%', change: '+3%', icon: 'bar_chart', color: 'stat-orange' },
  ];

  occupancyBuildings: OccupancyBuilding[] = [
    { name: 'Bâtiment A', occupied: 8,  total: 12, percentage: 67 },
    { name: 'Bâtiment B', occupied: 6,  total: 8,  percentage: 75 },
    { name: 'Bâtiment C', occupied: 4,  total: 10, percentage: 40 },
    { name: 'Laboratoires', occupied: 5, total: 6,  percentage: 83 },
  ];

  upcomingCourses: UpcomingCourse[] = [
    { subject: 'Chimie Organique', time: '16:15 - 18:15', room: 'Lab D104', professor: 'Prof. Laurent', students: 35, color: '#3B82F6' },
    { subject: 'Mathématiques', time: '08:00 - 10:00', room: 'A101', professor: 'Dr. Martin', students: 42, color: '#10B981' },
    { subject: 'Physique', time: '10:00 - 12:00', room: 'B203', professor: 'Dr. Laurent', students: 38, color: '#8B5CF6' },
  ];

  recentActivities: RecentActivity[] = [
    { type: 'success', message: 'Cours de Mathématiques ajouté pour L1-G1', time: 'Il y a 5 min', icon: 'check_circle' },
    { type: 'warning', message: 'Conflit détecté: Salle A101 (Lundi 10h)', time: 'Il y a 12 min', icon: 'warning' },
    { type: 'success', message: 'Nouvel utilisateur: Prof. Dubois', time: 'Il y a 23 min', icon: 'person_add' },
    { type: 'error',   message: 'Réservation annulée: Salle B203', time: 'Il y a 1h', icon: 'cancel' },
    { type: 'success', message: 'Emploi du temps validé pour L2-G2', time: 'Il y a 2h', icon: 'event_available' },
  ];

  efficiencyScore = 78;

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', {
      weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
    });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  getActivityClass(type: string): string {
    switch (type) {
      case 'success': return 'activity-success';
      case 'warning': return 'activity-warning';
      case 'error':   return 'activity-error';
      default:        return 'activity-success';
    }
  }

  getOccupancyColor(percentage: number): string {
    if (percentage >= 80) return '#EF4444';
    if (percentage >= 60) return '#3B82F6';
    return '#10B981';
  }
}