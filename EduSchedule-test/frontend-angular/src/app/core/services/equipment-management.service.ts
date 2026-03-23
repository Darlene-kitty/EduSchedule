import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface TypeMaterielBackend {
  id: number;
  code: string;
  nom: string;
  icone: string;
  couleur: string;
  description: string;
  active: boolean;
}

export interface MaterielBackend {
  id: number;
  code: string;
  nom: string;
  typeMateriel?: TypeMaterielBackend;
  typeMaterielId?: number;
  marque: string;
  modele: string;
  numeroSerie: string;
  ecole: string;
  salle: string;
  etat: string;
  dateAcquisition: string;
  valeur: number;
  description: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class EquipmentManagementService {
  constructor(private api: ApiService) {}

  // ── Types de matériel ──
  getAllTypes(): Observable<TypeMaterielBackend[]> {
    return this.api.get<any>('/v1/equipment-types').pipe(
      map(r => r.data ?? r),
      catchError(() => of([]))
    );
  }

  createType(dto: Partial<TypeMaterielBackend>): Observable<TypeMaterielBackend | null> {
    return this.api.post<any>('/v1/equipment-types', dto).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }

  updateType(id: number, dto: Partial<TypeMaterielBackend>): Observable<TypeMaterielBackend | null> {
    return this.api.put<any>(`/v1/equipment-types/${id}`, dto).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }

  deleteType(id: number): Observable<boolean> {
    return this.api.delete<any>(`/v1/equipment-types/${id}`).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  // ── Matériels ──
  getAllMateriels(typeId?: number, ecole?: string): Observable<MaterielBackend[]> {
    let params = '';
    if (typeId) params = `?typeId=${typeId}`;
    else if (ecole) params = `?ecole=${encodeURIComponent(ecole)}`;
    return this.api.get<any>(`/v1/equipment${params}`).pipe(
      map(r => r.data ?? r),
      catchError(() => of([]))
    );
  }

  createMateriel(dto: Partial<MaterielBackend>): Observable<MaterielBackend | null> {
    return this.api.post<any>('/v1/equipment', dto).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }

  updateMateriel(id: number, dto: Partial<MaterielBackend>): Observable<MaterielBackend | null> {
    return this.api.put<any>(`/v1/equipment/${id}`, dto).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }

  deleteMateriel(id: number): Observable<boolean> {
    return this.api.delete<any>(`/v1/equipment/${id}`).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }
}
