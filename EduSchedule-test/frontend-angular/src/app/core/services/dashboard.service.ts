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

  getStats(): Observable<DashboardStats> {
    return forkJoin({
      users:        this.api.get<any[]>('/users').pipe(catchError(() => of([]))),
      salles:       this.api.get<any[]>('/v1/salles').pipe(catchError(() => of([]))),
      reservations: this.api.get<any>('/v1/reservations').pipe(catchError(() => of([])))
    }).pipe(
      map(({ users, salles, reservations }) => {
        const sallesList: any[]       = Array.isArray(salles) ? salles : [];
        const usersList: any[]        = Array.isArray(users) ? users : [];
        const resList: any[]          = Array.isArray(reservations?.data) ? reservations.data : (Array.isArray(reservations) ? reservations : []);
        const activeRes               = resList.filter((r: any) => ['approved', 'APPROVED', 'pending', 'PENDING'].includes(r.status));
        const occupied                = sallesList.filter((s: any) => !s.disponible).length;
        const total                   = sallesList.length || 1;
        return {
          activeUsers:        usersList.filter((u: any) => u.enabled !== false).length,
          activeReservations: activeRes.length,
          totalRooms:         sallesList.length,
          occupancyRate:      Math.round((occupied / total) * 100)
        };
      })
    );
  }

  getUpcomingCourses(): Observable<UpcomingCourse[]> {
    return this.api.get<any>('/v1/schedules/upcoming').pipe(
      map(res => {
        const list: any[] = Array.isArray(res?.data) ? res.data : (Array.isArray(res) ? res : []);
        const colors = ['#3B82F6', '#10B981', '#8B5CF6', '#F59E0B', '#EF4444'];
        return list.slice(0, 5).map((s: any, i: number) => ({
          subject:   s.course || s.title || s.courseName || 'Cours',
          time:      `${s.startTime || ''} - ${s.endTime || ''}`,
          room:      s.room || s.roomName || '',
          professor: s.teacher || s.teacherName || '',
          students:  s.groupSize || s.studentCount || 0,
          color:     colors[i % colors.length]
        }));
      }),
      catchError(() => of([]))
    );
  }

  getRecentActivities(): Observable<RecentActivity[]> {
    return forkJoin({
      reservations: this.api.get<any>('/v1/reservations').pipe(catchError(() => of([]))),
      salles:       this.api.get<any[]>('/v1/salles').pipe(catchError(() => of([])))
    }).pipe(
      map(({ reservations, salles }) => {
        const activities: RecentActivity[] = [];
        const resList: any[] = Array.isArray(reservations?.data) ? reservations.data : (Array.isArray(reservations) ? reservations : []);

        // Dernières réservations
        resList.slice(0, 3).forEach((r: any) => {
          const status = (r.status || '').toLowerCase();
          activities.push({
            type:    status === 'approved' ? 'success' : status === 'rejected' ? 'error' : 'warning',
            message: `Réservation ${r.roomName || r.title || '#' + r.id} — ${status === 'approved' ? 'approuvée' : status === 'rejected' ? 'rejetée' : 'en attente'}`,
            time:    r.createdAt ? new Date(r.createdAt).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }) : 'Récemment',
            icon:    status === 'approved' ? 'check_circle' : status === 'rejected' ? 'cancel' : 'hourglass_empty'
          });
        });

        // Salles indisponibles
        const sallesList: any[] = Array.isArray(salles) ? salles : [];
        const occupied = sallesList.filter((s: any) => !s.disponible);
        if (occupied.length > 0) {
          activities.push({
            type: 'warning',
            message: `${occupied.length} salle(s) actuellement occupée(s)`,
            time: 'Maintenant',
            icon: 'meeting_room'
          });
        }

        if (!activities.length) {
          activities.push({ type: 'success', message: 'Système opérationnel', time: 'À l\'instant', icon: 'check_circle' });
        }

        return activities;
      }),
      catchError(() => of([{ type: 'success' as const, message: 'Système connecté', time: 'À l\'instant', icon: 'check_circle' }]))
    );
  }

  getOccupancyByBuilding(): Observable<OccupancyBuilding[]> {
    return this.api.get<any[]>('/v1/salles').pipe(
      map(salles => {
        const list: any[] = Array.isArray(salles) ? salles : [];
        const buildings = [...new Set(list.map((s: any) => s.batiment || 'Principal'))];
        return buildings.map(b => {
          const bSalles  = list.filter((s: any) => (s.batiment || 'Principal') === b);
          const occupied = bSalles.filter((s: any) => !s.disponible).length;
          return { name: b, occupied, total: bSalles.length, percentage: Math.round((occupied / bSalles.length) * 100) };
        });
      }),
      catchError(() => of([]))
    );
  }
}
