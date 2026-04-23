import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpEventType, HttpRequest } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, filter } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export type DocumentCategory = 'COURS' | 'TD' | 'TP' | 'EXAMEN' | 'AUTRE';

export interface CourseDocument {
  id: number;
  courseId: number;
  originalFilename: string;
  contentType: string;
  fileSize: number;
  category: DocumentCategory;
  description?: string;
  uploadedBy?: number;
  createdAt: string;
  downloadUrl: string;
}

export interface UploadProgress {
  progress: number;   // 0-100
  done: boolean;
  document?: CourseDocument;
  error?: string;
}

interface ApiWrapped<T> { success: boolean; data: T; message?: string; }

@Injectable({ providedIn: 'root' })
export class CourseDocumentsService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl || 'http://localhost:8080/api'}/v1/courses`;

  /** Récupère les documents d'un cours */
  getDocuments(courseId: number): Observable<CourseDocument[]> {
    return this.http.get<ApiWrapped<CourseDocument[]>>(`${this.base}/${courseId}/documents`).pipe(
      map(res => res?.data ?? (res as any) ?? []),
      catchError(() => of([]))
    );
  }

  /**
   * Upload un fichier avec suivi de progression.
   * Émet des UploadProgress jusqu'à { done: true }.
   */
  uploadDocument(
    courseId: number,
    file: File,
    category: DocumentCategory = 'COURS',
    description = '',
    uploadedBy?: number
  ): Observable<UploadProgress> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    formData.append('category', category);
    if (description) formData.append('description', description);
    if (uploadedBy)  formData.append('uploadedBy', String(uploadedBy));

    const req = new HttpRequest('POST', `${this.base}/${courseId}/documents`, formData, {
      reportProgress: true,
    });

    return this.http.request(req).pipe(
      map(event => {
        if (event.type === HttpEventType.UploadProgress) {
          const progress = event.total
            ? Math.round(100 * event.loaded / event.total)
            : 0;
          return { progress, done: false };
        }
        if (event.type === HttpEventType.Response) {
          const body = event.body as ApiWrapped<CourseDocument>;
          return { progress: 100, done: true, document: body?.data };
        }
        return { progress: 0, done: false };
      }),
      catchError(err => of({
        progress: 0, done: true,
        error: err?.error?.message || 'Erreur lors de l\'upload'
      }))
    );
  }

  /** Supprime un document */
  deleteDocument(courseId: number, documentId: number): Observable<boolean> {
    return this.http.delete<ApiWrapped<void>>(`${this.base}/${courseId}/documents/${documentId}`).pipe(
      map(res => res?.success ?? true),
      catchError(() => of(false))
    );
  }

  /** URL de téléchargement direct */
  getDownloadUrl(courseId: number, documentId: number): string {
    return `${this.base}/${courseId}/documents/${documentId}/download`;
  }

  // ── Helpers UI ────────────────────────────────────────────────────────────

  /** Icône Material selon le type MIME */
  static iconFor(contentType: string): string {
    if (!contentType) return 'insert_drive_file';
    if (contentType.includes('pdf'))         return 'picture_as_pdf';
    if (contentType.includes('word') || contentType.includes('msword')) return 'description';
    if (contentType.includes('presentation') || contentType.includes('powerpoint')) return 'slideshow';
    if (contentType.includes('sheet') || contentType.includes('excel')) return 'table_chart';
    if (contentType.includes('text'))        return 'article';
    if (contentType.includes('image'))       return 'image';
    return 'insert_drive_file';
  }

  /** Couleur selon le type MIME */
  static colorFor(contentType: string): string {
    if (!contentType) return '#6B7280';
    if (contentType.includes('pdf'))         return '#DC2626';
    if (contentType.includes('word') || contentType.includes('msword')) return '#1D4ED8';
    if (contentType.includes('presentation') || contentType.includes('powerpoint')) return '#EA580C';
    if (contentType.includes('sheet') || contentType.includes('excel')) return '#15803D';
    if (contentType.includes('text'))        return '#374151';
    if (contentType.includes('image'))       return '#7C3AED';
    return '#6B7280';
  }

  /** Formate la taille en Ko/Mo */
  static formatSize(bytes: number): string {
    if (!bytes) return '0 o';
    if (bytes < 1024)        return `${bytes} o`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} Mo`;
  }

  /** Label français de la catégorie */
  static categoryLabel(cat: DocumentCategory): string {
    const map: Record<DocumentCategory, string> = {
      COURS: 'Cours', TD: 'TD', TP: 'TP', EXAMEN: 'Examen', AUTRE: 'Autre'
    };
    return map[cat] ?? cat;
  }
}
