import { Injectable } from '@angular/core';
import { Observable, of, forkJoin } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
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

/** Affectation retournée par school-service */
interface AffectationBackend {
  id: number;
  etudiantId: number;
  groupeId: number;
  groupeName: string;
  groupeCode: string;
  niveauId: number;
  niveauName: string;
  filiereId: number;
  filiereName: string;
  schoolId: number;
  schoolName: string;
  dateDebut: string;
  dateFin: string | null;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class StudentsManagementService {
  private readonly base    = '/students';
  private readonly affBase = '/v1/etudiants';

  constructor(private api: ApiService) {}

  // ─── Lecture ─────────────────────────────────────────────────────────────

  /**
   * Retourne la liste des étudiants enrichie avec leur groupe actif
   * (filière, niveau, classe) depuis school-service.
   */
  getStudents(): Observable<import('../../features/students/students').Student[]> {
    return this.api.get<any>(this.base).pipe(
      map(r => (r.data ?? r) as StudentBackend[]),
      switchMap(list => {
        if (!list || list.length === 0) return of([]);
        // Pour chaque étudiant, récupérer son affectation active en parallèle
        const requests = list.map(s =>
          this.api.get<any>(`${this.affBase}/${s.id}/groupe`).pipe(
            map(r => r.data ?? null),
            catchError(() => of(null))
          )
        );
        return forkJoin(requests).pipe(
          map((affectations: (AffectationBackend | null)[]) =>
            list.map((s, i) => this.toFrontend(s, affectations[i] ?? null))
          )
        );
      }),
      catchError(() => of([]))
    );
  }

  addStudent(
    dto: Partial<import('../../features/students/students').Student>
  ): Observable<import('../../features/students/students').Student> {
    const safeUsername =
      (dto.matricule ?? '').replace(/[^a-zA-Z0-9_-]/g, '_').trim() ||
      `student_${Date.now()}`;
    const payload = {
      firstName: dto.prenom,
      lastName:  dto.nom,
      email:     dto.email,
      username:  safeUsername,
      password:  'Student@2025',
      role:      'STUDENT',
      enabled:   dto.enabled ?? true,
    };
    return this.api.post<any>(this.base, payload).pipe(
      map(r => this.toFrontend(r.data ?? r, null))
    );
  }

  updateStudent(
    id: number,
    dto: Partial<import('../../features/students/students').Student>
  ): Observable<import('../../features/students/students').Student> {
    const safeUsername = (dto.matricule ?? '').replace(/[^a-zA-Z0-9_-]/g, '_');
    const payload = {
      firstName: dto.prenom,
      lastName:  dto.nom,
      email:     dto.email,
      username:  safeUsername || undefined,
      role:      'STUDENT',
      enabled:   dto.enabled,
    };
    return this.api.put<any>(`${this.base}/${id}`, payload).pipe(
      // After update, re-fetch the affectation to keep classe/filière/niveau in sync
      switchMap(r => {
        const base = this.toFrontend(r.data ?? r, null);
        return this.api.get<any>(`${this.affBase}/${id}/groupe`).pipe(
          map(ar => this.toFrontend(r.data ?? r, ar.data ?? null)),
          catchError(() => of(base))
        );
      })
    );
  }

  deleteStudent(id: number): Observable<boolean> {
    return this.api.delete<void>(`${this.base}/${id}`).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  // ─── Mapping ─────────────────────────────────────────────────────────────

  private toFrontend(
    s: StudentBackend,
    affectation: AffectationBackend | null
  ): import('../../features/students/students').Student {
    return {
      id:            s.id,
      matricule:     s.username  ?? '',
      nom:           s.lastName  ?? '',
      prenom:        s.firstName ?? '',
      email:         s.email     ?? '',
      telephone:     '',
      filiere:       affectation?.filiereName ?? '',
      niveau:        affectation?.niveauName  ?? '',
      classe:        affectation?.groupeName  ?? '',
      dateNaissance: '',
      enabled:       s.enabled,
    };
  }
}
