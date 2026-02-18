import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { environment } from '../../../environments/environment';

export interface User {
  id: number;
  username: string;
  email: string;
  role: string;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  role: string;
}

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  templateUrl: './users.html',
  styleUrl: './users.css'
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  filteredUsers: User[] = [];
  loading = true;
  error: string | null = null;
  message = '';
  isError = false;

  searchQuery = '';
  selectedRole = 'all';
  isAddModalOpen = false;

  newUser: CreateUserRequest = { username: '', email: '', password: '', role: 'STUDENT' };
  roles = ['all', 'ADMIN', 'TEACHER', 'STUDENT'];

  constructor(private http: HttpClient) {}

  ngOnInit(): void { this.loadUsers(); }

  loadUsers(): void {
    this.loading = true;
    const mockUsers: User[] = [
      { id: 1, username: 'admin', email: 'admin@edu.com', role: 'ADMIN', enabled: true, createdAt: '2025-01-01', updatedAt: '2025-01-01' },
      { id: 2, username: 'martin', email: 'martin@edu.com', role: 'TEACHER', enabled: true, createdAt: '2025-01-02', updatedAt: '2025-01-02' },
      { id: 3, username: 'sophie', email: 'sophie@edu.com', role: 'STUDENT', enabled: true, createdAt: '2025-01-03', updatedAt: '2025-01-03' },
      { id: 4, username: 'laurent', email: 'laurent@edu.com', role: 'TEACHER', enabled: false, createdAt: '2025-01-04', updatedAt: '2025-01-04' },
      { id: 5, username: 'pierre', email: 'pierre@edu.com', role: 'STUDENT', enabled: true, createdAt: '2025-01-05', updatedAt: '2025-01-05' },
    ];
    this.users = mockUsers;
    this.applyFilters();
    this.loading = false;
  }

  applyFilters(): void {
    this.filteredUsers = this.users.filter(user => {
      const matchesSearch =
        user.username.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        user.email.toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchesRole = this.selectedRole === 'all' || user.role === this.selectedRole;
      return matchesSearch && matchesRole;
    });
  }

  onSearchChange(): void { this.applyFilters(); }
  onRoleChange(role: string): void { this.selectedRole = role; this.applyFilters(); }

  openAddModal(): void {
    this.newUser = { username: '', email: '', password: '', role: 'STUDENT' };
    this.isAddModalOpen = true;
  }

  closeModal(): void { this.isAddModalOpen = false; }

  handleAddUser(): void {
    this.http.post<User>(`${environment.apiUrl}/users`, this.newUser).subscribe({
      next: (user) => {
        this.users = [...this.users, user];
        this.applyFilters();
        this.isError = false;
        this.message = 'Utilisateur créé avec succès';
        this.closeModal();
      },
      error: (err) => {
        this.isError = true;
        this.message = err.message || 'Erreur lors de la création';
      }
    });
  }

  handleDeleteUser(id: number): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) return;
    this.http.delete(`${environment.apiUrl}/users/${id}`).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.id !== id);
        this.applyFilters();
        this.isError = false;
        this.message = 'Utilisateur supprimé avec succès';
      },
      error: (err) => {
        this.isError = true;
        this.message = err.message || 'Erreur lors de la suppression';
      }
    });
  }

  getInitials(username: string): string { return username.substring(0, 2).toUpperCase(); }

  getRoleClass(role: string): string {
    switch (role) {
      case 'ADMIN':   return 'badge-purple';
      case 'TEACHER': return 'badge-blue';
      case 'STUDENT': return 'badge-green';
      default:        return 'badge-gray';
    }
  }

  getRoleLabel(role: string): string {
    switch (role) {
      case 'ADMIN':   return 'Administrateur';
      case 'TEACHER': return 'Professeur';
      case 'STUDENT': return 'Étudiant';
      default:        return role;
    }
  }

  getStatusClass(enabled: boolean): string { return enabled ? 'badge-green' : 'badge-gray'; }
  getStatusLabel(enabled: boolean): string { return enabled ? 'Actif' : 'Inactif'; }
  formatDate(date: string): string { return new Date(date).toLocaleDateString('fr-FR'); }
}