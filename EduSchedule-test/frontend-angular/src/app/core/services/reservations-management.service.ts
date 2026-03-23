import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface Reservation {
  id: number;
  roomName?: string;
  roomId?: number;
  resourceId?: number;
  date?: string;
  startTime?: string;
  endTime?: string;
  startDateTime?: string;
  endDateTime?: string;
  purpose?: string;
  title?: string;
  requestedBy?: string;
  userId?: number;
  status: 'pending' | 'approved' | 'rejected' | 'cancelled' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  notes?: string;
  createdAt?: string;
}

interface ApiWrapped<T> { success: boolean; data: T; }

@Injectable({
  providedIn: 'root'
})
export class ReservationsManagementService {
  private api = inject(ApiService);

  getReservations(): Observable<Reservation[]> {
    return this.api.get<ApiWrapped<Reservation[]>>('/v1/reservations').pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  getReservationById(id: number): Observable<Reservation> {
    return this.api.get<ApiWrapped<Reservation>>(`/v1/reservations/${id}`).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  addReservation(reservation: Omit<Reservation, 'id' | 'createdAt'>): Observable<Reservation> {
    return this.api.post<ApiWrapped<Reservation>>('/v1/reservations', reservation).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  updateReservation(id: number, reservationData: Partial<Reservation>): Observable<Reservation> {
    return this.api.put<ApiWrapped<Reservation>>(`/v1/reservations/${id}`, reservationData).pipe(
      map(res => res?.data ?? (res as any))
    );
  }

  deleteReservation(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/reservations/${id}`);
  }

  approveReservation(id: number): Observable<void> {
    return this.api.post<void>(`/v1/reservations/${id}/approve`, {});
  }

  rejectReservation(id: number): Observable<void> {
    return this.api.post<void>(`/v1/reservations/${id}/reject`, {});
  }

  cancelReservation(id: number): Observable<void> {
    return this.api.post<void>(`/v1/reservations/${id}/cancel`, {});
  }
}
