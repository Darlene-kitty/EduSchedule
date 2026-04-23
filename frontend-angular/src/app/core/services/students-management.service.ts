import { Injectable } from '@angular/core';
import { Observable, throwError, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface StudentBackend {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  enabled: boolean;
  createdAt?: string;
}

@Injectable({ providedIn: 'root' })
export class StudentsManagementService {
  private readonly base = '/students';

  constructor(private api: ApiService) {}

  /** Retourne la liste des étudiants mappée vers le modèle frontend */
  getStudents(): Observable<import('../../features/students/students').Student[]> {
    return this.api.get<any>(this.base).pipe(
      map(r => (r.data ?? r) as StudentBackend[]),
      map(list => list.map(s => this.toFrontend(s))),
      catchError(() => of([]))
    );
  }

  addStudent(dto: Partial<import('../../features/students/students').Student>): Observable<import('../../features/students/students').Student> {
    const safeUsername = (dto.matricule ?? '').replace(/[^a-zA-Z0-9_-]/g, '_').trim() || `student_${Date.now()}`;
    const payload = { firstName: dto.prenom, lastName: dto.nom, email: dto.email, username: safeUsername, password: 'Student@2025', role: 'STUDENT', enabled: dto.enabled ?? true };
    return this.api.post<any>(this.base, payload).pipe(
      map(r => this.toFrontend(r.data ?? r))
    );
  }

  updateStudent(id: number, dto: Partial<import('../../features/students/students').Student>): Observable<import('../../features/students/students').Student> {
    // Sanitize username: remove spaces and special chars not allowed by backend pattern
    const safeUsername = (dto.matricule ?? '').replace(/[^a-zA-Z0-9_-]/g, '_');
    const payload = { firstName: dto.prenom, lastName: dto.nom, email: dto.email, username: safeUsername || undefined, role: 'STUDENT', enabled: dto.enabled };
    return this.api.put<any>(`${this.base}/${id}`, payload).pipe(
      map(r => this.toFrontend(r.data ?? r))
    );
  }

  deleteStudent(id: number): Observable<boolean> {
    return this.api.delete<void>(`${this.base}/${id}`).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  private toFrontend(s: StudentBackend): import('../../features/students/students').Student {
    return {
      id: s.id,
      matricule: s.username ?? '',
      nom: s.lastName ?? '',
      prenom: s.firstName ?? '',
      email: s.email ?? '',
      telephone: '',
      filiere: '',
      niveau: '',
      classe: '',
      dateNaissance: '',
      enabled: s.enabled
    };
  }
}
