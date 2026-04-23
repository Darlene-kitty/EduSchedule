import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface NiveauBackend {
  id: number;
  name: string;
  code: string;
  ordre: number;
  filiereId: number;
  filiereName: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class NiveauxManagementService {
  private readonly base = '/v1/niveaux';

  constructor(private api: ApiService) {}

  getAll(filiereId?: number): Observable<NiveauBackend[]> {
    const params = filiereId ? `?filiereId=${filiereId}` : '';
    return this.api.get<any>(`${this.base}${params}`).pipe(
      map(r => r.data ?? r),
      catchError(() => of([]))
    );
  }

  getById(id: number): Observable<NiveauBackend | null> {
    return this.api.get<any>(`${this.base}/${id}`).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }

  create(dto: Partial<NiveauBackend>): Observable<NiveauBackend | null> {
    return this.api.post<any>(this.base, dto).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }

  update(id: number, dto: Partial<NiveauBackend>): Observable<NiveauBackend | null> {
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
