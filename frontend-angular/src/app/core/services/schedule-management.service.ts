import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { StorageService } from './storage.service';

export interface ScheduleEntry {
  id: number;
  courseId: number;
  courseName: string;
  teacher: string;
  room: string;
  dayOfWeek: number; // 0-6 (Dimanche-Samedi)
  startTime: string;
  endTime: string;
  level: string;
  group: string;
  color?: string;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ScheduleManagementService {
  private storageService = inject(StorageService);
  private scheduleSubject = new BehaviorSubject<ScheduleEntry[]>([]);
  schedule$ = this.scheduleSubject.asObservable();

  constructor() {
    this.loadScheduleFromStorage();
  }

  private loadScheduleFromStorage(): void {
    const schedule = this.storageService.getItem<ScheduleEntry[]>('scheduleList') || this.getDefaultSchedule();
    this.scheduleSubject.next(schedule);
  }

  private getDefaultSchedule(): ScheduleEntry[] {
    return [
      {
        id: 1,
        courseId: 1,
        courseName: 'Mathématiques',
        teacher: 'Dr. Martin',
        room: 'A101',
        dayOfWeek: 1, // Lundi
        startTime: '08:00',
        endTime: '10:00',
        level: 'L3',
        group: 'G1',
        color: '#3B82F6',
        createdAt: '2024-01-15'
      },
      {
        id: 2,
        courseId: 2,
        courseName: 'Physique',
        teacher: 'Prof. Dubois',
        room: 'B203',
        dayOfWeek: 2, // Mardi
        startTime: '10:00',
        endTime: '12:00',
        level: 'M1',
        group: 'G1',
        color: '#10B981',
        createdAt: '2024-01-15'
      },
      {
        id: 3,
        courseId: 3,
        courseName: 'Chimie',
        teacher: 'Prof. Laurent',
        room: 'Lab D104',
        dayOfWeek: 3, // Mercredi
        startTime: '14:00',
        endTime: '16:00',
        level: 'L2',
        group: 'G2',
        color: '#8B5CF6',
        createdAt: '2024-01-15'
      }
    ];
  }

  private saveSchedule(schedule: ScheduleEntry[]): void {
    this.storageService.setItem('scheduleList', schedule);
    this.scheduleSubject.next(schedule);
  }

  getSchedule(): Observable<ScheduleEntry[]> {
    return this.schedule$;
  }

  getScheduleByDay(dayOfWeek: number): ScheduleEntry[] {
    return this.scheduleSubject.value
      .filter(entry => entry.dayOfWeek === dayOfWeek)
      .sort((a, b) => a.startTime.localeCompare(b.startTime));
  }

  getScheduleByRoom(room: string): ScheduleEntry[] {
    return this.scheduleSubject.value
      .filter(entry => entry.room === room)
      .sort((a, b) => {
        if (a.dayOfWeek !== b.dayOfWeek) {
          return a.dayOfWeek - b.dayOfWeek;
        }
        return a.startTime.localeCompare(b.startTime);
      });
  }

  addScheduleEntry(entry: Omit<ScheduleEntry, 'id' | 'createdAt'>): Observable<ScheduleEntry> {
    return new Observable(observer => {
      setTimeout(() => {
        // Vérifier les conflits
        const conflict = this.checkConflict(entry);
        if (conflict) {
          observer.error(new Error('Conflit détecté avec un cours existant'));
          return;
        }

        const schedule = this.scheduleSubject.value;
        const newEntry: ScheduleEntry = {
          ...entry,
          id: Date.now(),
          createdAt: new Date().toISOString().split('T')[0]
        };
        
        const updatedSchedule = [...schedule, newEntry];
        this.saveSchedule(updatedSchedule);
        
        observer.next(newEntry);
        observer.complete();
      }, 300);
    });
  }

  updateScheduleEntry(id: number, entryData: Partial<ScheduleEntry>): Observable<ScheduleEntry> {
    return new Observable(observer => {
      setTimeout(() => {
        const schedule = this.scheduleSubject.value;
        const index = schedule.findIndex(e => e.id === id);
        
        if (index !== -1) {
          const updatedEntry = { ...schedule[index], ...entryData };
          
          // Vérifier les conflits (exclure l'entrée actuelle)
          const conflict = this.checkConflict(updatedEntry, id);
          if (conflict) {
            observer.error(new Error('Conflit détecté avec un cours existant'));
            return;
          }
          
          const updatedSchedule = [...schedule];
          updatedSchedule[index] = updatedEntry;
          
          this.saveSchedule(updatedSchedule);
          observer.next(updatedEntry);
        } else {
          observer.error(new Error('Entrée non trouvée'));
        }
        
        observer.complete();
      }, 300);
    });
  }

  deleteScheduleEntry(id: number): Observable<void> {
    return new Observable(observer => {
      setTimeout(() => {
        const schedule = this.scheduleSubject.value;
        const updatedSchedule = schedule.filter(e => e.id !== id);
        
        this.saveSchedule(updatedSchedule);
        observer.next();
        observer.complete();
      }, 300);
    });
  }

  private checkConflict(entry: Partial<ScheduleEntry>, excludeId?: number): boolean {
    const schedule = this.scheduleSubject.value;
    
    return schedule.some(existing => {
      if (excludeId && existing.id === excludeId) return false;
      
      // Même jour et même salle
      if (existing.dayOfWeek === entry.dayOfWeek && existing.room === entry.room) {
        // Vérifier le chevauchement des horaires
        const existingStart = this.timeToMinutes(existing.startTime);
        const existingEnd = this.timeToMinutes(existing.endTime);
        const newStart = this.timeToMinutes(entry.startTime || '');
        const newEnd = this.timeToMinutes(entry.endTime || '');
        
        return (newStart < existingEnd && newEnd > existingStart);
      }
      
      return false;
    });
  }

  private timeToMinutes(time: string): number {
    const [hours, minutes] = time.split(':').map(Number);
    return hours * 60 + minutes;
  }

  getConflicts(): ScheduleEntry[][] {
    const schedule = this.scheduleSubject.value;
    const conflicts: ScheduleEntry[][] = [];
    
    for (let i = 0; i < schedule.length; i++) {
      for (let j = i + 1; j < schedule.length; j++) {
        if (this.hasConflict(schedule[i], schedule[j])) {
          conflicts.push([schedule[i], schedule[j]]);
        }
      }
    }
    
    return conflicts;
  }

  private hasConflict(entry1: ScheduleEntry, entry2: ScheduleEntry): boolean {
    if (entry1.dayOfWeek !== entry2.dayOfWeek || entry1.room !== entry2.room) {
      return false;
    }
    
    const start1 = this.timeToMinutes(entry1.startTime);
    const end1 = this.timeToMinutes(entry1.endTime);
    const start2 = this.timeToMinutes(entry2.startTime);
    const end2 = this.timeToMinutes(entry2.endTime);
    
    return (start1 < end2 && end1 > start2);
  }
}
