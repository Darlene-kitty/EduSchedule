import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

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
  private http = inject(HttpClient);
  private base = '/api/room-optimization';

  suggest(criteria: SuggestionCriteria): Observable<RoomSuggestion[]> {
    return this.http.post<RoomSuggestion[]>(`${this.base}/suggest`, criteria).pipe(
      catchError(() => of([]))
    );
  }

  suggestForCourse(courseType: string, startTime: string, endTime: string,
                   capacity: number): Observable<RoomSuggestion[]> {
    return this.http.get<RoomSuggestion[]>(`${this.base}/suggest-for-course`, {
      params: { courseType, startTime, endTime, capacity: capacity.toString(), maxSuggestions: '5' }
    }).pipe(catchError(() => of([])));
  }
}
