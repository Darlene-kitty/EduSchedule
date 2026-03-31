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

// Matches the backend Salle entity exactly
interface SallePayload {
  code: string;
  name: string;
  batiment?: string;
  etage?: string;
  capacite?: number;
  type?: string;
  disponible?: boolean;
  active?: boolean;
}

const TYPE_MAP: Record<string, string> = {
  'Amphithéâtre':       'AMPHITHEATRE',
  'Salle de cours':     'SALLE_COURS',
  'Salle TP':           'SALLE_TP',
  'Salle informatique': 'SALLE_COURS',
  'Laboratoire':        'LABORATOIRE',
  'Salle de réunion':   'SALLE_TD',
};

function toPayload(room: Omit<Room, 'id' | 'createdAt'>, code = ''): SallePayload {
  return {
    code,
    name: room.name,
    batiment: room.building,
    capacite: room.capacity,
    type: TYPE_MAP[room.type] ?? room.type,
    disponible: room.status === 'available',
    active: true,
  };
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

  addRoom(room: Omit<Room, 'id' | 'createdAt'>, code: string): Observable<Room> {
    return this.api.post<Room>('/v1/salles', toPayload(room, code));
  }

  updateRoom(id: number, roomData: Omit<Room, 'id' | 'createdAt'>, code: string): Observable<Room> {
    return this.api.put<Room>(`/v1/salles/${id}`, toPayload(roomData, code));
  }

  deleteRoom(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/salles/${id}`);
  }

  getAvailableRooms(): Observable<Room[]> {
    return this.api.get<Room[]>('/v1/salles/disponibles');
  }
}
