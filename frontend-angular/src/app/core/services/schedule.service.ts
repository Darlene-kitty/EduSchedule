import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
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

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {
  constructor(private apiService: ApiService) {}

  getSchedules(): Observable<Schedule[]> {
    return this.apiService.get<Schedule[]>('/schedules');
  }

  getSchedule(id: number): Observable<Schedule> {
    return this.apiService.get<Schedule>(`/schedules/${id}`);
  }

  createSchedule(schedule: Partial<Schedule>): Observable<Schedule> {
    return this.apiService.post<Schedule>('/schedules', schedule);
  }

  updateSchedule(id: number, schedule: Partial<Schedule>): Observable<Schedule> {
    return this.apiService.put<Schedule>(`/schedules/${id}`, schedule);
  }

  deleteSchedule(id: number): Observable<void> {
    return this.apiService.delete<void>(`/schedules/${id}`);
  }

  getSchedulesByDate(startDate: string, endDate: string): Observable<Schedule[]> {
    return this.apiService.get<Schedule[]>(`/schedules?start=${startDate}&end=${endDate}`);
  }
}
