import { Injectable, inject } from '@angular/core';
import { Observable, map, catchError, of } from 'rxjs';
import { ApiService } from './api.service';

/** Créneau en conflit ou relaxé retourné par le backend */
export interface PendingAdjustment {
  id: number;
  courseCode: string;
  courseName: string;
  teacherId: number;
  roomId: number;
  roomName: string;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  level: string;
  semester: string;
  schoolId: number;
  status: 'CONFLICT' | 'RELAXED';
  conflictReason: string;
}

/** Suggestion de créneau alternatif calculée dynamiquement par le backend */
export interface AlternativeSuggestion {
  slotKey: string;   // ex: "LUNDI_08:00"
  label: string;     // texte affiché
  score: number;     // 0–100
  fullyAvailable: boolean;
}

interface ApiWrapped<T> { success: boolean; data?: T; suggestions?: T; total?: number; }

@Injectable({ providedIn: 'root' })
export class ConflictsManagementService {
  private api = inject(ApiService);

  /**
   * Récupère tous les créneaux en conflit ou relaxés (toutes écoles).
   * GET /api/v1/timetable/adjustments
   */
  getAllAdjustments(): Observable<PendingAdjustment[]> {
    return this.api.get<ApiWrapped<PendingAdjustment[]>>('/v1/timetable/adjustments').pipe(
      map(res => (res?.data ?? []) as PendingAdjustment[]),
      catchError(() => of([]))
    );
  }

  /**
   * Récupère les créneaux en conflit ou relaxés pour un enseignant donné.
   * GET /api/v1/timetable/adjustments/teacher/{teacherId}
   */
  getAdjustmentsByTeacher(teacherId: number): Observable<PendingAdjustment[]> {
    return this.api.get<ApiWrapped<PendingAdjustment[]>>(`/v1/timetable/adjustments/teacher/${teacherId}`).pipe(
      map(res => (res?.data ?? []) as PendingAdjustment[]),
      catchError(() => of([]))
    );
  }

  /**
   * Récupère les suggestions alternatives calculées dynamiquement pour un créneau.
   * GET /api/v1/timetable/adjustments/{slotId}/suggestions
   */
  getSuggestions(slotId: number): Observable<AlternativeSuggestion[]> {
    return this.api.get<any>(`/v1/timetable/adjustments/${slotId}/suggestions`).pipe(
      map(res => (res?.suggestions ?? res?.data ?? []) as AlternativeSuggestion[]),
      catchError(() => of([]))
    );
  }

  /**
   * Applique une solution de résolution de conflit.
   * POST /api/v1/timetable/adjustments/{slotId}/resolve
   */
  resolveAdjustment(slotId: number, slotKey: string): Observable<{ success: boolean; message: string }> {
    return this.api.post<any>(
      `/v1/timetable/adjustments/${slotId}/resolve`,
      { slotKey }
    ).pipe(
      map(res => ({ success: true, message: res?.message ?? 'Résolu' })),
      catchError(() => of({ success: false, message: 'Erreur lors de la résolution' }))
    );
  }

  /**
   * Déclenche une vérification de cohérence planning ↔ réservations.
   * POST /api/v1/timetable/sync-check
   */
  syncCheck(schoolId: number, semester: string, level: string): Observable<{ conflicts: any[]; total: number; message: string }> {
    return this.api.post<any>(
      `/v1/timetable/sync-check?schoolId=${schoolId}&semester=${encodeURIComponent(semester)}&level=${encodeURIComponent(level)}`,
      null
    ).pipe(
      map(res => ({ conflicts: res?.conflicts ?? [], total: res?.total ?? 0, message: res?.message ?? '' })),
      catchError(() => of({ conflicts: [], total: 0, message: 'Erreur lors de la vérification' }))
    );
  }
}
