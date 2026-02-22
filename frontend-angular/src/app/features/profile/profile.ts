import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class ProfileComponent implements OnInit {
  activeTab = 'personal';
  message = '';
  isError = false;
  isEditMode = false;
  currentDate = '';
  currentTime = '';

  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  profileData = {
    name: 'Admin Système',
    email: 'admin@edu.com',
    phone: '01 23 45 67 89',
    address: 'Paris, France',
    bio: 'Administrateur de la plateforme EduSchedule. Responsable de la gestion des emplois du temps et des ressources pédagogiques.',
    department: 'Administration',
    specialization: 'Gestion scolaire'
  };

  profileDataEdit: any = { ...this.profileData };

  passwordData = { currentPassword: '', newPassword: '', confirmPassword: '' };

  preferences = { emailNotifications: true, pushNotifications: false, language: 'fr' };

  stats = [
    { label: 'Cours',    value: '12',  icon: 'menu_book',    color: 'bg-blue'   },
    { label: 'Heures',   value: '48',  icon: 'schedule',     color: 'bg-green'  },
    { label: 'Salles',   value: '5',   icon: 'meeting_room', color: 'bg-purple' },
    { label: 'Présence', value: '94%', icon: 'check_circle', color: 'bg-orange' },
  ];

  tabs = [
    { key: 'personal',     label: 'Informations',  icon: 'person'   },
    { key: 'password',     label: 'Mot de passe',  icon: 'lock'     },
    { key: 'preferences',  label: 'Préférences',   icon: 'settings' },
  ];

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  setTab(tab: string): void { this.activeTab = tab; this.message = ''; }

  enterEditMode(): void { this.profileDataEdit = { ...this.profileData }; this.isEditMode = true; this.message = ''; }
  cancelEdit(): void { this.isEditMode = false; this.message = ''; }

  saveProfile(): void {
    if (!this.profileDataEdit.name?.trim()) { this.isError = true; this.message = 'Le nom est obligatoire'; return; }
    this.profileData = { ...this.profileDataEdit };
    this.isEditMode = false; this.isError = false; this.message = 'Profil mis à jour avec succès';
  }

  changePassword(): void {
    if (this.passwordData.newPassword !== this.passwordData.confirmPassword) { this.isError = true; this.message = 'Les mots de passe ne correspondent pas'; return; }
    if (this.passwordData.newPassword.length < 6) { this.isError = true; this.message = 'Le mot de passe doit contenir au moins 6 caractères'; return; }
    this.isError = false; this.message = 'Mot de passe changé avec succès';
    this.passwordData = { currentPassword: '', newPassword: '', confirmPassword: '' };
  }

  savePreferences(): void { this.isError = false; this.message = 'Préférences sauvegardées'; }
  logout(): void { this.router.navigate(['/login']); }
  goBack(): void { this.router.navigate(['/dashboard']); }
  getInitials(): string { return this.profileData.name ? this.profileData.name.substring(0, 2).toUpperCase() : 'AD'; }
  
}