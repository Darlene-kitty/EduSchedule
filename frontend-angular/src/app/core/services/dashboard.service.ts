import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map, catchError, of } from 'rxjs';
import { ApiService } from './api.service';

export interface DashboardStats {
  activeUsers: number;
  activeReservations: number;
  totalRooms: number;
  occupancyRate: number;
  /** Tendances calculées sur les 7 derniers jours vs 7 jours précédents */
  trends: {
    users: string;
    reservations: string;
    rooms: string;
    occupancy: string;
  };
  /** Score d'efficacité composite (0-100) */
  efficiencyScore: number;
  /** Nombre de cours planifiés aujourd'hui */
  coursesToday: number;
  /** Nombre de conflits actifs */
  activeConflicts: number;
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
      users:        this.api.get<any>('/users').pipe(catchError(() => of([]))),
      salles:       this.api.get<any>('/v1/salles').pipe(catchError(() => of([]))),
      reservations: this.api.get<any>('/v1/reservations').pipe(catchError(() => of([]))),
      schedules:    this.api.get<any>('/v1/schedules').pipe(catchError(() => of([]))),
      courses:      this.api.get<any>('/v1/courses').pipe(catchError(() => of([]))),
      adjustments:  this.api.get<any>('/v1/timetable/adjustments').pipe(catchError(() => of([]))),
    }).pipe(
      map(({ users, salles, reservations, schedules, courses, adjustments }) => {
        // Normaliser les listes
        const usersList: any[]  = Array.isArray(users?.data) ? users.data : (Array.isArray(users) ? users : []);
        const sallesList: any[] = Array.isArray(salles?.data) ? salles.data : (Array.isArray(salles) ? salles : []);
        const resList: any[]    = Array.isArray(reservations?.data) ? reservations.data : (Array.isArray(reservations) ? reservations : []);
        const schedList: any[]  = Array.isArray(schedules?.data) ? schedules.data : (Array.isArray(schedules) ? schedules : []);
        const coursesList: any[]= Array.isArray(courses?.data) ? courses.data : (Array.isArray(courses) ? courses : []);

        // Conflits actifs depuis le timetable-service
        const adjList: any[] = Array.isArray(adjustments?.data) ? adjustments.data : (Array.isArray(adjustments) ? adjustments : []);
        const activeConflicts = adjList.filter((a: any) => a.status === 'CONFLICT').length;

        const activeUsers = usersList.filter((u: any) => u.enabled !== false).length;
        const activeRes   = resList.filter((r: any) => ['approved','APPROVED','pending','PENDING','CONFIRMED'].includes(r.status));
        const occupied    = sallesList.filter((s: any) => !s.disponible).length;
        const total       = sallesList.length || 1;
        const occupancyRate = Math.round((occupied / total) * 100);

        // Cours planifiés aujourd'hui
        const today = new Date().toLocaleDateString('fr-FR', { weekday: 'long' }).split(' ')[0];
        const jours: Record<string, string> = { lundi:'Lundi', mardi:'Mardi', mercredi:'Mercredi', jeudi:'Jeudi', vendredi:'Vendredi', samedi:'Samedi' };
        const todayFr = jours[today.toLowerCase()] || today;
        const coursesToday = schedList.filter((s: any) =>
          (s.dayOfWeek === todayFr || s.dayOfWeek === today.toUpperCase())
        ).length;

        // Score d'efficacité composite :
        // 40% occupation salles + 30% réservations actives (normalisé sur 20 max) + 30% cours planifiés (normalisé sur 10 max)
        const resScore     = Math.min(100, (activeRes.length / Math.max(1, 20)) * 100);
        const courseScore  = Math.min(100, (coursesToday / Math.max(1, 10)) * 100);
        const efficiencyScore = Math.round(occupancyRate * 0.4 + resScore * 0.3 + courseScore * 0.3);

        // Tendances : comparer réservations récentes (7j) vs précédentes (7j)
        const now = new Date();
        const sevenDaysAgo = new Date(now); sevenDaysAgo.setDate(now.getDate() - 7);
        const fourteenDaysAgo = new Date(now); fourteenDaysAgo.setDate(now.getDate() - 14);
        const recentRes = resList.filter((r: any) => r.createdAt && new Date(r.createdAt) >= sevenDaysAgo).length;
        const prevRes   = resList.filter((r: any) => r.createdAt && new Date(r.createdAt) >= fourteenDaysAgo && new Date(r.createdAt) < sevenDaysAgo).length;
        const resTrend  = prevRes > 0 ? Math.round(((recentRes - prevRes) / prevRes) * 100) : (recentRes > 0 ? 100 : 0);

        return {
          activeUsers,
          activeReservations: activeRes.length,
          totalRooms: sallesList.length,
          occupancyRate,
          efficiencyScore,
          coursesToday,
          activeConflicts,
          trends: {
            users:        activeUsers > 0 ? `${activeUsers} actifs` : '—',
            reservations: resTrend >= 0 ? `+${resTrend}%` : `${resTrend}%`,
            rooms:        `${sallesList.filter((s: any) => s.disponible !== false).length} disponibles`,
            occupancy:    occupancyRate >= 70 ? '↑ Forte demande' : occupancyRate >= 40 ? '→ Normale' : '↓ Faible',
          }
        };
      })
    );
  }

  getUpcomingCourses(): Observable<UpcomingCourse[]> {
    return forkJoin({
      schedules: this.api.get<any>('/v1/schedules').pipe(catchError(() => of([]))),
      events:    this.api.get<any>('/v1/events/upcoming').pipe(catchError(() => of([]))),
    }).pipe(
      map(({ schedules, events }) => {
        const colors = ['#3B82F6', '#10B981', '#8B5CF6', '#F59E0B', '#EF4444'];
        const schedList: any[] = Array.isArray(schedules?.data) ? schedules.data : (Array.isArray(schedules) ? schedules : []);
        const eventList: any[] = Array.isArray(events?.data) ? events.data : (Array.isArray(events) ? events : []);

        // Fusionner séances et événements à venir
        const combined = [
          ...schedList.slice(0, 4).map((s: any) => ({
            subject:   s.courseName || s.title || 'Cours',
            time:      `${s.startTime || ''} – ${s.endTime || ''}`,
            room:      s.room || s.roomName || '—',
            professor: s.teacher || s.teacherName || '—',
            students:  s.groupSize || s.studentCount || 0,
          })),
          ...eventList.slice(0, 2).map((e: any) => ({
            subject:   e.title || 'Événement',
            time:      e.startDateTime ? new Date(e.startDateTime).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }) : '—',
            room:      e.resourceName || '—',
            professor: e.organizerName || '—',
            students:  e.currentParticipants || 0,
          })),
        ];

        return combined.slice(0, 5).map((c, i) => ({ ...c, color: colors[i % colors.length] }));
      }),
      catchError(() => of([]))
    );
  }

  getRecentActivities(): Observable<RecentActivity[]> {
    return forkJoin({
      reservations: this.api.get<any>('/v1/reservations').pipe(catchError(() => of([]))),
      salles:       this.api.get<any[]>('/v1/salles').pipe(catchError(() => of([]))),
    }).pipe(
      map(({ reservations, salles }) => {
        const activities: RecentActivity[] = [];
        const resList: any[] = Array.isArray(reservations?.data) ? reservations.data : (Array.isArray(reservations) ? reservations : []);

        // Trier par date décroissante et prendre les 3 plus récentes
        const sorted = [...resList].sort((a, b) =>
          new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime()
        );

        sorted.slice(0, 3).forEach((r: any) => {
          const status = (r.status || '').toLowerCase();
          const label  = r.title || r.roomName || `Réservation #${r.id}`;
          activities.push({
            type:    status === 'approved' || status === 'confirmed' ? 'success' : status === 'rejected' || status === 'cancelled' ? 'error' : 'warning',
            message: `${label} — ${status === 'approved' || status === 'confirmed' ? 'approuvée' : status === 'rejected' ? 'rejetée' : status === 'cancelled' ? 'annulée' : 'en attente'}`,
            time:    r.createdAt ? new Date(r.createdAt).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }) : 'Récemment',
            icon:    status === 'approved' || status === 'confirmed' ? 'check_circle' : status === 'rejected' || status === 'cancelled' ? 'cancel' : 'hourglass_empty'
          });
        });

        // Salles occupées
        const sallesList: any[] = Array.isArray(salles) ? salles : [];
        const occupiedCount = sallesList.filter((s: any) => !s.disponible).length;
        if (occupiedCount > 0) {
          activities.push({ type: 'warning', message: `${occupiedCount} salle(s) actuellement occupée(s)`, time: 'Maintenant', icon: 'meeting_room' });
        }

        return activities.length ? activities : [{ type: 'success' as const, message: 'Système opérationnel', time: 'À l\'instant', icon: 'check_circle' }];
      }),
      catchError(() => of([{ type: 'success' as const, message: 'Système connecté', time: 'À l\'instant', icon: 'check_circle' }]))
    );
  }

  getOccupancyByBuilding(): Observable<OccupancyBuilding[]> {
    return this.api.get<any>('/v1/salles').pipe(
      map(res => {
        const list: any[] = Array.isArray(res?.data) ? res.data : (Array.isArray(res) ? res : []);
        const buildings = [...new Set(list.map((s: any) => s.batiment || 'Bâtiment principal'))];
        return buildings.map(b => {
          const bSalles  = list.filter((s: any) => (s.batiment || 'Bâtiment principal') === b);
          const occupied = bSalles.filter((s: any) => !s.disponible).length;
          return { name: b, occupied, total: bSalles.length, percentage: bSalles.length ? Math.round((occupied / bSalles.length) * 100) : 0 };
        }).sort((a, b) => b.percentage - a.percentage);
      }),
      catchError(() => of([]))
    );
  }
}
