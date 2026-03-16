import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { StorageService } from './storage.service';

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

@Injectable({
  providedIn: 'root'
})
export class EventsManagementService {
  private storageService = inject(StorageService);
  private eventsSubject = new BehaviorSubject<Event[]>([]);
  events$ = this.eventsSubject.asObservable();

  constructor() {
    this.loadEventsFromStorage();
  }

  private loadEventsFromStorage(): void {
    const events = this.storageService.getItem<Event[]>('eventsList') || this.getDefaultEvents();
    this.eventsSubject.next(events);
  }

  private getDefaultEvents(): Event[] {
    return [
      {
        id: 1,
        title: 'Conférence sur l\'IA',
        description: 'Conférence sur les dernières avancées en intelligence artificielle',
        date: '2024-03-20',
        startTime: '14:00',
        endTime: '17:00',
        location: 'Amphithéâtre A',
        organizer: 'Département Informatique',
        type: 'conference',
        participants: 150,
        status: 'scheduled',
        createdAt: '2024-02-15'
      },
      {
        id: 2,
        title: 'Atelier Recherche',
        description: 'Atelier de méthodologie de recherche',
        date: '2024-03-22',
        startTime: '09:00',
        endTime: '12:00',
        location: 'Salle B203',
        organizer: 'Prof. Martin',
        type: 'workshop',
        participants: 30,
        status: 'scheduled',
        createdAt: '2024-02-18'
      }
    ];
  }

  private saveEvents(events: Event[]): void {
    this.storageService.setItem('eventsList', events);
    this.eventsSubject.next(events);
  }

  getEvents(): Observable<Event[]> {
    return this.events$;
  }

  getEventById(id: number): Event | undefined {
    return this.eventsSubject.value.find(e => e.id === id);
  }

  addEvent(event: Omit<Event, 'id' | 'createdAt'>): Observable<Event> {
    return new Observable(observer => {
      setTimeout(() => {
        const events = this.eventsSubject.value;
        const newEvent: Event = {
          ...event,
          id: Date.now(),
          createdAt: new Date().toISOString().split('T')[0],
          status: event.status || 'scheduled'
        };
        
        const updatedEvents = [...events, newEvent];
        this.saveEvents(updatedEvents);
        
        observer.next(newEvent);
        observer.complete();
      }, 300);
    });
  }

  updateEvent(id: number, eventData: Partial<Event>): Observable<Event> {
    return new Observable(observer => {
      setTimeout(() => {
        const events = this.eventsSubject.value;
        const index = events.findIndex(e => e.id === id);
        
        if (index !== -1) {
          const updatedEvent = { ...events[index], ...eventData };
          const updatedEvents = [...events];
          updatedEvents[index] = updatedEvent;
          
          this.saveEvents(updatedEvents);
          observer.next(updatedEvent);
        } else {
          observer.error(new Error('Événement non trouvé'));
        }
        
        observer.complete();
      }, 300);
    });
  }

  deleteEvent(id: number): Observable<void> {
    return new Observable(observer => {
      setTimeout(() => {
        const events = this.eventsSubject.value;
        const updatedEvents = events.filter(e => e.id !== id);
        
        this.saveEvents(updatedEvents);
        observer.next();
        observer.complete();
      }, 300);
    });
  }

  getUpcomingEvents(): Event[] {
    const today = new Date().toISOString().split('T')[0];
    return this.eventsSubject.value
      .filter(e => e.date >= today && e.status === 'scheduled')
      .sort((a, b) => a.date.localeCompare(b.date));
  }
}
