import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface Schedule {
  id: number;
  courseId: number;
  courseName?: string;
  roomId?: number;
  roomName?: string;
  teacherId?: number;
  teacherName?: string;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  date?: string;
}

interface ApiWrapped<T> { success: boolean; data: T; }

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {
  constructor(private apiService: ApiService) {}

  getSchedules(): Observable<Schedule[]> {
    return this.apiService.get<ApiWrapped<Schedule[]>>('/v1/schedules').pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  getSchedule(id: number): Observable<Schedule> {
    return this.apiService.get<ApiWrapped<Schedule>>(`/v1/schedules/${id}`).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  createSchedule(schedule: Partial<Schedule>): Observable<Schedule> {
    return this.apiService.post<ApiWrapped<Schedule>>('/v1/schedules', schedule).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  updateSchedule(id: number, schedule: Partial<Schedule>): Observable<Schedule> {
    return this.apiService.put<ApiWrapped<Schedule>>(`/v1/schedules/${id}`, schedule).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  deleteSchedule(id: number): Observable<void> {
    return this.apiService.delete<void>(`/v1/schedules/${id}`);
  }

  getSchedulesByDate(startDate: string, endDate: string): Observable<Schedule[]> {
    return this.apiService.get<ApiWrapped<Schedule[]>>(`/v1/schedules?start=${startDate}&end=${endDate}`).pipe(
      map(res => res?.data ?? (res as any))
    );
  }
}
