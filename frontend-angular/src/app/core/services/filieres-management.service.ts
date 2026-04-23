import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface FiliereBackend {
  id: number;
  name: string;
  code: string;
  description: string;
  schoolId: number;
  schoolName: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class FilieresManagementService {
  private readonly base = '/v1/filieres';

  constructor(private api: ApiService) {}

  getAll(schoolId?: number): Observable<FiliereBackend[]> {
    const params = schoolId ? `?schoolId=${schoolId}` : '';
    return this.api.get<any>(`${this.base}${params}`).pipe(
      map(r => r.data ?? r),
      catchError(() => of([]))
    );
  }

  getById(id: number): Observable<FiliereBackend | null> {
    return this.api.get<any>(`${this.base}/${id}`).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }

  create(dto: Partial<FiliereBackend>): Observable<FiliereBackend | null> {
    return this.api.post<any>(this.base, dto).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }

  update(id: number, dto: Partial<FiliereBackend>): Observable<FiliereBackend | null> {
    return this.api.put<any>(`${this.base}/${id}`, dto).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }

  delete(id: number): Observable<boolean> {
    return this.api.delete<any>(`${this.base}/${id}`).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }
}
