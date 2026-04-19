import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { StorageService } from './storage.service';

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  refreshToken?: string;
  type?: string;
  userId?: number;
  username?: string;
  email?: string;
  role?: string;
  user?: any;
}

export type UserRole = 'ADMIN' | 'TEACHER' | 'STUDENT' | string;

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private storageService = inject(StorageService);
  private baseUrl = environment.apiUrl;

  private userSubject = new BehaviorSubject<any>(null);
  user$ = this.userSubject.asObservable();

  constructor() {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const user = this.storageService.getUser();
    if (user) {
      this.userSubject.next(user);
    }
  }

  isAuthenticated(): boolean {
    return !!this.storageService.getToken();
  }

  getToken(): string | null {
    return this.storageService.getToken();
  }

  getUser(): any {
    return this.userSubject.value;
  }

  /** Retourne le rôle normalisé en majuscules */
  getRole(): UserRole {
    const user = this.getUser();
    return (user?.role || user?.roles?.[0] || '').toString().toUpperCase().replace('ROLE_', '');
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  isTeacher(): boolean {
    return this.getRole() === 'TEACHER';
  }

  hasRole(...roles: UserRole[]): boolean {
    return roles.map(r => r.toUpperCase()).includes(this.getRole());
  }

  setUser(user: any): void {
    this.userSubject.next(user);
    this.storageService.setUser(user);
  }

  login(credentials: LoginCredentials): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, credentials).pipe(
      tap(response => {
        this.storageService.setToken(response.token);
        if (response.refreshToken) {
          this.storageService.setItem('refreshToken', response.refreshToken);
        }
        const user = response.user ?? {
          id: response.userId,
          username: response.username,
          email: response.email,
          role: response.role
        };
        this.storageService.setUser(user);
        this.userSubject.next(user);
      })
    );
  }

  register(userData: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/register`, userData).pipe(
      tap(response => {
        this.storageService.setToken(response.token);
        this.storageService.setUser(response.user);
        this.userSubject.next(response.user);
      })
    );
  }

  logout(): void {
    this.storageService.removeToken();
    this.storageService.removeUser();
    this.storageService.removeItem('refreshToken');
    this.userSubject.next(null);
    this.router.navigate(['/login']);
  }
}
