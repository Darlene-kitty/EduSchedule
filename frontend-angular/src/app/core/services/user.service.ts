import { Injectable, inject } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { StorageService } from './storage.service';

export interface User {
  id: number;
  name: string;
  email: string;
  role?: string;
  // Ajoute d'autres propriétés selon ton modèle
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiService = inject(ApiService);
  private storageService = inject(StorageService);

  private userSubject = new BehaviorSubject<User | null>(null);
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

  getUsers(): Observable<User[]> {
    return this.apiService.get<User[]>('/users');
  }

  getUser(id: number): Observable<User> {
    return this.apiService.get<User>(`/users/${id}`).pipe(
      tap((user: User) => this.userSubject.next(user))
    );
  }

  setUser(user: User | null): void {
    this.userSubject.next(user);
    if (user) {
      this.storageService.setUser(user);
    } else {
      this.storageService.removeUser();
    }
  }

  createUser(user: Partial<User>): Observable<User> {
    return this.apiService.post<User>('/users', user);
  }

  updateUser(id: number, userData: Partial<User>): Observable<User> {
    return new Observable(observer => {
      // Mode développement: simuler la mise à jour
      const currentUser = this.storageService.getUser();
      const updatedUser = { ...currentUser, ...userData };
      
      setTimeout(() => {
        this.storageService.setUser(updatedUser);
        this.userSubject.next(updatedUser);
        observer.next(updatedUser);
        observer.complete();
      }, 300);

      // Version avec API (décommenter quand l'API est prête)
      /*
      this.apiService.put<User>(`/users/${id}`, userData).subscribe({
        next: (user) => {
          this.storageService.setUser(user);
          this.userSubject.next(user);
          observer.next(user);
          observer.complete();
        },
        error: (error) => observer.error(error)
      });
      */
    });
  }

  deleteUser(id: number): Observable<void> {
    return new Observable(observer => {
      // Mode développement: simuler la suppression
      setTimeout(() => {
        observer.next();
        observer.complete();
      }, 300);

      // Version avec API (décommenter quand l'API est prête)
      /*
      this.apiService.delete<void>(`/users/${id}`).subscribe({
        next: () => {
          observer.next();
          observer.complete();
        },
        error: (error) => observer.error(error)
      });
      */
    });
  }

  updateProfile(data: any): Observable<User> {
    return new Observable(observer => {
      // Mode développement: simuler la mise à jour du profil
      const currentUser = this.storageService.getUser();
      const updatedUser = { ...currentUser, ...data };
      
      setTimeout(() => {
        this.storageService.setUser(updatedUser);
        this.userSubject.next(updatedUser);
        observer.next(updatedUser);
        observer.complete();
      }, 300);

      // Version avec API (décommenter quand l'API est prête)
      /*
      this.apiService.put<User>('/users/profile', data).subscribe({
        next: (user) => {
          this.storageService.setUser(user);
          this.userSubject.next(user);
          observer.next(user);
          observer.complete();
        },
        error: (error) => observer.error(error)
      });
      */
    });
  }
}
