import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
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
    const payload = { firstName: dto.prenom, lastName: dto.nom, email: dto.email, username: dto.matricule, role: 'STUDENT', enabled: dto.enabled ?? true };
    return this.api.post<any>(this.base, payload).pipe(
      map(r => this.toFrontend(r.data ?? r)),
      catchError(() => of({ id: Date.now(), matricule: dto.matricule ?? '', nom: dto.nom ?? '', prenom: dto.prenom ?? '', email: dto.email ?? '', telephone: dto.telephone ?? '', filiere: dto.filiere ?? '', niveau: dto.niveau ?? '', classe: dto.classe ?? '', dateNaissance: dto.dateNaissance ?? '', enabled: dto.enabled ?? true }))
    );
  }

  updateStudent(id: number, dto: Partial<import('../../features/students/students').Student>): Observable<import('../../features/students/students').Student> {
    const payload = { firstName: dto.prenom, lastName: dto.nom, email: dto.email, username: dto.matricule, role: 'STUDENT', enabled: dto.enabled };
    return this.api.put<any>(`${this.base}/${id}`, payload).pipe(
      map(r => this.toFrontend(r.data ?? r)),
      catchError(() => of({ id, matricule: dto.matricule ?? '', nom: dto.nom ?? '', prenom: dto.prenom ?? '', email: dto.email ?? '', telephone: dto.telephone ?? '', filiere: dto.filiere ?? '', niveau: dto.niveau ?? '', classe: dto.classe ?? '', dateNaissance: dto.dateNaissance ?? '', enabled: dto.enabled ?? true }))
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
