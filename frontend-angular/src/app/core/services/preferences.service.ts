import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { StorageService } from './storage.service';

export interface UserPreferences {
  theme?: 'light' | 'dark';
  language?: string;
  notifications?: boolean;
  emailNotifications?: boolean;
  calendarView?: 'day' | 'week' | 'month';
}

@Injectable({
  providedIn: 'root'
})
export class PreferencesService {
  private storageService = inject(StorageService);

  private preferencesSubject = new BehaviorSubject<UserPreferences>(this.getDefaultPreferences());
  preferences$ = this.preferencesSubject.asObservable();

  constructor() {
    this.loadPreferences();
  }

  private getDefaultPreferences(): UserPreferences {
    return {
      theme: 'light',
      language: 'fr',
      notifications: true,
      emailNotifications: true,
      calendarView: 'week'
    };
  }

  private loadPreferences(): void {
    const savedPreferences = this.storageService.getPreferences();
    if (savedPreferences) {
      this.preferencesSubject.next({ ...this.getDefaultPreferences(), ...savedPreferences });
    }
  }

  getPreferences(): UserPreferences {
    return this.preferencesSubject.value;
  }

  updatePreferences(preferences: Partial<UserPreferences>): void {
    const currentPreferences = this.preferencesSubject.value;
    const updatedPreferences = { ...currentPreferences, ...preferences };
    this.preferencesSubject.next(updatedPreferences);
    this.storageService.setPreferences(updatedPreferences);
  }

  resetPreferences(): void {
    const defaultPreferences = this.getDefaultPreferences();
    this.preferencesSubject.next(defaultPreferences);
    this.storageService.setPreferences(defaultPreferences);
  }

  setTheme(theme: 'light' | 'dark'): void {
    this.updatePreferences({ theme });
  }

  getTheme(): 'light' | 'dark' {
    return this.preferencesSubject.value.theme || 'light';
  }
}
