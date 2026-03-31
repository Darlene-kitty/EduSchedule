import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface ExamSlot {
  courseId: number; courseName: string; courseCode: string;
  level: string; semester: string; teacherName?: string;
  roomId?: number; roomName?: string;
  date: string; startTime: string; endTime: string;
  durationMinutes: number; examType: string;
  status: 'SCHEDULED' | 'CONFLICT'; conflictReason?: string;
}

export interface ExamSchedulingRequest {
  schoolId?: number;
  semester: string;
  levels: string[];
  sessionStart: string;   // YYYY-MM-DD
  sessionEnd: string;
  defaultDurationMinutes: number;
  availableSlots?: string[];
  roomIds?: number[];
  maxExamsPerDayPerLevel?: number;
  respectTeacherAvailability?: boolean;
}

export interface ExamSchedulingResult {
  success: boolean; message: string;
  totalCourses: number; scheduled: number; conflicts: number;
  slots: ExamSlot[]; unscheduled: ExamSlot[];
}

@Injectable({ providedIn: 'root' })
export class ExamSchedulingService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl || 'http://localhost:8080/api'}/v1/exams/scheduling`;

  generate(request: ExamSchedulingRequest): Observable<ExamSchedulingResult> {
    return this.http.post<ExamSchedulingResult>(`${this.base}/generate`, request).pipe(
      catchError(() => of({ success: false, message: 'Erreur de connexion', totalCourses: 0, scheduled: 0, conflicts: 0, slots: [], unscheduled: [] }))
    );
  }
}
