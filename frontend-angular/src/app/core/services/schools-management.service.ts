import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { ApiService } from './api.service';

export interface School {
  id: number;
  sigle: string;
  nom: string;
  directeur: string;
  email: string;
  telephone: string;
  description: string;
  filieres: string[];
  niveaux: string[];
  couleur: string;
  enabled: boolean;
}

interface ApiWrapped<T> { success: boolean; data: T; }

@Injectable({
  providedIn: 'root'
})
export class SchoolsManagementService {
  private api = inject(ApiService);

  getSchools(): Observable<School[]> {
    return this.api.get<ApiWrapped<School[]>>('/v1/schools').pipe(
      map(res => res?.data ?? (res as any)),
      catchError(() => of([]))
    );
  }

  getSchoolById(id: number): Observable<School> {
    return this.api.get<ApiWrapped<School>>(`/v1/schools/${id}`).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  addSchool(school: Omit<School, 'id'>): Observable<School> {
    return this.api.post<ApiWrapped<School>>('/v1/schools', school).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  updateSchool(id: number, data: Partial<School>): Observable<School> {
    return this.api.put<ApiWrapped<School>>(`/v1/schools/${id}`, data).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  deleteSchool(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/schools/${id}`);
  }
}
