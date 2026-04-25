import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from './api.service';

export interface ScheduleEntry {
  id: number;
  courseId?: number;
  courseName?: string;
  title?: string;
  teacher?: string;
  /** ID de l'enseignant dans le user-service — retourné par le backend depuis la v2 */
  teacherId?: number;
  room?: string;
  dayOfWeek?: number | string;
  startTime: string;
  endTime: string;
  level?: string;
  group?: string;
  groupName?: string;
  color?: string;
  status?: string;
  course?: string;
  createdAt?: string;
}

interface ApiWrapped<T> { success: boolean; data: T; }

@Injectable({
  providedIn: 'root'
})
export class ScheduleManagementService {
  private api = inject(ApiService);

  getSchedule(): Observable<ScheduleEntry[]> {
    return this.api.get<ApiWrapped<ScheduleEntry[]>>('/v1/schedules').pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  getScheduleById(id: number): Observable<ScheduleEntry> {
    return this.api.get<ApiWrapped<ScheduleEntry>>(`/v1/schedules/${id}`).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  addScheduleEntry(entry: Omit<ScheduleEntry, 'id' | 'createdAt'>): Observable<ScheduleEntry> {
    return this.api.post<ApiWrapped<ScheduleEntry>>('/v1/schedules', entry).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  updateScheduleEntry(id: number, entryData: Partial<ScheduleEntry>): Observable<ScheduleEntry> {
    return this.api.put<ApiWrapped<ScheduleEntry>>(`/v1/schedules/${id}`, entryData).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  deleteScheduleEntry(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/schedules/${id}`);
  }

  getConflicts(room: string, startTime: string, endTime: string): Observable<ScheduleEntry[]> {
    return this.api.get<ApiWrapped<ScheduleEntry[]>>(
      `/v1/schedules/conflicts/room?room=${encodeURIComponent(room)}&startTime=${startTime}&endTime=${endTime}`
    ).pipe(map(res => res?.data ?? (res as any)));
  }
}
