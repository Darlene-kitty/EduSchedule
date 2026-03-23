import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface Course {
  id: number;
  name: string;
  code: string;
  teacher: string;
  level: string;
  group: string;
  hours: number;
  semester: string;
  status?: 'active' | 'inactive';
  createdAt?: string;
}

interface ApiWrapped<T> { success: boolean; data: T; }

@Injectable({
  providedIn: 'root'
})
export class CoursesManagementService {
  private api = inject(ApiService);

  getCourses(): Observable<Course[]> {
    return this.api.get<ApiWrapped<Course[]>>('/v1/courses').pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  getCourseById(id: number): Observable<Course> {
    return this.api.get<ApiWrapped<Course>>(`/v1/courses/${id}`).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  addCourse(course: Omit<Course, 'id' | 'createdAt'>): Observable<Course> {
    return this.api.post<ApiWrapped<Course>>('/v1/courses', course).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  updateCourse(id: number, courseData: Partial<Course>): Observable<Course> {
    return this.api.put<ApiWrapped<Course>>(`/v1/courses/${id}`, courseData).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  deleteCourse(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/courses/${id}`);
  }
}
