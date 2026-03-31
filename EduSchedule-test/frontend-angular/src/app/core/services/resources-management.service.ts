import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
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

export interface EquipmentDashboard {
  totalMateriels: number;
  bonEtat: number;
  usage: number;
  enPanne: number;
  enMaintenance: number;
  tauxDisponibilite: number;
  materielsDueForMaintenance: number;
  maintenancesEnCours: number;
  maintenancesPlanifiees: number;
  topMaterielsUtilises: { materielId: number; nom: string; totalMinutes: number }[];
}

export interface EquipmentAlerte {
  type: string;
  materielId: number;
  nom: string;
  message: string;
  heuresUtilisation?: number;
  seuil?: number;
}

export interface MaintenanceRecord {
  id: number;
  typeIntervention: string;
  statut: string;
  dateDebut: string;
  dateFin?: string;
  description: string;
  technicien?: string;
  coutReparation?: string;
  heuresUtilisationAuMoment?: number;
}

interface ApiWrapped<T> { success: boolean; data: T; }

@Injectable({
  providedIn: 'root'
})
export class ResourcesManagementService {
  private api = inject(ApiService);

  getResources(): Observable<Resource[]> {
    return this.api.get<ApiWrapped<Resource[]>>('/v1/equipment').pipe(
      map(res => {
        const list = res?.data ?? (res as any);
        if (!Array.isArray(list)) return [];
        return list.map((m: any) => ({
          id: m.id,
          name: m.nom ?? m.name,
          type: m.typeMateriel?.nom ?? m.type ?? 'Matériel',
          quantity: 1,
          available: m.etat === 'BON_ETAT' ? 1 : 0,
          location: m.salle ?? m.location ?? '',
          description: m.description ?? '',
          status: (m.etat === 'BON_ETAT' ? 'available' : m.etat === 'EN_PANNE' ? 'unavailable' : 'limited') as Resource['status']
        }));
      }),
      catchError(() => of([]))
    );
  }

  getResourceById(id: number): Observable<Resource> {
    return this.api.get<ApiWrapped<Resource>>(`/v1/equipment/${id}`).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  addResource(resource: Omit<Resource, 'id' | 'createdAt'>): Observable<Resource> {
    const dto = { nom: resource.name, code: resource.name?.substring(0, 10).toUpperCase(), type: resource.type, salle: resource.location, description: resource.description, active: true };
    return this.api.post<ApiWrapped<Resource>>('/v1/equipment', dto).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  updateResource(id: number, resourceData: Partial<Resource>): Observable<Resource> {
    return this.api.put<ApiWrapped<Resource>>(`/v1/equipment/${id}`, resourceData).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  deleteResource(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/equipment/${id}`);
  }

  // ── Équipements (resource-service) ──────────────────────────────────────────

  getEquipmentDashboard(): Observable<EquipmentDashboard> {
    return this.api.get<EquipmentDashboard>('/v1/equipment-usage/dashboard').pipe(
      catchError(() => of({
        totalMateriels: 0, bonEtat: 0, usage: 0, enPanne: 0, enMaintenance: 0,
        tauxDisponibilite: 0, materielsDueForMaintenance: 0,
        maintenancesEnCours: 0, maintenancesPlanifiees: 0, topMaterielsUtilises: []
      }))
    );
  }

  getEquipmentAlertes(): Observable<EquipmentAlerte[]> {
    return this.api.get<EquipmentAlerte[]>('/v1/equipment-usage/alertes').pipe(
      catchError(() => of([]))
    );
  }

  getEquipmentStats(materielId: number): Observable<any> {
    return this.api.get<any>(`/v1/equipment-usage/stats/${materielId}`).pipe(
      catchError(() => of(null))
    );
  }

  getMaintenanceHistory(materielId: number): Observable<MaintenanceRecord[]> {
    return this.api.get<MaintenanceRecord[]>(`/v1/equipment-usage/${materielId}/maintenances`).pipe(
      catchError(() => of([]))
    );
  }

  createMaintenance(materielId: number, data: {
    typeIntervention: string; dateDebut: string; description: string; technicien?: string;
  }): Observable<any> {
    return this.api.post<any>(`/v1/equipment-usage/${materielId}/maintenances`, data);
  }

  completeMaintenance(maintenanceId: number, notes: string, cout?: string): Observable<any> {
    return this.api.put<any>(`/v1/equipment-usage/maintenances/${maintenanceId}/terminer`, { notes, cout });
  }
}
