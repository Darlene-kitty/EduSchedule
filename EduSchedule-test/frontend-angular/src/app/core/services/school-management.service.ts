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
  phone?: string;
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
    // Mapper les champs frontend vers les champs backend attendus
    const payload: any = {
      ...school,
      name: (school as any).nom || school.name || school.sigle || '',
      code: school.sigle || school.code || '',
      phone: (school as any).telephone || school.phone || '',
      active: school.enabled ?? school.active ?? true,
    };
    return this.api.post<ApiWrapped<SchoolEntry>>('/v1/schools', payload).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  update(id: number, school: Partial<SchoolEntry>): Observable<SchoolEntry> {
    const payload: any = {
      ...school,
      name: (school as any).nom || school.name || school.sigle || undefined,
      code: school.sigle || school.code || undefined,
      phone: (school as any).telephone || school.phone || undefined,
      active: school.enabled ?? school.active ?? undefined,
    };
    return this.api.put<ApiWrapped<SchoolEntry>>(`/v1/schools/${id}`, payload).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/schools/${id}`);
  }
}
