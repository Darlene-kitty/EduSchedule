import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AppLayoutComponent } from '../../shared/components/app-layout/app-layout.component';

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  status: string;
  avatar?: string;
  createdAt: string;
}

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users implements OnInit {
  searchQuery = '';
  selectedRole = 'all';
  users: User[] = [];

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.users = [
      {
        id: 1,
        name: 'Admin User',
        email: 'admin@iusjc.cm',
        role: 'admin',
        status: 'active',
        createdAt: new Date().toISOString()
      },
      {
        id: 2,
        name: 'Prof. Martin Dubois',
        email: 'martin.dubois@iusjc.cm',
        role: 'teacher',
        status: 'active',
        createdAt: new Date().toISOString()
      },
      {
        id: 3,
        name: 'Jean Kamga',
        email: 'jean.kamga@student.iusjc.cm',
        role: 'student',
        status: 'active',
        createdAt: new Date().toISOString()
      },
      {
        id: 4,
        name: 'Prof. Marie Laurent',
        email: 'marie.laurent@iusjc.cm',
        role: 'teacher',
        status: 'active',
        createdAt: new Date().toISOString()
      }
    ];
  }

  get filteredUsers() {
    return this.users.filter(user => {
      const matchesSearch = 
        user.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        user.email.toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchesRole = this.selectedRole === 'all' || user.role === this.selectedRole;
      return matchesSearch && matchesRole;
    });
  }

  get stats() {
    return {
      total: this.users.length,
      admins: this.users.filter(u => u.role === 'admin').length,
      teachers: this.users.filter(u => u.role === 'teacher').length,
      students: this.users.filter(u => u.role === 'student').length
    };
  }

  getRoleColor(role: string): string {
    switch (role) {
      case 'admin':
        return 'bg-red-100 text-red-700';
      case 'teacher':
        return 'bg-blue-100 text-blue-700';
      case 'student':
        return 'bg-green-100 text-green-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  }

  getRoleLabel(role: string): string {
    switch (role) {
      case 'admin':
        return 'Administrateur';
      case 'teacher':
        return 'Enseignant';
      case 'student':
        return 'Étudiant';
      default:
        return role;
    }
  }

  viewUser(user: User) {
    alert(`Voir l'utilisateur: ${user.name}`);
  }

  editUser(user: User) {
    alert(`Éditer l'utilisateur: ${user.name}`);
  }

  deleteUser(user: User) {
    if (confirm(`Êtes-vous sûr de vouloir supprimer l'utilisateur "${user.name}" ?`)) {
      this.users = this.users.filter(u => u.id !== user.id);
    }
  }

  addUser() {
    alert('Ajouter un nouvel utilisateur');
  }
}
