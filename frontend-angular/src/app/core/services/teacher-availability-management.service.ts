import { Injectable, inject } from '@angular/core';
import { Observable, of, forkJoin } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { UsersManagementService } from './users-management.service';

export interface TimeSlot {
  day: string;       // ex: "MONDAY"
  dayLabel: string;  // ex: "Lundi"
  startTime: string; // ex: "08:00"
  endTime: string;   // ex: "12:00"
}

/** Format attendu par le frontend (vue groupée par enseignant) */
export interface TeacherAvailabilityEntry {
  id: number;
  teacherName: string;
  teacherId?: number;
  effectiveDate: string;
  endDate?: string;
  slots: TimeSlot[];
  status: 'active' | 'inactive' | 'pending';
  maxHoursPerDay: number;
  maxHoursPerWeek: number;
  notes?: string;
}

/** Format brut retourné par le backend (une entrée = un créneau) */
export interface BackendAvailabilityDTO {
  id: number;
  teacherId: number;
  schoolId?: number;
  dayOfWeek: string;   // ex: "MONDAY"
  startTime: string;   // ex: "08:00:00" ou "08:00"
  endTime: string;
  availabilityType: string; // "AVAILABLE" | "PREFERRED" | "UNAVAILABLE"
  recurring?: boolean;
  specificDate?: string;
  priority?: number;
  notes?: string;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

/** Format envoyé au backend pour créer/modifier un créneau */
export interface BackendAvailabilityRequest {
  teacherId: number;
  schoolId?: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  availabilityType: string;
  recurring: boolean;
  priority: number;
  notes?: string;
}

const DAY_LABELS: Record<string, string> = {
  MONDAY: 'Lundi', TUESDAY: 'Mardi', WEDNESDAY: 'Mercredi',
  THURSDAY: 'Jeudi', FRIDAY: 'Vendredi', SATURDAY: 'Samedi', SUNDAY: 'Dimanche'
};

/** Normalise "08:00:00" → "08:00" */
function normalizeTime(t: string): string {
  if (!t) return '08:00';
  const parts = t.split(':');
  return `${parts[0].padStart(2, '0')}:${parts[1] || '00'}`;
}

/** Calcule les heures totales par semaine depuis les slots */
function calcWeeklyHours(slots: TimeSlot[]): number {
  return slots.reduce((sum, s) => {
    const [sh, sm] = s.startTime.split(':').map(Number);
    const [eh, em] = s.endTime.split(':').map(Number);
    return sum + (eh + em / 60) - (sh + sm / 60);
  }, 0);
}

/** Détermine le statut depuis availabilityType */
function toStatus(type: string): 'active' | 'inactive' | 'pending' {
  if (type === 'AVAILABLE' || type === 'PREFERRED') return 'active';
  if (type === 'UNAVAILABLE') return 'inactive';
  return 'pending';
}

@Injectable({ providedIn: 'root' })
export class TeacherAvailabilityManagementService {
  private api      = inject(ApiService);
  private usersSvc = inject(UsersManagementService);

  // ── Lecture ──────────────────────────────────────────────────────────────

  /**
   * Récupère toutes les disponibilités et les groupe par enseignant.
   * Résout les noms d'enseignants depuis le user-service.
   */
  getAll(): Observable<TeacherAvailabilityEntry[]> {
    return this.api.get<BackendAvailabilityDTO[]>('/teacher-availability').pipe(
      switchMap(raw => {
        const list = Array.isArray(raw) ? raw : (raw as any)?.data ?? [];
        if (list.length === 0) return of([]);
        return this.usersSvc.getUsers().pipe(
          map(users => this.groupAndEnrich(list, users)),
          catchError(() => of(this.groupAndEnrich(list, [])))
        );
      }),
      catchError(() => of([]))
    );
  }

  getByTeacher(teacherId: number): Observable<TeacherAvailabilityEntry[]> {
    return this.api.get<BackendAvailabilityDTO[]>(`/teacher-availability/teacher/${teacherId}`).pipe(
      switchMap(raw => {
        const list = Array.isArray(raw) ? raw : (raw as any)?.data ?? [];
        if (list.length === 0) return of([]);
        return this.usersSvc.getUserById(teacherId).pipe(
          map(user => {
            const name = user?.name || [user?.firstName, user?.lastName].filter(Boolean).join(' ') || user?.username || `Enseignant #${teacherId}`;
            return this.groupAndEnrich(list, [{ ...user, name }]);
          }),
          catchError(() => of(this.groupAndEnrich(list, [])))
        );
      }),
      catchError(() => of([]))
    );
  }

  // ── Écriture ─────────────────────────────────────────────────────────────

  /**
   * Crée un ou plusieurs créneaux pour un enseignant.
   * Le frontend envoie une entrée groupée → on éclate en N requêtes backend.
   */
  create(entry: Omit<TeacherAvailabilityEntry, 'id'>): Observable<TeacherAvailabilityEntry> {
    const teacherId = this.resolveTeacherId(entry);
    if (entry.slots.length === 0) {
      return of(this.slotsToEntry([], entry.teacherName, teacherId));
    }
    const requests: Observable<BackendAvailabilityDTO>[] = entry.slots.map(slot =>
      this.api.post<BackendAvailabilityDTO>('/teacher-availability', this.toBackendRequest(teacherId, slot, entry))
    );
    return forkJoin(requests).pipe(
      map((results: BackendAvailabilityDTO[]) => {
        const valid = results.filter(r => r !== null && r !== undefined) as BackendAvailabilityDTO[];
        return this.slotsToEntry(valid, entry.teacherName, teacherId);
      })
    );
  }

  /**
   * Met à jour une disponibilité groupée.
   * Stratégie : supprime les anciens créneaux du même enseignant et recrée.
   */
  update(id: number, entry: Partial<TeacherAvailabilityEntry>): Observable<TeacherAvailabilityEntry> {
    // On supprime l'entrée existante puis on recrée
    return this.api.delete<void>(`/teacher-availability/${id}`).pipe(
      switchMap(() => {
        if (!entry.slots || entry.slots.length === 0) {
          return of({ id, teacherName: entry.teacherName || '', slots: [], status: 'inactive' as const, maxHoursPerDay: 8, maxHoursPerWeek: 0, effectiveDate: '' });
        }
        const teacherId = this.resolveTeacherId(entry as TeacherAvailabilityEntry);
        const requests = entry.slots!.map(slot =>
          this.api.post<BackendAvailabilityDTO>('/teacher-availability', this.toBackendRequest(teacherId, slot, entry as TeacherAvailabilityEntry))
        );
        return forkJoin(requests).pipe(
          map(results => this.slotsToEntry(results, entry.teacherName || '', teacherId))
        );
      }),
      catchError(err => {
        // Si la suppression échoue (id inconnu), on essaie juste de créer
        const teacherId = this.resolveTeacherId(entry as TeacherAvailabilityEntry);
        const slots = entry.slots || [];
        const requests = slots.map(slot =>
          this.api.post<BackendAvailabilityDTO>('/teacher-availability', this.toBackendRequest(teacherId, slot, entry as TeacherAvailabilityEntry))
        );
        if (requests.length === 0) return of({ id, teacherName: entry.teacherName || '', slots: [], status: 'inactive' as const, maxHoursPerDay: 8, maxHoursPerWeek: 0, effectiveDate: '' });
        return forkJoin(requests).pipe(
          map(results => this.slotsToEntry(results, entry.teacherName || '', teacherId))
        );
      })
    );
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`/teacher-availability/${id}`);
  }

  checkAvailability(teacherId: number, startDateTime: string, endDateTime: string): Observable<boolean> {
    return this.api.get<boolean>(
      `/teacher-availability/teacher/${teacherId}/check?startDateTime=${encodeURIComponent(startDateTime)}&endDateTime=${encodeURIComponent(endDateTime)}`
    ).pipe(catchError(() => of(false)));
  }

  // ── Helpers privés ────────────────────────────────────────────────────────

  /**
   * Groupe une liste de BackendAvailabilityDTO par teacherId
   * et construit des TeacherAvailabilityEntry enrichis avec les noms.
   */
  private groupAndEnrich(
    list: BackendAvailabilityDTO[],
    users: { id: number; name?: string; firstName?: string; lastName?: string; username?: string }[]
  ): TeacherAvailabilityEntry[] {
    const userMap = new Map(users.map(u => [
      u.id,
      u.name || [u.firstName, u.lastName].filter(Boolean).join(' ') || u.username || `Enseignant #${u.id}`
    ]));

    // Grouper par teacherId
    const grouped = new Map<number, BackendAvailabilityDTO[]>();
    for (const dto of list) {
      if (!dto.active && dto.active !== undefined) continue; // ignorer inactifs
      const arr = grouped.get(dto.teacherId) || [];
      arr.push(dto);
      grouped.set(dto.teacherId, arr);
    }

    const entries: TeacherAvailabilityEntry[] = [];
    grouped.forEach((dtos, teacherId) => {
      const teacherName = userMap.get(teacherId) || `Enseignant #${teacherId}`;
      // Utiliser l'id du premier créneau comme id de l'entrée groupée
      const firstId = dtos[0].id;
      const slots: TimeSlot[] = dtos.map(d => ({
        day: d.dayOfWeek,
        dayLabel: DAY_LABELS[d.dayOfWeek] || d.dayOfWeek,
        startTime: normalizeTime(d.startTime),
        endTime: normalizeTime(d.endTime)
      }));
      const weeklyHours = calcWeeklyHours(slots);
      const status = toStatus(dtos[0].availabilityType);
      entries.push({
        id: firstId,
        teacherName,
        teacherId,
        effectiveDate: dtos[0].createdAt ? dtos[0].createdAt.split('T')[0] : new Date().toISOString().split('T')[0],
        slots,
        status,
        maxHoursPerDay: 8,
        maxHoursPerWeek: Math.round(weeklyHours),
        notes: dtos.map(d => d.notes).filter(Boolean).join('; ') || undefined
      });
    });

    return entries.sort((a, b) => a.teacherName.localeCompare(b.teacherName));
  }

  private slotsToEntry(dtos: BackendAvailabilityDTO[], teacherName: string, teacherId: number): TeacherAvailabilityEntry {
    const slots: TimeSlot[] = dtos.map(d => ({
      day: d.dayOfWeek,
      dayLabel: DAY_LABELS[d.dayOfWeek] || d.dayOfWeek,
      startTime: normalizeTime(d.startTime),
      endTime: normalizeTime(d.endTime)
    }));
    return {
      id: dtos[0]?.id ?? 0,
      teacherName,
      teacherId,
      effectiveDate: new Date().toISOString().split('T')[0],
      slots,
      status: toStatus(dtos[0]?.availabilityType || 'AVAILABLE'),
      maxHoursPerDay: 8,
      maxHoursPerWeek: Math.round(calcWeeklyHours(slots)),
      notes: dtos.map(d => d.notes).filter(Boolean).join('; ') || undefined
    };
  }

  private toBackendRequest(teacherId: number, slot: TimeSlot, entry: Partial<TeacherAvailabilityEntry>): BackendAvailabilityRequest {
    return {
      teacherId,
      dayOfWeek: slot.day,
      startTime: slot.startTime,
      endTime: slot.endTime,
      availabilityType: entry.status === 'inactive' ? 'UNAVAILABLE' : entry.status === 'pending' ? 'PREFERRED' : 'AVAILABLE',
      recurring: true,
      priority: 2,
      notes: entry.notes || undefined
    };
  }

  /**
   * Résout le teacherId depuis le nom ou depuis teacherId déjà présent.
   * Fallback : 0 (le backend rejettera avec une erreur claire).
   */
  private resolveTeacherId(entry: Pick<TeacherAvailabilityEntry, 'teacherId'>): number {
    return entry.teacherId ?? 0;
  }
}
