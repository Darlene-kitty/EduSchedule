import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface Course {
  id: number;
  name: string;
  code: string;
  description?: string;
  credits: number;
  teacherId?: number;
  teacherName?: string;
  semester: string;
  department?: string;
}

interface ApiWrapped<T> { success: boolean; data: T; }

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  constructor(private apiService: ApiService) {}

  getCourses(): Observable<Course[]> {
    return this.apiService.get<ApiWrapped<Course[]>>('/v1/courses').pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  getCourse(id: number): Observable<Course> {
    return this.apiService.get<ApiWrapped<Course>>(`/v1/courses/${id}`).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  createCourse(course: Partial<Course>): Observable<Course> {
    return this.apiService.post<ApiWrapped<Course>>('/v1/courses', course).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  updateCourse(id: number, course: Partial<Course>): Observable<Course> {
    return this.apiService.put<ApiWrapped<Course>>(`/v1/courses/${id}`, course).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  deleteCourse(id: number): Observable<void> {
    return this.apiService.delete<void>(`/v1/courses/${id}`);
  }
}
