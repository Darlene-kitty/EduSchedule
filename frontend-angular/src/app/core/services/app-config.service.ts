import { Injectable, inject } from '@angular/core';
import { Observable, of, shareReplay } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface SessionType  { value: string; label: string; }
export interface EventType    { value: string; label: string; }
export interface WorkDay      { key: string;   label: string; }

export interface AppConfig {
  academicLevels: string[];
  semesters:      string[];
  sessionTypes:   SessionType[];
  eventTypes:     EventType[];
  departments:    string[];
  courseDurations: number[];
  creditValues:   number[];
  examSlots:      string[];
  workDays:       WorkDay[];
  workHours:      string[];
  levelTypes:     string[];
  courseTypes:    string[];
}

/** Valeurs par défaut utilisées si le backend est indisponible */
const DEFAULTS: AppConfig = {
  academicLevels:  ['L1', 'L2', 'L3', 'M1', 'M2'],
  semesters:       ['S1', 'S2', 'S3', 'S4', 'S5', 'S6', 'S7', 'S8', 'S9', 'S10'],
  sessionTypes:    [
    { value: 'CM',        label: 'Cours magistral' },
    { value: 'TD',        label: 'TD'              },
    { value: 'TP',        label: 'TP'              },
    { value: 'Séminaire', label: 'Séminaire'       },
  ],
  eventTypes: [
    { value: 'CONFERENCE',  label: 'Conférence'  },
    { value: 'SEMINAR',     label: 'Séminaire'   },
    { value: 'WORKSHOP',    label: 'Atelier'     },
    { value: 'MEETING',     label: 'Réunion'     },
    { value: 'EXAM',        label: 'Examen'      },
    { value: 'DEFENSE',     label: 'Soutenance'  },
    { value: 'CEREMONY',    label: 'Cérémonie'   },
    { value: 'TRAINING',    label: 'Formation'   },
    { value: 'COMPETITION', label: 'Compétition' },
    { value: 'OTHER',       label: 'Autre'       },
  ],
  departments: [
    'Informatique', 'Mathématiques', 'Physique', 'Chimie',
    'Biologie', 'Économie', 'Droit', 'Lettres',
    'Sciences Humaines', 'Génie Civil', 'Génie Électrique',
  ],
  courseDurations: [30, 45, 60, 90, 120, 150, 180, 240],
  creditValues:    [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
  examSlots:       ['08:00-10:00', '10:30-12:30', '14:00-16:00', '16:30-18:30'],
  workDays: [
    { key: 'MONDAY',    label: 'Lundi'    },
    { key: 'TUESDAY',   label: 'Mardi'    },
    { key: 'WEDNESDAY', label: 'Mercredi' },
    { key: 'THURSDAY',  label: 'Jeudi'    },
    { key: 'FRIDAY',    label: 'Vendredi' },
  ],
  workHours: [
    '07:00','08:00','09:00','10:00','11:00',
    '12:00','13:00','14:00','15:00','16:00',
    '17:00','18:00','19:00',
  ],
  levelTypes:  ['Licence', 'Master', 'Doctorat', 'Préparatoire', 'CPGE'],
  courseTypes: ['CM', 'TD', 'TP', 'EXAM', 'CONFERENCE', 'SEMINAR'],
};

@Injectable({ providedIn: 'root' })
export class AppConfigService {
  private api = inject(ApiService);

  /** Config mise en cache — un seul appel HTTP pour toute la session */
  private config$: Observable<AppConfig> | null = null;

  /** Retourne la config (depuis le cache ou le backend) */
  getConfig(): Observable<AppConfig> {
    if (!this.config$) {
      this.config$ = this.api.get<{ success: boolean; data: AppConfig }>('/v1/config/app').pipe(
        map(res => res?.data ?? DEFAULTS),
        catchError(() => {
          console.warn('[AppConfigService] Backend unavailable — using defaults');
          return of(DEFAULTS);
        }),
        shareReplay(1)
      );
    }
    return this.config$;
  }

  /** Invalide le cache (utile après une mise à jour de config) */
  invalidateCache(): void {
    this.config$ = null;
  }
}
