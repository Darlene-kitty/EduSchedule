import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { StorageService } from './storage.service';

export interface UserManagement {
  id: number;
  name: string;
  email: string;
  role: string;
  phone?: string;
  department?: string;
  status?: 'active' | 'inactive';
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UsersManagementService {
  private storageService = inject(StorageService);
  private usersSubject = new BehaviorSubject<UserManagement[]>([]);
  users$ = this.usersSubject.asObservable();

  // Event emitter pour notifier les changements
  private userChangedSubject = new BehaviorSubject<void>(undefined);
  userChanged$ = this.userChangedSubject.asObservable();

  constructor() {
    this.loadUsersFromStorage();
  }

  private loadUsersFromStorage(): void {
    const users = this.storageService.getItem<UserManagement[]>('usersList') || this.getDefaultUsers();
    this.usersSubject.next(users);
  }

  private getDefaultUsers(): UserManagement[] {
    return [
      {
        id: 1,
        name: 'Admin Système',
        email: 'admin@edu.com',
        role: 'admin',
        phone: '01 23 45 67 89',
        department: 'Administration',
        status: 'active',
        createdAt: '2024-01-15'
      },
      {
        id: 2,
        name: 'Prof. Martin',
        email: 'martin@edu.com',
        role: 'teacher',
        phone: '01 23 45 67 90',
        department: 'Mathématiques',
        status: 'active',
        createdAt: '2024-02-01'
      },
      {
        id: 3,
        name: 'Prof. Dubois',
        email: 'dubois@edu.com',
        role: 'teacher',
        phone: '01 23 45 67 91',
        department: 'Physique',
        status: 'active',
        createdAt: '2024-02-10'
      }
    ];
  }

  private saveUsers(users: UserManagement[]): void {
    this.storageService.setItem('usersList', users);
    this.usersSubject.next(users);
    this.userChangedSubject.next(); // Notifier le changement
  }

  getUsers(): Observable<UserManagement[]> {
    return this.users$;
  }

  getUserById(id: number): UserManagement | undefined {
    return this.usersSubject.value.find(u => u.id === id);
  }

  addUser(user: Omit<UserManagement, 'id' | 'createdAt'>): Observable<UserManagement> {
    return new Observable(observer => {
      setTimeout(() => {
        const users = this.usersSubject.value;
        const newUser: UserManagement = {
          ...user,
          id: Date.now(),
          createdAt: new Date().toISOString().split('T')[0],
          status: user.status || 'active'
        };
        
        const updatedUsers = [...users, newUser];
        this.saveUsers(updatedUsers);
        
        observer.next(newUser);
        observer.complete();
      }, 300);
    });
  }

  updateUser(id: number, userData: Partial<UserManagement>): Observable<UserManagement> {
    return new Observable(observer => {
      setTimeout(() => {
        const users = this.usersSubject.value;
        const index = users.findIndex(u => u.id === id);
        
        if (index !== -1) {
          const updatedUser = { ...users[index], ...userData };
          const updatedUsers = [...users];
          updatedUsers[index] = updatedUser;
          
          this.saveUsers(updatedUsers);
          observer.next(updatedUser);
        } else {
          observer.error(new Error('Utilisateur non trouvé'));
        }
        
        observer.complete();
      }, 300);
    });
  }

  deleteUser(id: number): Observable<void> {
    return new Observable(observer => {
      setTimeout(() => {
        const users = this.usersSubject.value;
        const updatedUsers = users.filter(u => u.id !== id);
        
        this.saveUsers(updatedUsers);
        observer.next();
        observer.complete();
      }, 300);
    });
  }

  searchUsers(query: string): UserManagement[] {
    const users = this.usersSubject.value;
    const lowerQuery = query.toLowerCase();
    
    return users.filter(user => 
      user.name.toLowerCase().includes(lowerQuery) ||
      user.email.toLowerCase().includes(lowerQuery) ||
      user.role.toLowerCase().includes(lowerQuery) ||
      user.department?.toLowerCase().includes(lowerQuery)
    );
  }

  getUsersByRole(role: string): UserManagement[] {
    return this.usersSubject.value.filter(u => u.role === role);
  }

  toggleUserStatus(id: number): Observable<UserManagement> {
    return new Observable(observer => {
      setTimeout(() => {
        const users = this.usersSubject.value;
        const index = users.findIndex(u => u.id === id);
        
        if (index !== -1) {
          const user = users[index];
          const updatedUser = {
            ...user,
            status: user.status === 'active' ? 'inactive' : 'active'
          } as UserManagement;
          
          const updatedUsers = [...users];
          updatedUsers[index] = updatedUser;
          
          this.saveUsers(updatedUsers);
          observer.next(updatedUser);
        } else {
          observer.error(new Error('Utilisateur non trouvé'));
        }
        
        observer.complete();
      }, 300);
    });
  }
}
