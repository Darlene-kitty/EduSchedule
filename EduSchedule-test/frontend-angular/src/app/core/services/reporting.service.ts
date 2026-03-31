import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export type ReportType =
  | 'USER_STATISTICS' | 'COURSE_UTILIZATION' | 'ROOM_OCCUPANCY'
  | 'RESERVATION_SUMMARY' | 'SCHEDULE_OVERVIEW' | 'ATTENDANCE_REPORT'
  | 'RESOURCE_USAGE' | 'MONTHLY_SUMMARY' | 'YEARLY_SUMMARY' | 'CUSTOM_REPORT';

export type ReportFormat = 'PDF' | 'EXCEL' | 'CSV' | 'JSON';

export type ReportStatus = 'PENDING' | 'GENERATING' | 'COMPLETED' | 'FAILED' | 'EXPIRED';

export interface ReportRequest {
  type: ReportType;
  format: ReportFormat;
  title?: string;
  description?: string;
  startDate?: string;   // YYYY-MM-DD
  endDate?: string;
  userId?: number;
  courseId?: number;
  resourceId?: number;
  department?: string;
  level?: string;
  customParameters?: Record<string, unknown>;
}

export interface ReportDTO {
  id: number;
  title: string;
  description?: string;
  type: ReportType;
  format: ReportFormat;
  status: ReportStatus;
  filePath?: string;
  fileSize?: number;
  generatedBy?: number;
  generatedAt?: string;
  downloadUrl?: string;
  errorMessage?: string;
  createdAt?: string;
}

export interface StatisticsDTO {
  totalReports?: number;
  completedReports?: number;
  failedReports?: number;
  [key: string]: unknown;
}

@Injectable({ providedIn: 'root' })
export class ReportingService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl || 'http://localhost:8080/api'}/v1/reports`;

  /** Génère un rapport côté serveur et retourne le ReportDTO */
  generate(request: ReportRequest): Observable<ReportDTO> {
    return this.http.post<ReportDTO>(`${this.base}/generate`, request).pipe(
      catchError(() => of({ id: 0, title: request.title ?? '', type: request.type, format: request.format, status: 'FAILED' as ReportStatus }))
    );
  }

  /** Génération asynchrone (retourne immédiatement, polling nécessaire) */
  generateAsync(request: ReportRequest): Observable<ReportDTO> {
    return this.http.post<ReportDTO>(`${this.base}/generate-async`, request);
  }

  /** Récupère un rapport par ID */
  getById(id: number): Observable<ReportDTO> {
    return this.http.get<ReportDTO>(`${this.base}/${id}`);
  }

  /** Historique des rapports de l'utilisateur courant */
  getMyReports(userId: number): Observable<{ content: ReportDTO[] }> {
    return this.http.get<{ content: ReportDTO[] }>(`${this.base}/user/${userId}`).pipe(
      catchError(() => of({ content: [] }))
    );
  }

  /** Tous les rapports (admin) */
  getAll(): Observable<{ content: ReportDTO[] }> {
    return this.http.get<{ content: ReportDTO[] }>(this.base).pipe(
      catchError(() => of({ content: [] }))
    );
  }

  /** Statistiques système */
  getStatistics(): Observable<StatisticsDTO> {
    return this.http.get<StatisticsDTO>(`${this.base}/statistics`).pipe(
      catchError(() => of({}))
    );
  }

  /** Supprime un rapport */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  /** URL de téléchargement d'un rapport */
  downloadUrl(id: number): string {
    return `${this.base}/${id}/download`;
  }

  /** Déclenche le téléchargement du fichier dans le navigateur */
  downloadFile(report: ReportDTO): void {
    if (!report.id) return;
    const url = this.downloadUrl(report.id);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${report.title ?? 'rapport'}.${report.format.toLowerCase()}`;
    a.click();
  }

  // ── Labels ────────────────────────────────────────────────────────────────

  static typeLabel(t: ReportType): string {
    const map: Record<ReportType, string> = {
      USER_STATISTICS:    'Statistiques utilisateurs',
      COURSE_UTILIZATION: 'Utilisation des cours',
      ROOM_OCCUPANCY:     'Occupation des salles',
      RESERVATION_SUMMARY:'Résumé des réservations',
      SCHEDULE_OVERVIEW:  'Vue d\'ensemble EDT',
      ATTENDANCE_REPORT:  'Rapport de présence',
      RESOURCE_USAGE:     'Utilisation des ressources',
      MONTHLY_SUMMARY:    'Résumé mensuel',
      YEARLY_SUMMARY:     'Résumé annuel',
      CUSTOM_REPORT:      'Rapport personnalisé',
    };
    return map[t] ?? t;
  }

  static statusLabel(s: ReportStatus): string {
    const map: Record<ReportStatus, string> = {
      PENDING:    'En attente', GENERATING: 'En cours',
      COMPLETED:  'Terminé',   FAILED:     'Échec', EXPIRED: 'Expiré'
    };
    return map[s] ?? s;
  }

  static statusClass(s: ReportStatus): string {
    const map: Record<ReportStatus, string> = {
      PENDING: 'status-pending', GENERATING: 'status-generating',
      COMPLETED: 'status-completed', FAILED: 'status-failed', EXPIRED: 'status-expired'
    };
    return map[s] ?? '';
  }
}
