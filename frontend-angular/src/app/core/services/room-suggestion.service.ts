import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface RoomSuggestion {
  resourceId: number;
  roomName: string;
  capacity: number;
  location: string;
  equipment: string;
  optimizationScore: number;
  availabilityStatus: string;
  recommendationReason: string;
}

export interface SuggestionCriteria {
  startTime: string;       // ISO datetime
  endTime: string;
  minCapacity?: number;
  courseType?: string;     // COURS | TD | TP | EXAMEN
  requiredEquipment?: string[];
  preferredBuilding?: string;
  maxSuggestions?: number;
}

@Injectable({ providedIn: 'root' })
export class RoomSuggestionService {
  private api = inject(ApiService);

  suggest(criteria: SuggestionCriteria): Observable<RoomSuggestion[]> {
    return this.api.post<RoomSuggestion[]>('/room-optimization/suggest', criteria).pipe(
      catchError(() => of([]))
    );
  }

  suggestForCourse(courseType: string, startTime: string, endTime: string,
                   capacity: number): Observable<RoomSuggestion[]> {
    const params = `?courseType=${encodeURIComponent(courseType)}&startTime=${encodeURIComponent(startTime)}&endTime=${encodeURIComponent(endTime)}&capacity=${capacity}&maxSuggestions=5`;
    return this.api.get<RoomSuggestion[]>(`/room-optimization/suggest-for-course${params}`).pipe(
      catchError(() => of([]))
    );
  }
}
