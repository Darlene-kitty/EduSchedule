import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { StorageService } from './storage.service';
import { UsersManagementService } from './users-management.service';

export interface DashboardStats {
  activeUsers: number;
  activeReservations: number;
  totalRooms: number;
  occupancyRate: number;
}

export interface OccupancyBuilding {
  name: string;
  occupied: number;
  total: number;
  percentage: number;
}

export interface UpcomingCourse {
  subject: string;
  time: string;
  room: string;
  professor: string;
  students: number;
  color: string;
}

export interface RecentActivity {
  type: 'success' | 'warning' | 'error';
  message: string;
  time: string;
  icon: string;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private storageService = inject(StorageService);
  private usersManagementService = inject(UsersManagementService);

  private statsSubject = new BehaviorSubject<DashboardStats>(this.getDefaultStats());
  stats$ = this.statsSubject.asObservable();

  private activitiesSubject = new BehaviorSubject<RecentActivity[]>([]);
  activities$ = this.activitiesSubject.asObservable();

  constructor() {
    this.loadDashboardData();
    this.calculateStats();
    
    // Écouter les changements d'utilisateurs pour recalculer les stats
    this.usersManagementService.userChanged$.subscribe(() => {
      this.calculateStats();
    });
  }

  private getDefaultStats(): DashboardStats {
    return {
      activeUsers: 0,
      activeReservations: 36,
      totalRooms: 30,
      occupancyRate: 73
    };
  }

  private loadDashboardData(): void {
    // Charger les activités depuis le localStorage
    const savedActivities = this.storageService.getItem<RecentActivity[]>('recentActivities');
    if (savedActivities && savedActivities.length > 0) {
      this.activitiesSubject.next(savedActivities);
    } else {
      // Activités par défaut
      this.activitiesSubject.next(this.getDefaultActivities());
    }
  }

  private getDefaultActivities(): RecentActivity[] {
    return [
      { type: 'success', message: 'Système initialisé avec succès', time: 'Il y a 1 min', icon: 'check_circle' },
      { type: 'success', message: 'Configuration chargée', time: 'Il y a 2 min', icon: 'settings' },
    ];
  }

  calculateStats(): void {
    // Calculer les stats basées sur les données réelles
    this.usersManagementService.getUsers().subscribe(users => {
      const activeUsers = users.filter(u => u.status === 'active').length;
      
      const stats: DashboardStats = {
        activeUsers,
        activeReservations: this.getReservationsCount(),
        totalRooms: this.getTotalRooms(),
        occupancyRate: this.calculateOccupancyRate()
      };

      this.statsSubject.next(stats);
      this.storageService.setItem('dashboardStats', stats);
    });
  }

  private getReservationsCount(): number {
    const reservations = this.storageService.getItem<any[]>('reservations') || [];
    return reservations.filter(r => r.status === 'active').length;
  }

  private getTotalRooms(): number {
    const rooms = this.storageService.getItem<any[]>('rooms') || this.getDefaultRooms();
    return rooms.length;
  }

  private getDefaultRooms(): any[] {
    const defaultRooms = [
      { id: 1, name: 'A101', building: 'A', capacity: 30, status: 'available' },
      { id: 2, name: 'A102', building: 'A', capacity: 40, status: 'available' },
      { id: 3, name: 'B201', building: 'B', capacity: 25, status: 'occupied' },
      { id: 4, name: 'B202', building: 'B', capacity: 35, status: 'available' },
      { id: 5, name: 'C301', building: 'C', capacity: 50, status: 'occupied' },
    ];
    this.storageService.setItem('rooms', defaultRooms);
    return defaultRooms;
  }

  private calculateOccupancyRate(): number {
    const rooms = this.storageService.getItem<any[]>('rooms') || this.getDefaultRooms();
    const occupiedRooms = rooms.filter(r => r.status === 'occupied').length;
    return Math.round((occupiedRooms / rooms.length) * 100);
  }

  getOccupancyByBuilding(): OccupancyBuilding[] {
    const rooms = this.storageService.getItem<any[]>('rooms') || this.getDefaultRooms();
    const buildings = ['A', 'B', 'C', 'Lab'];
    
    return buildings.map(building => {
      const buildingRooms = rooms.filter(r => 
        building === 'Lab' ? r.building === 'Lab' : r.building === building
      );
      const occupied = buildingRooms.filter(r => r.status === 'occupied').length;
      const total = buildingRooms.length || 1;
      
      return {
        name: building === 'Lab' ? 'Laboratoires' : `Bâtiment ${building}`,
        occupied,
        total,
        percentage: Math.round((occupied / total) * 100)
      };
    }).filter(b => b.total > 0);
  }

  getUpcomingCourses(): UpcomingCourse[] {
    const courses = this.storageService.getItem<UpcomingCourse[]>('upcomingCourses');
    if (courses && courses.length > 0) {
      return courses;
    }

    // Cours par défaut
    const defaultCourses: UpcomingCourse[] = [
      { subject: 'Mathématiques', time: '08:00 - 10:00', room: 'A101', professor: 'Dr. Martin', students: 42, color: '#10B981' },
      { subject: 'Physique', time: '10:00 - 12:00', room: 'B203', professor: 'Dr. Laurent', students: 38, color: '#8B5CF6' },
      { subject: 'Chimie Organique', time: '14:00 - 16:00', room: 'Lab D104', professor: 'Prof. Laurent', students: 35, color: '#3B82F6' },
    ];
    
    this.storageService.setItem('upcomingCourses', defaultCourses);
    return defaultCourses;
  }

  getRecentActivities(): Observable<RecentActivity[]> {
    return this.activities$;
  }

  addActivity(activity: Omit<RecentActivity, 'time'>): void {
    const activities = this.activitiesSubject.value;
    const newActivity: RecentActivity = {
      ...activity,
      time: 'À l\'instant'
    };

    const updatedActivities = [newActivity, ...activities].slice(0, 10); // Garder les 10 dernières
    this.activitiesSubject.next(updatedActivities);
    this.storageService.setItem('recentActivities', updatedActivities);
  }

  // Méthodes pour mettre à jour les données

  addReservation(reservation: any): void {
    const reservations = this.storageService.getItem<any[]>('reservations') || [];
    reservations.push({ ...reservation, id: Date.now(), status: 'active' });
    this.storageService.setItem('reservations', reservations);
    
    this.addActivity({
      type: 'success',
      message: `Nouvelle réservation: ${reservation.room}`,
      icon: 'event'
    });
    
    this.calculateStats();
  }

  updateRoomStatus(roomId: number, status: 'available' | 'occupied' | 'maintenance'): void {
    const rooms = this.storageService.getItem<any[]>('rooms') || [];
    const updatedRooms = rooms.map(r => 
      r.id === roomId ? { ...r, status } : r
    );
    this.storageService.setItem('rooms', updatedRooms);
    this.calculateStats();
  }

  addRoom(room: any): void {
    const rooms = this.storageService.getItem<any[]>('rooms') || [];
    rooms.push({ ...room, id: Date.now() });
    this.storageService.setItem('rooms', rooms);
    
    this.addActivity({
      type: 'success',
      message: `Nouvelle salle ajoutée: ${room.name}`,
      icon: 'meeting_room'
    });
    
    this.calculateStats();
  }

  getEfficiencyScore(): number {
    const stats = this.statsSubject.value;
    // Calculer un score basé sur plusieurs métriques
    const userScore = Math.min((stats.activeUsers / 100) * 100, 100);
    const occupancyScore = stats.occupancyRate;
    const reservationScore = Math.min((stats.activeReservations / 50) * 100, 100);
    
    return Math.round((userScore + occupancyScore + reservationScore) / 3);
  }

  refreshStats(): void {
    this.calculateStats();
  }
}
