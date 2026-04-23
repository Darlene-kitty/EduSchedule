import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface SalleMateriel {
  id: number;
  salle: { id: number; code: string; name: string; type: string };
  materiel: {
    id: number; code: string; nom: string;
    typeMateriel?: { code: string; nom: string; icone: string; couleur: string };
    marque: string; modele: string; etat: string;
  };
  quantiteTotale: number;
  quantiteDisponible: number;
  quantiteReservee: number;
  requis: boolean;
  notes: string;
  dateInstallation: string;
}

export interface EquipementReservation {
  id: number;
  reservationId: number;
  salleMateriel: SalleMateriel;
  quantite: number;
  typeCours: string;
  dateDebut: string;
  dateFin: string;
  statut: 'ACTIVE' | 'TERMINEE' | 'ANNULEE';
  notes: string;
}

export interface DisponibiliteEquipement {
  salleId: number;
  typeCours: string;
  toutDisponible: boolean;
  equipements: {
    typeEquipement: string;
    disponible: boolean;
    quantiteDisponible: number;
    requis: boolean;
  }[];
}

interface ApiWrapped<T> { success: boolean; data: T; total?: number; }

@Injectable({ providedIn: 'root' })
export class SalleMaterielService {
  private api = inject(ApiService);

  // ── Inventaire par salle ──────────────────────────────────────────────────

  getInventaireSalle(salleId: number): Observable<SalleMateriel[]> {
    return this.api.get<ApiWrapped<SalleMateriel[]>>(`/v1/salle-materiels/salle/${salleId}`).pipe(
      map(r => r?.data ?? []),
      catchError(() => of([]))
    );
  }

  getEquipementsDisponibles(salleId: number): Observable<SalleMateriel[]> {
    return this.api.get<ApiWrapped<SalleMateriel[]>>(`/v1/salle-materiels/salle/${salleId}/disponibles`).pipe(
      map(r => r?.data ?? []),
      catchError(() => of([]))
    );
  }

  ajouterMaterielDansSalle(salleId: number, materielId: number, quantite: number,
                            requis: boolean, notes: string): Observable<SalleMateriel | null> {
    return this.api.post<ApiWrapped<SalleMateriel>>(
      `/v1/salle-materiels/salle/${salleId}/materiel/${materielId}`,
      { quantite, requis, notes }
    ).pipe(
      map(r => r?.data ?? null),
      catchError(() => of(null))
    );
  }

  mettreAJourQuantite(id: number, quantite: number): Observable<SalleMateriel | null> {
    return this.api.put<ApiWrapped<SalleMateriel>>(`/v1/salle-materiels/${id}/quantite`, { quantite }).pipe(
      map(r => r?.data ?? null),
      catchError(() => of(null))
    );
  }

  retirerMateriel(id: number): Observable<boolean> {
    return this.api.delete<any>(`/v1/salle-materiels/${id}`).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  // ── Disponibilité et allocation ───────────────────────────────────────────

  verifierDisponibilite(salleId: number, typeCours: string,
                         debut: string, fin: string): Observable<DisponibiliteEquipement> {
    const params = `?typeCours=${typeCours}&debut=${encodeURIComponent(debut)}&fin=${encodeURIComponent(fin)}`;
    return this.api.get<DisponibiliteEquipement>(`/v1/salle-materiels/salle/${salleId}/disponibilite${params}`).pipe(
      catchError(() => of({ salleId, typeCours, toutDisponible: false, equipements: [] }))
    );
  }

  getEquipementsRequis(typeCours: string): Observable<string[]> {
    return this.api.get<any>(`/v1/salle-materiels/equipements-requis?typeCours=${typeCours}`).pipe(
      map(r => r?.equipementsRequis ?? []),
      catchError(() => of([]))
    );
  }

  allouerEquipements(reservationId: number, salleId: number, typeCours: string,
                      dateDebut: string, dateFin: string): Observable<EquipementReservation[]> {
    return this.api.post<ApiWrapped<EquipementReservation[]>>('/v1/salle-materiels/allouer', {
      reservationId, salleId, typeCours, dateDebut, dateFin
    }).pipe(
      map(r => r?.data ?? []),
      catchError(() => of([]))
    );
  }

  libererEquipements(reservationId: number): Observable<boolean> {
    return this.api.post<any>(`/v1/salle-materiels/liberer/${reservationId}`, {}).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  // ── Réservations d'équipements ────────────────────────────────────────────

  getReservationsEquipements(reservationId: number): Observable<EquipementReservation[]> {
    return this.api.get<ApiWrapped<EquipementReservation[]>>(`/v1/salle-materiels/reservations/${reservationId}`).pipe(
      map(r => r?.data ?? []),
      catchError(() => of([]))
    );
  }

  // ── Statistiques ──────────────────────────────────────────────────────────

  getStatistiques(): Observable<any> {
    return this.api.get<any>('/v1/salle-materiels/statistiques').pipe(
      catchError(() => of(null))
    );
  }
}
