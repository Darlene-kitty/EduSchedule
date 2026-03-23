import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface GroupeBackend {
  id: number;
  name: string;
  code: string;
  capacite: number;
  niveauId: number;
  niveauName: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class GroupesManagementService {
  private readonly base = '/v1/groupes';

  constructor(private api: ApiService) {}

  getAll(niveauId?: number): Observable<GroupeBackend[]> {
    const params = niveauId ? `?niveauId=${niveauId}` : '';
    return this.api.get<any>(`${this.base}${params}`).pipe(
      map(r => r.data ?? r),
      catchError(() => of([]))
    );
  }

  getById(id: number): Observable<GroupeBackend | null> {
    return this.api.get<any>(`${this.base}/${id}`).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }

  create(dto: Partial<GroupeBackend>): Observable<GroupeBackend | null> {
    return this.api.post<any>(this.base, dto).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }

  update(id: number, dto: Partial<GroupeBackend>): Observable<GroupeBackend | null> {
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
