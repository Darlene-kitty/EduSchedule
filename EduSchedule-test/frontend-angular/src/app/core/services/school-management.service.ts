import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface SchoolEntry {
  id: number;
  sigle?: string;
  code?: string;
  nom?: string;
  name?: string;
  directeur?: string;
  email?: string;
  telephone?: string;
  description?: string;
  filieres?: string[];
  niveaux?: string[];
  couleur?: string;
  enabled?: boolean;
  active?: boolean;
}

interface ApiWrapped<T> { success: boolean; data: T; }

@Injectable({ providedIn: 'root' })
export class SchoolManagementService {
  private api = inject(ApiService);

  getAll(): Observable<SchoolEntry[]> {
    return this.api.get<ApiWrapped<SchoolEntry[]>>('/v1/schools').pipe(
      map(res => res?.data ?? (res as any)),
      catchError(() => of([]))
    );
  }

  getById(id: number): Observable<SchoolEntry> {
    return this.api.get<ApiWrapped<SchoolEntry>>(`/v1/schools/${id}`).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  create(school: Omit<SchoolEntry, 'id'>): Observable<SchoolEntry> {
    return this.api.post<ApiWrapped<SchoolEntry>>('/v1/schools', school).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  update(id: number, school: Partial<SchoolEntry>): Observable<SchoolEntry> {
    return this.api.put<ApiWrapped<SchoolEntry>>(`/v1/schools/${id}`, school).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/schools/${id}`);
  }
}
