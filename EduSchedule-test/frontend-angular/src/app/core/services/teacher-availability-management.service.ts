import { Injectable, inject } from '@angular/core';
import { Observable, of, forkJoin } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface TimeSlot {
  day: string;
  dayLabel: string;
  startTime: string;
  endTime: string;
}

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

/** Format attendu par le backend teacher-availability-service */
interface BackendPayload {
  teacherId: number;
  schoolId?: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  availabilityType: string;
  isRecurring: boolean;
  notes?: string;
}

@Injectable({ providedIn: 'root' })
export class TeacherAvailabilityManagementService {
  private api = inject(ApiService);

  /** Récupère toutes les dispo et les regroupe par enseignant */
  getAll(): Observable<TeacherAvailabilityEntry[]> {
    return this.api.get<any>('/teacher-availability').pipe(
      map(res => {
        const list: any[] = Array.isArray(res) ? res : (res?.data ?? []);
        return this.groupByTeacher(list);
      }),
      catchError(() => of([]))
    );
  }

  /** Crée une entrée : envoie un payload par slot */
  create(entry: Omit<TeacherAvailabilityEntry, 'id'>): Observable<TeacherAvailabilityEntry> {
    const payloads = this.toBackendPayloads(entry);
    const calls = payloads.map(p => this.api.post<any>('/teacher-availability', p));
    return forkJoin(calls).pipe(
      map(results => this.backendToEntry(results, entry)),
      catchError(err => { throw err; })
    );
  }

  /** Met à jour : supprime les anciens slots et recrée */
  update(id: number, entry: Partial<TeacherAvailabilityEntry>): Observable<TeacherAvailabilityEntry> {
    return this.api.put<any>(`/teacher-availability/${id}`, this.toBackendPayloads(entry as any)[0]).pipe(
      map(res => ({ ...entry, id, slots: entry.slots ?? [] } as TeacherAvailabilityEntry)),
      catchError(err => { throw err; })
    );
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`/teacher-availability/${id}`);
  }

  /** Convertit les slots frontend → payloads backend (1 payload par slot) */
  private toBackendPayloads(entry: Omit<TeacherAvailabilityEntry, 'id'>): BackendPayload[] {
    const slots = entry.slots?.length ? entry.slots : [{ day: 'MONDAY', dayLabel: 'Lundi', startTime: '08:00', endTime: '12:00' }];
    return slots.map(slot => ({
      teacherId: entry.teacherId ?? 0,
      dayOfWeek: slot.day,
      startTime: slot.startTime,
      endTime: slot.endTime,
      availabilityType: 'AVAILABLE',
      isRecurring: true,
      notes: entry.notes
    }));
  }

  /** Regroupe une liste de dispo backend par teacherId */
  private groupByTeacher(list: any[]): TeacherAvailabilityEntry[] {
    const map = new Map<number, TeacherAvailabilityEntry>();
    for (const item of list) {
      const tid = item.teacherId;
      if (!map.has(tid)) {
        map.set(tid, {
          id: item.id,
          teacherId: tid,
          teacherName: item.teacherName ?? `Enseignant ${tid}`,
          effectiveDate: item.specificDate ?? '',
          slots: [],
          status: item.isActive ? 'active' : 'inactive',
          maxHoursPerDay: 8,
          maxHoursPerWeek: 40,
          notes: item.notes
        });
      }
      map.get(tid)!.slots.push({
        day: item.dayOfWeek,
        dayLabel: item.dayOfWeek,
        startTime: item.startTime,
        endTime: item.endTime
      });
    }
    return Array.from(map.values());
  }

  private backendToEntry(results: any[], entry: Omit<TeacherAvailabilityEntry, 'id'>): TeacherAvailabilityEntry {
    return { ...entry, id: results[0]?.id ?? Date.now() };
  }
}
