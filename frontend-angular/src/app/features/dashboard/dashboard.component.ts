import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { HeaderComponent } from '../../shared/components/header/header.component';

interface DashboardStats {
  label: string;
  value: string;
  change: string;
  trend: 'up' | 'down';
  color: string;
  icon: string;
}

interface Activity {
  type: 'success' | 'warning' | 'error';
  message: string;
  time: string;
  icon: string;
}

interface Course {
  subject: string;
  time: string;
  room: string;
  group: string;
  professor: string;
  color: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  loading = false;
  
  stats: DashboardStats[] = [
    {
      label: 'Utilisateurs actifs',
      value: '248',
      change: '+12%',
      trend: 'up',
      color: 'bg-blue-500',
      icon: 'users'
    },
    {
      label: 'Réservations actives',
      value: '42',
      change: '+8%',
      trend: 'up',
      color: 'bg-green-500',
      icon: 'calendar'
    },
    {
      label: 'Total salles',
      value: '28',
      change: '0%',
      trend: 'up',
      color: 'bg-purple-500',
      icon: 'book'
    },
    {
      label: "Taux d'occupation",
      value: '76%',
      change: '+5%',
      trend: 'up',
      color: 'bg-orange-500',
      icon: 'alert'
    }
  ];

  efficiencyScore = 85;
  efficiencyTrend = '+3%';

  recentActivities: Activity[] = [
    {
      type: 'success',
      message: 'Cours de Mathématiques ajouté pour L1-G1',
      time: 'Il y a 5 min',
      icon: 'check'
    },
    {
      type: 'warning',
      message: 'Conflit détecté: Salle A101 (Lundi 10h)',
      time: 'Il y a 12 min',
      icon: 'alert'
    },
    {
      type: 'success',
      message: 'Nouvel utilisateur: Prof. Dubois',
      time: 'Il y a 23 min',
      icon: 'user'
    },
    {
      type: 'error',
      message: 'Réservation annulée: Salle B203',
      time: 'Il y a 1h',
      icon: 'x'
    },
    {
      type: 'success',
      message: 'Emploi du temps validé pour L2-G2',
      time: 'Il y a 2h',
      icon: 'calendar'
    }
  ];

  upcomingCourses: Course[] = [
    {
      subject: 'Mathématiques',
      time: '08:00-10:00',
      room: 'A101',
      group: 'L1-G1',
      professor: 'Dr. Martin',
      color: 'bg-blue-500'
    },
    {
      subject: 'Physique',
      time: '10:00-12:00',
      room: 'B203',
      group: 'L1-G1',
      professor: 'Dr. Laurent',
      color: 'bg-green-500'
    },
    {
      subject: 'Analyse',
      time: '08:00-10:00',
      room: 'A103',
      group: 'L2-G1',
      professor: 'Prof. Bernard',
      color: 'bg-cyan-500'
    },
    {
      subject: 'Géométrie',
      time: '09:00-11:00',
      room: 'A101',
      group: 'L1-G1',
      professor: 'Dr. Martin',
      color: 'bg-indigo-500'
    }
  ];

  ngOnInit() {
    // Load dashboard data
  }

  getActivityClass(type: string): string {
    switch (type) {
      case 'success':
        return 'bg-green-100';
      case 'warning':
        return 'bg-yellow-100';
      case 'error':
        return 'bg-red-100';
      default:
        return 'bg-gray-100';
    }
  }

  getActivityIconClass(type: string): string {
    switch (type) {
      case 'success':
        return 'text-green-600';
      case 'warning':
        return 'text-yellow-600';
      case 'error':
        return 'text-red-600';
      default:
        return 'text-gray-600';
    }
  }
}
