import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class ProfileComponent implements OnInit {
  activeTab = 'personal';
  message = '';
  isError = false;

  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  profileData = {
    name: '',
    email: '',
    phone: '',
    address: '',
    bio: '',
    department: '',
    specialization: ''
  };

  passwordData = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  preferences = {
    emailNotifications: true,
    pushNotifications: false,
    language: 'fr'
  };

  stats = [
    { label: 'Cours', value: '12', icon: 'menu_book', color: 'bg-blue' },
    { label: 'Heures', value: '48', icon: 'schedule', color: 'bg-green' },
    { label: 'Salles', value: '5', icon: 'meeting_room', color: 'bg-purple' },
    { label: 'Présence', value: '94%', icon: 'check_circle', color: 'bg-orange' },
  ];

  tabs = [
    { key: 'personal', label: 'Informations', icon: 'person' },
    { key: 'password', label: 'Mot de passe', icon: 'lock' },
    { key: 'preferences', label: 'Préférences', icon: 'settings' },
  ];

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.profileData.name = 'Admin';
    this.profileData.email = 'admin@edu.com';
  }

  setTab(tab: string): void { this.activeTab = tab; }

  saveProfile(): void {
    this.isError = false;
    this.message = 'Profil mis à jour avec succès';
  }

  changePassword(): void {
    if (this.passwordData.newPassword !== this.passwordData.confirmPassword) {
      this.isError = true;
      this.message = 'Les mots de passe ne correspondent pas';
      return;
    }
    if (this.passwordData.newPassword.length < 6) {
      this.isError = true;
      this.message = 'Le mot de passe doit contenir au moins 6 caractères';
      return;
    }
    this.isError = false;
    this.message = 'Mot de passe changé avec succès';
    this.passwordData = { currentPassword: '', newPassword: '', confirmPassword: '' };
  }

  savePreferences(): void {
    this.isError = false;
    this.message = 'Préférences sauvegardées';
  }

  logout(): void { this.router.navigate(['/login']); }

  getInitials(): string {
    return this.profileData.name ? this.profileData.name.substring(0, 2).toUpperCase() : 'AD';
  }
}