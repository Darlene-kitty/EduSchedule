import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from './api.service';

export interface ScheduleEntry {
  id: number;
  courseId?: number;
  courseName?: string;
  title?: string;
  teacher?: string;
  room?: string;
  dayOfWeek?: number | string;
  startTime: string;
  endTime: string;
  level?: string;
  group?: string;
  color?: string;
  createdAt?: string;
}

/** Payload attendu par le backend ScheduleDTO */
interface SchedulePayload {
  title: string;
  description?: string;
  startTime: string;   // LocalDateTime ISO: 2026-04-02T08:00:00
  endTime: string;
  room?: string;
  teacher?: string;
  course?: string;
  groupName?: string;
  status?: string;
}

interface ApiWrapped<T> { success: boolean; data: T; }

// Jours de la semaine → numéro ISO (lundi=1)
const DAY_TO_ISO: Record<string, number> = {
  Lundi: 1, Mardi: 2, Mercredi: 3, Jeudi: 4, Vendredi: 5, Samedi: 6, Dimanche: 7,
  MONDAY: 1, TUESDAY: 2, WEDNESDAY: 3, THURSDAY: 4, FRIDAY: 5, SATURDAY: 6, SUNDAY: 7
};

/** Construit un LocalDateTime ISO à partir d'un jour de semaine + heure HH:mm.
 *  Utilise toujours la prochaine occurrence future du jour (jamais dans le passé). */
function toLocalDateTime(dayOfWeek: string, time: string): string {
  const now = new Date();
  const targetDay = DAY_TO_ISO[dayOfWeek] ?? 1; // 1=lundi ... 7=dimanche
  // getDay() retourne 0=dim, 1=lun... on convertit en ISO (1=lun...7=dim)
  const todayISO = now.getDay() === 0 ? 7 : now.getDay();
  const [h, m] = time.split(':').map(Number);

  // Calculer le décalage en jours pour atteindre le prochain targetDay
  let daysAhead = targetDay - todayISO;
  if (daysAhead < 0) {
    daysAhead += 7; // semaine prochaine
  } else if (daysAhead === 0) {
    // Même jour : vérifier si l'heure est déjà passée
    const targetToday = new Date(now);
    targetToday.setHours(h, m, 0, 0);
    if (targetToday <= now) daysAhead = 7; // semaine prochaine
  }

  const target = new Date(now);
  target.setDate(now.getDate() + daysAhead);
  target.setHours(h, m, 0, 0);
  // Format LocalDateTime sans timezone : 2026-04-07T08:00:00
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${target.getFullYear()}-${pad(target.getMonth()+1)}-${pad(target.getDate())}T${pad(h)}:${pad(m)}:00`;
}

@Injectable({ providedIn: 'root' })
export class ScheduleManagementService {
  private api = inject(ApiService);

  getSchedule(): Observable<ScheduleEntry[]> {
    return this.api.get<ApiWrapped<ScheduleEntry[]>>('/v1/schedules').pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  addScheduleEntry(entry: Omit<ScheduleEntry, 'id' | 'createdAt'>): Observable<ScheduleEntry> {
    const payload: SchedulePayload = {
      title: entry.courseName || entry.title || 'Séance',
      startTime: toLocalDateTime(String(entry.dayOfWeek ?? 'Lundi'), entry.startTime),
      endTime:   toLocalDateTime(String(entry.dayOfWeek ?? 'Lundi'), entry.endTime),
      room:      entry.room,
      teacher:   entry.teacher,
      course:    entry.courseName,
      groupName: entry.group,
      status:    'ACTIVE'
    };
    return this.api.post<ApiWrapped<ScheduleEntry>>('/v1/schedules', payload).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  updateScheduleEntry(id: number, entry: Partial<ScheduleEntry>): Observable<ScheduleEntry> {
    const payload: Partial<SchedulePayload> = {
      title:     entry.courseName || entry.title,
      startTime: entry.dayOfWeek && entry.startTime ? toLocalDateTime(String(entry.dayOfWeek), entry.startTime) : undefined,
      endTime:   entry.dayOfWeek && entry.endTime   ? toLocalDateTime(String(entry.dayOfWeek), entry.endTime)   : undefined,
      room:      entry.room,
      teacher:   entry.teacher,
      course:    entry.courseName,
      groupName: entry.group
    };
    return this.api.put<ApiWrapped<ScheduleEntry>>(`/v1/schedules/${id}`, payload).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  deleteScheduleEntry(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/schedules/${id}`);
  }
}
