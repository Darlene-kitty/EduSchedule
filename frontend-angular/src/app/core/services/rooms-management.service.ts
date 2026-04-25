import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface Room {
  id: number;
  name: string;
  building?: string;
  capacity: number;
  type: string;
  equipment?: string[];
  status?: 'available' | 'occupied' | 'maintenance';
  /** ID de l'école dans school-service (optionnel) */
  schoolId?: number;
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
    return this.api.get<any[]>('/v1/salles').pipe(
      map(list => list.map(s => this.mapSalleToRoom(s)))
    );
  }

  getRoomById(id: number): Observable<Room> {
    return this.api.get<any>(`/v1/salles/${id}`).pipe(
      map(s => this.mapSalleToRoom(s))
    );
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
    return this.api.get<any[]>('/v1/salles/disponibles').pipe(
      map(list => list.map(s => this.mapSalleToRoom(s)))
    );
  }

  /** Maps the backend Salle entity fields to the frontend Room interface */
  private mapSalleToRoom(s: any): Room {
    return {
      id:        s.id,
      name:      s.name ?? s.nom,
      building:  s.batiment ?? s.building,
      capacity:  s.capacite ?? s.capacity ?? 0,
      type:      s.type ?? '',
      equipment: s.equipment ?? [],
      status:    s.disponible === false ? 'maintenance'
               : s.disponible === true  ? 'available'
               : (s.status ?? 'available'),
      schoolId:  s.schoolId ?? undefined,
      createdAt: s.createdAt,
    };
  }
}
