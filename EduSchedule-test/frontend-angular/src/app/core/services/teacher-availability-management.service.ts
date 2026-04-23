import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
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

interface ApiWrapped<T> { success: boolean; data: T; }

@Injectable({ providedIn: 'root' })
export class TeacherAvailabilityManagementService {
  private api = inject(ApiService);

  getAll(): Observable<TeacherAvailabilityEntry[]> {
    return this.api.get<ApiWrapped<TeacherAvailabilityEntry[]>>('/teacher-availability').pipe(
      map(res => res?.data ?? (res as any)),
      catchError(() => of([]))
    );
  }

  getByTeacher(teacherId: number): Observable<TeacherAvailabilityEntry[]> {
    return this.api.get<TeacherAvailabilityEntry[]>(`/teacher-availability/teacher/${teacherId}`).pipe(
      catchError(() => of([]))
    );
  }

  getById(id: number): Observable<TeacherAvailabilityEntry> {
    return this.api.get<ApiWrapped<TeacherAvailabilityEntry>>(`/teacher-availability/${id}`).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  create(entry: Omit<TeacherAvailabilityEntry, 'id'>): Observable<TeacherAvailabilityEntry> {
    return this.api.post<ApiWrapped<TeacherAvailabilityEntry>>('/teacher-availability', entry).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  update(id: number, entry: Partial<TeacherAvailabilityEntry>): Observable<TeacherAvailabilityEntry> {
    return this.api.put<ApiWrapped<TeacherAvailabilityEntry>>(`/teacher-availability/${id}`, entry).pipe(
      map(res => res?.data ?? (res as any))
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
}
