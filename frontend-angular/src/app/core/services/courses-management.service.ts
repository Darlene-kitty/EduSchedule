import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { ApiService } from './api.service';

/** Aligné sur CourseDTO.java */
export interface Course {
  id: number;
  name: string;
  code: string;
  description?: string;
  credits?: number;
  hoursPerWeek?: number;
  duration?: number;       // en semaines (CourseDTO) ou minutes (CourseRequest)
  level: string;           // L1, L2, L3, M1, M2, DOCTORAT
  department?: string;
  semester: string;        // S1, S2
  schoolId?: number;
  teacherId?: number;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
  // Champs calculés retournés par le backend
  schoolName?: string;
  teacherName?: string;
  totalHours?: number;
  groupCount?: number;
  // Champs legacy conservés pour compatibilité UI
  teacher?: string;
  group?: string;
  hours?: number;
  status?: 'active' | 'inactive';
  maxStudents?: number;
}

/** Aligné sur CourseRequest.java */
export interface CoursePayload {
  name: string;
  code: string;
  description?: string;
  credits: number;
  duration: number;        // durée séance en minutes (30–480)
  department: string;
  level: string;
  semester: string;
  teacherId?: number;
  maxStudents?: number;
  hoursPerWeek?: number;
  schoolId?: number;       // @NotNull dans CourseDTO — obligatoire à la création
}

interface ApiWrapped<T> { success: boolean; data: T; }

@Injectable({ providedIn: 'root' })
export class CoursesManagementService {
  private api = inject(ApiService);

  getCourses(): Observable<Course[]> {
    return this.api.get<ApiWrapped<Course[]>>('/v1/courses').pipe(
      map(res => {
        const list = res?.data ?? (res as any);
        if (!Array.isArray(list)) return [];
        return list.map((c: any) => this.normalize(c));
      }),
      catchError(() => of([]))
    );
  }

  getCourseById(id: number): Observable<Course> {
    return this.api.get<ApiWrapped<Course>>(`/v1/courses/${id}`).pipe(
      map(res => this.normalize(res?.data ?? (res as any)))
    );
  }

  addCourse(payload: CoursePayload): Observable<Course> {
    return this.api.post<ApiWrapped<Course>>('/v1/courses', payload).pipe(
      map(res => this.normalize(res?.data ?? (res as any)))
    );
  }

  updateCourse(id: number, payload: Partial<CoursePayload>): Observable<Course> {
    return this.api.put<ApiWrapped<Course>>(`/v1/courses/${id}`, payload).pipe(
      map(res => this.normalize(res?.data ?? (res as any)))
    );
  }

  deleteCourse(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/courses/${id}`);
  }

  /** Assigne un groupe (school-service) à un cours via course-groups */
  assignGroup(courseId: number, groupName: string, maxStudents: number, teacherId?: number): Observable<any> {
    return this.api.post<any>('/v1/course-groups', {
      courseId,
      groupName,
      type: 'COURS',
      maxStudents: maxStudents || 30,
      teacherId: teacherId ?? null
    }).pipe(catchError(() => of(null)));
  }

  /** Normalise la réponse backend vers l'interface Course UI */
  private normalize(c: any): Course {
    return {
      ...c,
      // Alias pour compatibilité UI
      teacher: c.teacherName ?? c.teacher ?? '',
      group:   c.group ?? '',
      hours:   c.hoursPerWeek ?? c.hours ?? 0,
      status:  c.active === false ? 'inactive' : 'active',
    };
  }
}
