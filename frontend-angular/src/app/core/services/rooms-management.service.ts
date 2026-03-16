import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { StorageService } from './storage.service';

export interface Room {
  id: number;
  name: string;
  building: string;
  capacity: number;
  type: string;
  equipment: string[];
  status?: 'available' | 'occupied' | 'maintenance';
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class RoomsManagementService {
  private storageService = inject(StorageService);
  private roomsSubject = new BehaviorSubject<Room[]>([]);
  rooms$ = this.roomsSubject.asObservable();

  constructor() {
    this.loadRoomsFromStorage();
  }

  private loadRoomsFromStorage(): void {
    const rooms = this.storageService.getItem<Room[]>('roomsList') || this.getDefaultRooms();
    this.roomsSubject.next(rooms);
  }

  private getDefaultRooms(): Room[] {
    return [
      {
        id: 1,
        name: 'A101',
        building: 'Bâtiment A',
        capacity: 50,
        type: 'Amphithéâtre',
        equipment: ['Projecteur', 'Tableau blanc', 'Sonorisation'],
        status: 'available',
        createdAt: '2024-01-10'
      },
      {
        id: 2,
        name: 'B203',
        building: 'Bâtiment B',
        capacity: 30,
        type: 'Salle de cours',
        equipment: ['Projecteur', 'Tableau blanc'],
        status: 'available',
        createdAt: '2024-01-10'
      },
      {
        id: 3,
        name: 'Lab D104',
        building: 'Bâtiment D',
        capacity: 25,
        type: 'Laboratoire',
        equipment: ['Équipement scientifique', 'Hottes', 'Paillasses'],
        status: 'available',
        createdAt: '2024-01-10'
      }
    ];
  }

  private saveRooms(rooms: Room[]): void {
    this.storageService.setItem('roomsList', rooms);
    this.roomsSubject.next(rooms);
  }

  getRooms(): Observable<Room[]> {
    return this.rooms$;
  }

  getRoomById(id: number): Room | undefined {
    return this.roomsSubject.value.find(r => r.id === id);
  }

  addRoom(room: Omit<Room, 'id' | 'createdAt'>): Observable<Room> {
    return new Observable(observer => {
      setTimeout(() => {
        const rooms = this.roomsSubject.value;
        const newRoom: Room = {
          ...room,
          id: Date.now(),
          createdAt: new Date().toISOString().split('T')[0],
          status: room.status || 'available'
        };
        
        const updatedRooms = [...rooms, newRoom];
        this.saveRooms(updatedRooms);
        
        observer.next(newRoom);
        observer.complete();
      }, 300);
    });
  }

  updateRoom(id: number, roomData: Partial<Room>): Observable<Room> {
    return new Observable(observer => {
      setTimeout(() => {
        const rooms = this.roomsSubject.value;
        const index = rooms.findIndex(r => r.id === id);
        
        if (index !== -1) {
          const updatedRoom = { ...rooms[index], ...roomData };
          const updatedRooms = [...rooms];
          updatedRooms[index] = updatedRoom;
          
          this.saveRooms(updatedRooms);
          observer.next(updatedRoom);
        } else {
          observer.error(new Error('Salle non trouvée'));
        }
        
        observer.complete();
      }, 300);
    });
  }

  deleteRoom(id: number): Observable<void> {
    return new Observable(observer => {
      setTimeout(() => {
        const rooms = this.roomsSubject.value;
        const updatedRooms = rooms.filter(r => r.id !== id);
        
        this.saveRooms(updatedRooms);
        observer.next();
        observer.complete();
      }, 300);
    });
  }

  searchRooms(query: string): Room[] {
    const rooms = this.roomsSubject.value;
    const lowerQuery = query.toLowerCase();
    
    return rooms.filter(room => 
      room.name.toLowerCase().includes(lowerQuery) ||
      room.building.toLowerCase().includes(lowerQuery) ||
      room.type.toLowerCase().includes(lowerQuery)
    );
  }

  getAvailableRooms(): Room[] {
    return this.roomsSubject.value.filter(r => r.status === 'available');
  }
}
