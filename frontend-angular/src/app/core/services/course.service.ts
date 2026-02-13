import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
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

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  constructor(private apiService: ApiService) {}

  getCourses(): Observable<Course[]> {
    return this.apiService.get<Course[]>('/courses');
  }

  getCourse(id: number): Observable<Course> {
    return this.apiService.get<Course>(`/courses/${id}`);
  }

  createCourse(course: Partial<Course>): Observable<Course> {
    return this.apiService.post<Course>('/courses', course);
  }

  updateCourse(id: number, course: Partial<Course>): Observable<Course> {
    return this.apiService.put<Course>(`/courses/${id}`, course);
  }

  deleteCourse(id: number): Observable<void> {
    return this.apiService.delete<void>(`/courses/${id}`);
  }
}
