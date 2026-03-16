import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class StorageService {
  private isBrowser(): boolean {
    return typeof window !== 'undefined' && typeof localStorage !== 'undefined';
  }

  // Méthodes génériques
  setItem(key: string, value: any): void {
    if (!this.isBrowser()) return;
    try {
      const serializedValue = JSON.stringify(value);
      localStorage.setItem(key, serializedValue);
    } catch (error) {
      console.error('Error saving to localStorage', error);
    }
  }

  getItem<T>(key: string): T | null {
    if (!this.isBrowser()) return null;
    try {
      const item = localStorage.getItem(key);
      return item ? JSON.parse(item) : null;
    } catch (error) {
      console.error('Error reading from localStorage', error);
      return null;
    }
  }

  removeItem(key: string): void {
    if (!this.isBrowser()) return;
    localStorage.removeItem(key);
  }

  clear(): void {
    if (!this.isBrowser()) return;
    localStorage.clear();
  }

  // Méthodes spécifiques pour l'authentification
  setToken(token: string): void {
    this.setItem('token', token);
  }

  getToken(): string | null {
    return this.getItem<string>('token');
  }

  removeToken(): void {
    this.removeItem('token');
  }

  // Méthodes pour l'utilisateur
  setUser(user: any): void {
    this.setItem('user', user);
  }

  getUser(): any {
    return this.getItem('user');
  }

  removeUser(): void {
    this.removeItem('user');
  }

  // Méthodes pour les préférences
  setPreferences(preferences: any): void {
    this.setItem('preferences', preferences);
  }

  getPreferences(): any {
    return this.getItem('preferences');
  }

  // Méthode pour vérifier l'authentification
  isAuthenticated(): boolean {
    return !!this.getToken();
  }
}
