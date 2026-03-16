import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { AuthService } from '../../core/services/auth.service';
import { DashboardService, OccupancyBuilding, UpcomingCourse, RecentActivity } from '../../core/services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatIconModule, SidebarComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private dashboardService = inject(DashboardService);

  currentDate = '';
  currentTime = '';
  currentUser: any = null;

  stats = [
    { label: 'Utilisateurs actifs', value: '0', change: '+0%', icon: 'people', color: 'stat-blue' },
    { label: 'Réservations actives', value: '0', change: '+0%', icon: 'calendar_today', color: 'stat-green' },
    { label: 'Total salles', value: '0', change: '0%', icon: 'meeting_room', color: 'stat-purple' },
    { label: "Taux d'occupation", value: '0%', change: '+0%', icon: 'bar_chart', color: 'stat-orange' },
  ];

  occupancyBuildings: OccupancyBuilding[] = [];
  upcomingCourses: UpcomingCourse[] = [];
  recentActivities: RecentActivity[] = [];
  efficiencyScore = 0;

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadUserData();
    this.loadDashboardData();
  }

  private loadUserData(): void {
    this.authService.user$.subscribe(user => {
      this.currentUser = user;
    });
  }

  private loadDashboardData(): void {
    // Charger les statistiques depuis le service
    this.dashboardService.stats$.subscribe(stats => {
      this.stats = [
        { 
          label: 'Utilisateurs actifs', 
          value: stats.activeUsers.toString(), 
          change: '+12%', 
          icon: 'people', 
          color: 'stat-blue' 
        },
        { 
          label: 'Réservations actives', 
          value: stats.activeReservations.toString(), 
          change: '+8%', 
          icon: 'calendar_today', 
          color: 'stat-green' 
        },
        { 
          label: 'Total salles', 
          value: stats.totalRooms.toString(), 
          change: '0%', 
          icon: 'meeting_room', 
          color: 'stat-purple' 
        },
        { 
          label: "Taux d'occupation", 
          value: stats.occupancyRate + '%', 
          change: '+3%', 
          icon: 'bar_chart', 
          color: 'stat-orange' 
        },
      ];
    });

    // Charger l'occupation par bâtiment
    this.occupancyBuildings = this.dashboardService.getOccupancyByBuilding();

    // Charger les cours à venir
    this.upcomingCourses = this.dashboardService.getUpcomingCourses();

    // Charger les activités récentes
    this.dashboardService.getRecentActivities().subscribe(activities => {
      this.recentActivities = activities;
    });

    // Charger le score d'efficacité
    this.efficiencyScore = this.dashboardService.getEfficiencyScore();
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

  getUserName(): string {
    return this.currentUser?.name || this.currentUser?.username || 'Utilisateur';
  }

  refreshDashboard(): void {
    this.dashboardService.refreshStats();
    this.loadDashboardData();
  }
}