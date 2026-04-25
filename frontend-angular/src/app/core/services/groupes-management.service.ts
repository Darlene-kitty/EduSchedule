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
  niveauCode: string;
  // Filière
  filiereId: number;
  filiereName: string;
  filiereCode: string;
  // École
  schoolId: number;
  schoolName: string;
  schoolCode: string;
  schoolCouleur: string;
  active: boolean;
  /** Effectif réel calculé depuis les affectations actives */
  effectif: number;
  /** Places disponibles = capacite - effectif */
  placesDisponibles: number;
}

export interface AffectationBackend {
  id: number;
  etudiantId: number;
  groupeId: number;
  groupeName: string;
  groupeCode: string;
  niveauId: number;
  niveauName: string;
  dateDebut: string;
  dateFin: string | null;
  active: boolean;
}

export interface AutoAffectationResult {
  totalDemandes: number;
  affectes: number;
  ignores: number;
  rejetes: number;
  affectations: AffectationBackend[];
  etudiantsRejetes: number[];
  message: string;
}

@Injectable({ providedIn: 'root' })
export class GroupesManagementService {
  private readonly base = '/v1/groupes';
  private readonly niveauxBase = '/v1/niveaux';
  private readonly etudiantsBase = '/v1/etudiants';

  constructor(private api: ApiService) {}

  // ─── CRUD Groupes ────────────────────────────────────────────────────────

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

  // ─── Affectations ────────────────────────────────────────────────────────

  /** Liste les étudiants actifs d'un groupe */
  getEtudiantsByGroupe(groupeId: number): Observable<AffectationBackend[]> {
    return this.api.get<any>(`${this.base}/${groupeId}/etudiants`).pipe(
      map(r => r.data ?? r),
      catchError(() => of([]))
    );
  }

  /** Retourne le groupe actif d'un étudiant */
  getGroupeByEtudiant(etudiantId: number): Observable<AffectationBackend | null> {
    return this.api.get<any>(`${this.etudiantsBase}/${etudiantId}/groupe`).pipe(
      map(r => r.data ?? null),
      catchError(() => of(null))
    );
  }

  /**
   * Affecte manuellement un étudiant à un groupe.
   * Si l'étudiant est déjà dans un autre groupe, l'ancienne affectation est clôturée.
   */
  affecter(groupeId: number, etudiantId: number): Observable<AffectationBackend | null> {
    return this.api.post<any>(`${this.base}/${groupeId}/affecter`, { etudiantId }).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }

  /** Retire un étudiant d'un groupe */
  desaffecter(groupeId: number, etudiantId: number): Observable<boolean> {
    return this.api.delete<any>(`${this.base}/${groupeId}/etudiants/${etudiantId}`).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  /**
   * Auto-affecte une liste d'étudiants aux groupes d'un niveau via round-robin.
   * @param niveauId  ID du niveau cible
   * @param etudiantIds  IDs des étudiants à distribuer
   * @param forceReaffectation  Si true, réaffecte même les étudiants déjà dans un groupe
   */
  autoAffecter(
    niveauId: number,
    etudiantIds: number[],
    forceReaffectation = false
  ): Observable<AutoAffectationResult | null> {
    return this.api.post<any>(
      `${this.niveauxBase}/${niveauId}/auto-affecter`,
      { etudiantIds, forceReaffectation }
    ).pipe(
      map(r => r.data ?? r),
      catchError(() => of(null))
    );
  }
}
