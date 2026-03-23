import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { ApiService } from './api.service';

export interface Resource {
  id: number;
  name: string;
  type: string;
  quantity?: number;
  available?: number;
  location?: string;
  description?: string;
  status?: 'available' | 'limited' | 'unavailable';
  createdAt?: string;
}

interface ApiWrapped<T> { success: boolean; data: T; }

@Injectable({
  providedIn: 'root'
})
export class ResourcesManagementService {
  private api = inject(ApiService);

  getResources(): Observable<Resource[]> {
    return this.api.get<ApiWrapped<Resource[]>>('/resources').pipe(
      map(res => res?.data ?? (res as any)),
      catchError(() => of([]))
    );
  }

  getResourceById(id: number): Observable<Resource> {
    return this.api.get<ApiWrapped<Resource>>(`/resources/${id}`).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  addResource(resource: Omit<Resource, 'id' | 'createdAt'>): Observable<Resource> {
    return this.api.post<ApiWrapped<Resource>>('/resources', resource).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  updateResource(id: number, resourceData: Partial<Resource>): Observable<Resource> {
    return this.api.put<ApiWrapped<Resource>>(`/resources/${id}`, resourceData).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  deleteResource(id: number): Observable<void> {
    return this.api.delete<void>(`/resources/${id}`);
  }
}
