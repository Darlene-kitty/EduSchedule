import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ApiService } from './api.service';

/** Valeurs de l'enum EventType Java */
export type EventType =
  | 'SEMINAR' | 'CONFERENCE' | 'WORKSHOP' | 'MEETING'
  | 'EXAM' | 'DEFENSE' | 'CEREMONY' | 'TRAINING'
  | 'COMPETITION' | 'OTHER';

/** Valeurs de l'enum EventStatus Java */
export type EventStatus =
  | 'PLANNED' | 'CONFIRMED' | 'IN_PROGRESS'
  | 'COMPLETED' | 'CANCELLED' | 'POSTPONED';

/** Aligné sur EventDTO.java */
export interface Event {
  id: number;
  title: string;
  description?: string;
  type: EventType;
  startDateTime: string;   // ISO LocalDateTime
  endDateTime: string;
  resourceId?: number;
  organizerId?: number;
  maxParticipants?: number;
  currentParticipants?: number;
  status?: EventStatus;
  isPublic?: boolean;
  requiresApproval?: boolean;
  createdAt?: string;
  updatedAt?: string;
  // Champs enrichis
  resourceName?: string;
  organizerName?: string;
}

/** Aligné sur EventRequest.java */
export interface EventPayload {
  title: string;
  description?: string;
  type: EventType;
  startDateTime: string;   // ISO : "2025-11-01T09:00:00"
  endDateTime: string;
  resourceId: number;
  organizerId: number;
  maxParticipants?: number;
  registrationRequired?: boolean;
  registrationDeadline?: string;
  equipmentNeeded?: string;
  specialRequirements?: string;
}

interface ApiWrapped<T> { success: boolean; data: T; }

@Injectable({ providedIn: 'root' })
export class EventsManagementService {
  private api = inject(ApiService);

  getEvents(): Observable<Event[]> {
    return this.api.get<ApiWrapped<Event[]>>('/v1/events').pipe(
      map(res => res?.data ?? (res as any)),
      catchError(() => of([]))
    );
  }

  getEventById(id: number): Observable<Event> {
    return this.api.get<ApiWrapped<Event>>(`/v1/events/${id}`).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  addEvent(payload: EventPayload): Observable<Event> {
    return this.api.post<ApiWrapped<Event>>('/v1/events', payload).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  updateEvent(id: number, payload: Partial<EventPayload>): Observable<Event> {
    return this.api.put<ApiWrapped<Event>>(`/v1/events/${id}`, payload).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  deleteEvent(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/events/${id}`);
  }

  getUpcomingEvents(): Observable<Event[]> {
    return this.api.get<ApiWrapped<Event[]>>('/v1/events/upcoming').pipe(
      map(res => res?.data ?? (res as any)),
      catchError(() => of([]))
    );
  }

  // ── Helpers de mapping UI ──────────────────────────────────────────────────

  /** Convertit EventType backend → label français */
  static typeLabel(type: EventType): string {
    const map: Record<EventType, string> = {
      SEMINAR: 'Séminaire', CONFERENCE: 'Conférence', WORKSHOP: 'Atelier',
      MEETING: 'Réunion', EXAM: 'Examen', DEFENSE: 'Soutenance',
      CEREMONY: 'Cérémonie', TRAINING: 'Formation',
      COMPETITION: 'Compétition', OTHER: 'Autre'
    };
    return map[type] ?? type;
  }

  /** Convertit EventStatus backend → label français */
  static statusLabel(status: EventStatus): string {
    const map: Record<EventStatus, string> = {
      PLANNED: 'Planifié', CONFIRMED: 'Confirmé', IN_PROGRESS: 'En cours',
      COMPLETED: 'Terminé', CANCELLED: 'Annulé', POSTPONED: 'Reporté'
    };
    return map[status] ?? status;
  }

  /** Formate un ISO LocalDateTime en heure courte "HH:mm" */
  static toTime(iso: string): string {
    if (!iso) return '';
    try { return new Date(iso).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }); }
    catch { return ''; }
  }

  /** Formate un ISO LocalDateTime en date longue française */
  static toDate(iso: string): string {
    if (!iso) return '';
    try { return new Date(iso).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' }); }
    catch { return ''; }
  }

  /** Construit un ISO LocalDateTime depuis date "YYYY-MM-DD" + heure "HH:mm" */
  static toISO(date: string, time: string): string {
    if (!date) return '';
    return `${date}T${time || '00:00'}:00`;
  }
}
