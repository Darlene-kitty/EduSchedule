import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { StorageService } from './storage.service';

export interface Reservation {
  id: number;
  roomName: string;
  roomId: number;
  date: string;
  startTime: string;
  endTime: string;
  purpose: string;
  requestedBy: string;
  status: 'pending' | 'approved' | 'rejected' | 'cancelled';
  notes?: string;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReservationsManagementService {
  private storageService = inject(StorageService);
  private reservationsSubject = new BehaviorSubject<Reservation[]>([]);
  reservations$ = this.reservationsSubject.asObservable();

  constructor() {
    this.loadReservationsFromStorage();
  }

  private loadReservationsFromStorage(): void {
    const reservations = this.storageService.getItem<Reservation[]>('reservationsList') || this.getDefaultReservations();
    this.reservationsSubject.next(reservations);
  }

  private getDefaultReservations(): Reservation[] {
    return [
      {
        id: 1,
        roomName: 'A101',
        roomId: 1,
        date: '2024-03-15',
        startTime: '09:00',
        endTime: '11:00',
        purpose: 'Cours de Mathématiques',
        requestedBy: 'Dr. Martin',
        status: 'approved',
        createdAt: '2024-02-20'
      },
      {
        id: 2,
        roomName: 'B203',
        roomId: 2,
        date: '2024-03-16',
        startTime: '14:00',
        endTime: '16:00',
        purpose: 'TP Physique',
        requestedBy: 'Prof. Dubois',
        status: 'pending',
        createdAt: '2024-02-21'
      }
    ];
  }

  private saveReservations(reservations: Reservation[]): void {
    this.storageService.setItem('reservationsList', reservations);
    this.reservationsSubject.next(reservations);
  }

  getReservations(): Observable<Reservation[]> {
    return this.reservations$;
  }

  getReservationById(id: number): Reservation | undefined {
    return this.reservationsSubject.value.find(r => r.id === id);
  }

  addReservation(reservation: Omit<Reservation, 'id' | 'createdAt'>): Observable<Reservation> {
    return new Observable(observer => {
      setTimeout(() => {
        const reservations = this.reservationsSubject.value;
        const newReservation: Reservation = {
          ...reservation,
          id: Date.now(),
          createdAt: new Date().toISOString().split('T')[0],
          status: reservation.status || 'pending'
        };
        
        const updatedReservations = [...reservations, newReservation];
        this.saveReservations(updatedReservations);
        
        observer.next(newReservation);
        observer.complete();
      }, 300);
    });
  }

  updateReservation(id: number, reservationData: Partial<Reservation>): Observable<Reservation> {
    return new Observable(observer => {
      setTimeout(() => {
        const reservations = this.reservationsSubject.value;
        const index = reservations.findIndex(r => r.id === id);
        
        if (index !== -1) {
          const updatedReservation = { ...reservations[index], ...reservationData };
          const updatedReservations = [...reservations];
          updatedReservations[index] = updatedReservation;
          
          this.saveReservations(updatedReservations);
          observer.next(updatedReservation);
        } else {
          observer.error(new Error('Réservation non trouvée'));
        }
        
        observer.complete();
      }, 300);
    });
  }

  deleteReservation(id: number): Observable<void> {
    return new Observable(observer => {
      setTimeout(() => {
        const reservations = this.reservationsSubject.value;
        const updatedReservations = reservations.filter(r => r.id !== id);
        
        this.saveReservations(updatedReservations);
        observer.next();
        observer.complete();
      }, 300);
    });
  }

  approveReservation(id: number): Observable<Reservation> {
    return this.updateReservation(id, { status: 'approved' });
  }

  rejectReservation(id: number): Observable<Reservation> {
    return this.updateReservation(id, { status: 'rejected' });
  }

  cancelReservation(id: number): Observable<Reservation> {
    return this.updateReservation(id, { status: 'cancelled' });
  }
}
