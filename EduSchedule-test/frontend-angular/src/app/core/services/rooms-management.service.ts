import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface Room {
  id: number;
  name: string;
  building?: string;
  capacity: number;
  type: string;
  equipment?: string[];
  status?: 'available' | 'occupied' | 'maintenance';
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class RoomsManagementService {
  private api = inject(ApiService);

  getRooms(): Observable<Room[]> {
    return this.api.get<Room[]>('/v1/salles');
  }

  getRoomById(id: number): Observable<Room> {
    return this.api.get<Room>(`/v1/salles/${id}`);
  }

  addRoom(room: Omit<Room, 'id' | 'createdAt'>): Observable<Room> {
    return this.api.post<Room>('/v1/salles', room);
  }

  updateRoom(id: number, roomData: Partial<Room>): Observable<Room> {
    return this.api.put<Room>(`/v1/salles/${id}`, roomData);
  }

  deleteRoom(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/salles/${id}`);
  }

  getAvailableRooms(): Observable<Room[]> {
    return this.api.get<Room[]>('/v1/salles/disponibles');
  }
}
