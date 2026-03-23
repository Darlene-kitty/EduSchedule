import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map, catchError, of } from 'rxjs';
import { ApiService } from './api.service';

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
  private api = inject(ApiService);

  /** Construit les stats à partir des services disponibles */
  getStats(): Observable<DashboardStats> {
    return forkJoin({
      users: this.api.get<any[]>('/users').pipe(catchError(() => of([]))),
      salles: this.api.get<any[]>('/v1/salles').pipe(catchError(() => of([]))),
      schedules: this.api.get<any>('/v1/schedules').pipe(catchError(() => of({ data: [] })))
    }).pipe(
      map(({ users, salles, schedules }) => {
        const sallesList: any[] = Array.isArray(salles) ? salles : [];
        const usersList: any[] = Array.isArray(users) ? users : [];
        const schedulesList: any[] = Array.isArray(schedules?.data) ? schedules.data : (Array.isArray(schedules) ? schedules : []);
        const occupied = sallesList.filter((s: any) => !s.disponible).length;
        const total = sallesList.length || 1;
        return {
          activeUsers: usersList.length,
          activeReservations: schedulesList.length,
          totalRooms: sallesList.length,
          occupancyRate: Math.round((occupied / total) * 100)
        };
      })
    );
  }

  getUpcomingCourses(): Observable<UpcomingCourse[]> {
    return this.api.get<any>('/v1/schedules/upcoming').pipe(
      map(res => {
        const list: any[] = Array.isArray(res?.data) ? res.data : (Array.isArray(res) ? res : []);
        return list.slice(0, 5).map((s: any) => ({
          subject: s.course || s.title || 'Cours',
          time: `${s.startTime || ''} - ${s.endTime || ''}`,
          room: s.room || '',
          professor: s.teacher || '',
          students: s.groupSize || 0,
          color: '#3B82F6'
        }));
      }),
      catchError(() => of([]))
    );
  }

  getRecentActivities(): Observable<RecentActivity[]> {
    return of([
      { type: 'success', message: 'Système connecté au backend', time: 'À l\'instant', icon: 'check_circle' }
    ]);
  }

  getOccupancyByBuilding(): Observable<OccupancyBuilding[]> {
    return this.api.get<any[]>('/v1/salles').pipe(
      map(salles => {
        const list: any[] = Array.isArray(salles) ? salles : [];
        const buildings = [...new Set(list.map((s: any) => s.batiment || 'Principal'))];
        return buildings.map(b => {
          const bSalles = list.filter((s: any) => (s.batiment || 'Principal') === b);
          const occupied = bSalles.filter((s: any) => !s.disponible).length;
          return { name: b, occupied, total: bSalles.length, percentage: Math.round((occupied / bSalles.length) * 100) };
        });
      }),
      catchError(() => of([]))
    );
  }
}
