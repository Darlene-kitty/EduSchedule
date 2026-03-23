import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface CategorieUEBackend {
  id: number;
  code: string;
  nom: string;
  type: string;
  credits: number;
  volumeHoraire: number;
  coefficient: number;
  description: string;
  couleur: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class CategoriesUeManagementService {
  private readonly base = '/v1/categories-ue';

  constructor(private api: ApiService) {}

  getAll(): Observable<CategorieUEBackend[]> {
    return this.api.get<any>(this.base).pipe(
      map(r => r.data ?? r),
      catchError(() => of([]))
    );
  }

  getById(id: number): Observable<CategorieUEBackend | null> {
    return this.api.get<any>(`${this.base}/${id}`).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }

  create(dto: Partial<CategorieUEBackend>): Observable<CategorieUEBackend | null> {
    return this.api.post<any>(this.base, dto).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }

  update(id: number, dto: Partial<CategorieUEBackend>): Observable<CategorieUEBackend | null> {
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
