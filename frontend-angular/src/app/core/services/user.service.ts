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
    return this.apiService.put<User>(`/users/${id}`, userData).pipe(
      tap((user: User) => {
        this.storageService.setUser(user);
        this.userSubject.next(user);
      })
    );
  }

  deleteUser(id: number): Observable<void> {
    return this.apiService.delete<void>(`/users/${id}`);
  }

  updateProfile(data: any): Observable<User> {
    return this.apiService.put<User>('/users/profile', data).pipe(
      tap((user: User) => {
        this.storageService.setUser(user);
        this.userSubject.next(user);
      })
    );
  }
}
