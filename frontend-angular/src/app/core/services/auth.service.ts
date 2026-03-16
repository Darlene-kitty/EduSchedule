import { BehaviorSubject, Observable } from 'rxjs';
import { Injectable, inject } from '@angular/core';
import { StorageService } from './storage.service';
import { ApiService } from './api.service';
import { Router } from '@angular/router';

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: any;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private storageService = inject(StorageService);
  private apiService = inject(ApiService);
  private router = inject(Router);

  private userSubject = new BehaviorSubject<any>(null);
  user$ = this.userSubject.asObservable();

  constructor() {
    // Charger l'utilisateur depuis le localStorage au démarrage
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const user = this.storageService.getUser();
    if (user) {
      this.userSubject.next(user);
    }
  }

  setUser(user: any): void {
    this.userSubject.next(user);
    this.storageService.setUser(user);
  }

  getUser(): any {
    return this.userSubject.value;
  }

  isAuthenticated(): boolean {
    return this.storageService.isAuthenticated();
  }

  login(credentials: LoginCredentials): Observable<AuthResponse> {
    return new Observable(observer => {
      // Mode développement: simuler la connexion si l'API n'est pas disponible
      const mockResponse: AuthResponse = {
        token: 'mock-jwt-token-' + Date.now(),
        user: {
          id: 1,
          username: credentials.username,
          name: credentials.username,
          email: credentials.username + '@edu.com',
          role: 'admin'
        }
      };

      // Simuler un délai réseau
      setTimeout(() => {
        this.storageService.setToken(mockResponse.token);
        this.storageService.setUser(mockResponse.user);
        this.userSubject.next(mockResponse.user);
        observer.next(mockResponse);
        observer.complete();
      }, 500);

      // Version avec API (décommenter quand l'API est prête)
      /*
      this.apiService.post<AuthResponse>('/auth/login', credentials).subscribe({
        next: (response) => {
          this.storageService.setToken(response.token);
          this.storageService.setUser(response.user);
          this.userSubject.next(response.user);
          observer.next(response);
          observer.complete();
        },
        error: (error) => {
          observer.error(error);
        }
      });
      */
    });
  }

  logout(): void {
    this.storageService.removeToken();
    this.storageService.removeUser();
    this.userSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.storageService.getToken();
  }

  register(userData: any): Observable<AuthResponse> {
    return new Observable(observer => {
      // Mode développement: simuler l'inscription
      const mockResponse: AuthResponse = {
        token: 'mock-jwt-token-' + Date.now(),
        user: {
          id: Date.now(),
          username: userData.email,
          name: userData.name,
          email: userData.email,
          phone: userData.phone,
          role: userData.role
        }
      };

      // Simuler un délai réseau
      setTimeout(() => {
        this.storageService.setToken(mockResponse.token);
        this.storageService.setUser(mockResponse.user);
        this.userSubject.next(mockResponse.user);
        observer.next(mockResponse);
        observer.complete();
      }, 500);

      // Version avec API (décommenter quand l'API est prête)
      /*
      this.apiService.post<AuthResponse>('/auth/register', userData).subscribe({
        next: (response) => {
          this.storageService.setToken(response.token);
          this.storageService.setUser(response.user);
          this.userSubject.next(response.user);
          observer.next(response);
          observer.complete();
        },
        error: (error) => {
          observer.error(error);
        }
      });
      */
    });
  }
}