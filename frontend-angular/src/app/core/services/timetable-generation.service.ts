import { Injectable, inject } from '@angular/core';
import { Observable, interval, switchMap, takeWhile } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface SchedulingRequest {
  schoolId: number;
  semester: string;
  level: string;
  availableSlots?: string[];
  roomIds?: number[];
  maxHoursPerDay?: number;
  algorithm?: 'ford-fulkerson' | 'edmonds-karp';
}

export interface ScheduleSlot {
  courseId: number;
  courseCode: string;
  courseName: string;
  teacherId: number;
  teacherName?: string;
  roomId: number;
  roomName: string;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  level: string;
  semester: string;
}

export interface GenerationJob {
  jobId: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'PARTIAL' | 'FAILED';
  progress: number;
  slots?: ScheduleSlot[];
  unassignedCourses?: string[];
  maxFlowValue?: number;
  totalDemand?: number;
  message?: string;
  generationTimeMs?: number;
}

interface ApiResponse<T> { success: boolean; data: T; }

@Injectable({ providedIn: 'root' })
export class TimetableGenerationService {
  private api = inject(ApiService);

  /** Lance la génération, retourne le jobId */
  generate(request: SchedulingRequest): Observable<string> {
    return this.api.post<{ success: boolean; jobId: string }>('/v1/timetable/generate', request).pipe(
      map(res => res.jobId)
    );
  }

  /** Récupère l'état d'un job */
  getStatus(jobId: string): Observable<GenerationJob> {
    return this.api.get<ApiResponse<GenerationJob>>(`/v1/timetable/status/${jobId}`).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  /** Polling toutes les 2s jusqu'à COMPLETED/PARTIAL/FAILED */
  pollUntilDone(jobId: string): Observable<GenerationJob> {
    return interval(2000).pipe(
      switchMap(() => this.getStatus(jobId)),
      takeWhile(job => job.status === 'PENDING' || job.status === 'RUNNING', true)
    );
  }

  /** Valide un résultat généré */
  validate(result: GenerationJob): Observable<{ valid: boolean; conflicts: number; message: string }> {
    return this.api.post('/v1/timetable/validate', result);
  }

  /** Confirme, sauvegarde en base et déclenche la sync calendrier */
  confirm(jobId: string, schoolId: number, userId: string, weekStart: string): Observable<{ success: boolean; savedSlots: number; message: string }> {
    return this.api.post(
      `/v1/timetable/${jobId}/confirm?schoolId=${schoolId}&userId=${encodeURIComponent(userId)}&weekStart=${weekStart}`,
      null
    );
  }
}
