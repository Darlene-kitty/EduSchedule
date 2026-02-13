import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
export interface User {
  id: number;
  name: string;
  email: string;
  // Ajoute d'autres propriétés selon ton modèle
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private apiService: ApiService) {}

  getUsers(): Observable<User[]> {
    return this.apiService.get<User[]>('/users');
  }

  getUser(id: number): Observable<User> {
    return this.apiService.get<User>(`/users/${id}`);
  }

  createUser(user: Partial<User>): Observable<User> {
    return this.apiService.post<User>('/users', user);
  }

  updateUser(id: number, user: Partial<User>): Observable<User> {
    return this.apiService.put<User>(`/users/${id}`, user);
  }

  deleteUser(id: number): Observable<void> {
    return this.apiService.delete<void>(`/users/${id}`);
  }

  updateProfile(data: any): Observable<User> {
    return this.apiService.put<User>('/users/profile', data);
  }
}
