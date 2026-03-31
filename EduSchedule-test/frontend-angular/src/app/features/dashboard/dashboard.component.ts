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
    this.dashboardService.getStats().subscribe(stats => {
      this.stats = [
        { label: 'Utilisateurs actifs', value: stats.activeUsers.toString(), change: '+12%', icon: 'people', color: 'stat-blue' },
        { label: 'Réservations actives', value: stats.activeReservations.toString(), change: '+8%', icon: 'calendar_today', color: 'stat-green' },
        { label: 'Total salles', value: stats.totalRooms.toString(), change: '0%', icon: 'meeting_room', color: 'stat-purple' },
        { label: "Taux d'occupation", value: stats.occupancyRate + '%', change: '+3%', icon: 'bar_chart', color: 'stat-orange' },
      ];
      this.efficiencyScore = Math.round(stats.occupancyRate);
    });

    this.dashboardService.getOccupancyByBuilding().subscribe(data => {
      this.occupancyBuildings = data;
    });

    this.dashboardService.getUpcomingCourses().subscribe(courses => {
      this.upcomingCourses = courses;
    });

    this.dashboardService.getRecentActivities().subscribe(activities => {
      this.recentActivities = activities;
    });
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

  getUserInitials(): string {
    const name = this.getUserName();
    return name.split(' ').map((n: string) => n[0]).slice(0, 2).join('').toUpperCase();
  }

  refreshDashboard(): void {
    this.loadDashboardData();
  }
}
