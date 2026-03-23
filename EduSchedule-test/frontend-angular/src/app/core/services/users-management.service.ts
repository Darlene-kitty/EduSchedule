import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { ApiService } from './api.service';

export interface UserManagement {
  id: number;
  name?: string;
  username?: string;
  firstName?: string;
  lastName?: string;
  email: string;
  role: string;
  password?: string;
  phone?: string;
  department?: string;
  status?: 'active' | 'inactive';
  enabled?: boolean;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UsersManagementService {
  private api = inject(ApiService);

  private userChangedSubject = new BehaviorSubject<void>(undefined);
  userChanged$ = this.userChangedSubject.asObservable();

  getUsers(): Observable<UserManagement[]> {
    return this.api.get<UserManagement[]>('/users');
  }

  getUserById(id: number): Observable<UserManagement> {
    return this.api.get<UserManagement>(`/users/${id}`);
  }

  addUser(user: Omit<UserManagement, 'id' | 'createdAt'>): Observable<UserManagement> {
    return this.api.post<UserManagement>('/users', user).pipe(
      tap(() => this.userChangedSubject.next())
    );
  }

  updateUser(id: number, userData: Partial<UserManagement>): Observable<UserManagement> {
    return this.api.put<UserManagement>(`/users/${id}`, userData).pipe(
      tap(() => this.userChangedSubject.next())
    );
  }

  deleteUser(id: number): Observable<void> {
    return this.api.delete<void>(`/users/${id}`).pipe(
      tap(() => this.userChangedSubject.next())
    );
  }
}
