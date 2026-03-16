import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { StorageService } from './storage.service';

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

@Injectable({
  providedIn: 'root'
})
export class CoursesManagementService {
  private storageService = inject(StorageService);
  private coursesSubject = new BehaviorSubject<Course[]>([]);
  courses$ = this.coursesSubject.asObservable();

  constructor() {
    this.loadCoursesFromStorage();
  }

  private loadCoursesFromStorage(): void {
    const courses = this.storageService.getItem<Course[]>('coursesList') || this.getDefaultCourses();
    this.coursesSubject.next(courses);
  }

  private getDefaultCourses(): Course[] {
    return [
      {
        id: 1,
        name: 'Mathématiques Avancées',
        code: 'MATH301',
        teacher: 'Dr. Martin',
        level: 'L3',
        group: 'G1',
        hours: 48,
        semester: 'S1',
        status: 'active',
        createdAt: '2024-01-15'
      },
      {
        id: 2,
        name: 'Physique Quantique',
        code: 'PHYS401',
        teacher: 'Prof. Dubois',
        level: 'M1',
        group: 'G1',
        hours: 36,
        semester: 'S1',
        status: 'active',
        createdAt: '2024-01-20'
      },
      {
        id: 3,
        name: 'Chimie Organique',
        code: 'CHEM201',
        teacher: 'Prof. Laurent',
        level: 'L2',
        group: 'G2',
        hours: 42,
        semester: 'S2',
        status: 'active',
        createdAt: '2024-02-01'
      }
    ];
  }

  private saveCourses(courses: Course[]): void {
    this.storageService.setItem('coursesList', courses);
    this.coursesSubject.next(courses);
  }

  getCourses(): Observable<Course[]> {
    return this.courses$;
  }

  getCourseById(id: number): Course | undefined {
    return this.coursesSubject.value.find(c => c.id === id);
  }

  addCourse(course: Omit<Course, 'id' | 'createdAt'>): Observable<Course> {
    return new Observable(observer => {
      setTimeout(() => {
        const courses = this.coursesSubject.value;
        const newCourse: Course = {
          ...course,
          id: Date.now(),
          createdAt: new Date().toISOString().split('T')[0],
          status: course.status || 'active'
        };
        
        const updatedCourses = [...courses, newCourse];
        this.saveCourses(updatedCourses);
        
        observer.next(newCourse);
        observer.complete();
      }, 300);
    });
  }

  updateCourse(id: number, courseData: Partial<Course>): Observable<Course> {
    return new Observable(observer => {
      setTimeout(() => {
        const courses = this.coursesSubject.value;
        const index = courses.findIndex(c => c.id === id);
        
        if (index !== -1) {
          const updatedCourse = { ...courses[index], ...courseData };
          const updatedCourses = [...courses];
          updatedCourses[index] = updatedCourse;
          
          this.saveCourses(updatedCourses);
          observer.next(updatedCourse);
        } else {
          observer.error(new Error('Cours non trouvé'));
        }
        
        observer.complete();
      }, 300);
    });
  }

  deleteCourse(id: number): Observable<void> {
    return new Observable(observer => {
      setTimeout(() => {
        const courses = this.coursesSubject.value;
        const updatedCourses = courses.filter(c => c.id !== id);
        
        this.saveCourses(updatedCourses);
        observer.next();
        observer.complete();
      }, 300);
    });
  }

  searchCourses(query: string): Course[] {
    const courses = this.coursesSubject.value;
    const lowerQuery = query.toLowerCase();
    
    return courses.filter(course => 
      course.name.toLowerCase().includes(lowerQuery) ||
      course.code.toLowerCase().includes(lowerQuery) ||
      course.teacher.toLowerCase().includes(lowerQuery) ||
      course.level.toLowerCase().includes(lowerQuery)
    );
  }
}
