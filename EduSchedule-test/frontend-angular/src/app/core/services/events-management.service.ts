import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { ApiService } from './api.service';

export interface Event {
  id: number;
  title: string;
  description: string;
  date: string;
  startTime: string;
  endTime: string;
  location: string;
  organizer: string;
  type: 'conference' | 'workshop' | 'seminar' | 'meeting' | 'other';
  participants?: number;
  status?: 'scheduled' | 'ongoing' | 'completed' | 'cancelled';
  createdAt?: string;
}

interface ApiWrapped<T> { success: boolean; data: T; }

@Injectable({
  providedIn: 'root'
})
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

  addEvent(event: Omit<Event, 'id' | 'createdAt'>): Observable<Event> {
    return this.api.post<ApiWrapped<Event>>('/v1/events', event).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  updateEvent(id: number, eventData: Partial<Event>): Observable<Event> {
    return this.api.put<ApiWrapped<Event>>(`/v1/events/${id}`, eventData).pipe(
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
}
