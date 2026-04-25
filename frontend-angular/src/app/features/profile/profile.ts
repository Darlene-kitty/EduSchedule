import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { AuthService } from '../../core/services/auth.service';
import { ProfileService } from '../../core/services/profile.service';
import { UsersManagementService } from '../../core/services/users-management.service';
import { PreferencesService } from '../../core/services/preferences.service';
import { NotificationsManagementService } from '../../core/services/notifications-management.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class ProfileComponent implements OnInit {
  private router = inject(Router);
  private authService = inject(AuthService);
  private profileService = inject(ProfileService);
  private usersMgmtService = inject(UsersManagementService);
  private preferencesService = inject(PreferencesService);
  private notifService = inject(NotificationsManagementService);

  activeTab = 'personal';
  message = '';
  isError = false;
  isEditMode = false;
  isSaving = false;
  currentDate = '';
  currentTime = '';
  /** Nom de l'utilisateur connecté affiché dans le header */
  currentUserName = '';
  /** Nombre de notifications non lues (header badge) */
  unreadCount = 0;

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
    { label: 'Cours',    value: '—',  icon: 'menu_book',    color: 'bg-blue'   },
    { label: 'Heures',   value: '—',  icon: 'schedule',     color: 'bg-green'  },
    { label: 'Salles',   value: '—',  icon: 'meeting_room', color: 'bg-purple' },
    { label: 'Présence', value: '—',  icon: 'check_circle', color: 'bg-orange' },
  ];

  tabs = [
    { key: 'personal',     label: 'Informations',  icon: 'person'   },
    { key: 'password',     label: 'Mot de passe',  icon: 'lock'     },
    { key: 'preferences',  label: 'Préférences',   icon: 'settings' },
  ];

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadUserData();
    this.loadPreferences();
  }

  private loadUserData(): void {
    const user = this.authService.getUser();
    if (user) {
      const name = user.name || [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username || 'Utilisateur';
      this.currentUserName = name;
      this.profileData = {
        name,
        email: user.email || this.profileData.email,
        phone: user.phone || this.profileData.phone,
        address: user.address || this.profileData.address,
        bio: user.bio || this.profileData.bio,
        department: user.department || this.profileData.department,
        specialization: user.specialization || this.profileData.specialization
      };
      // Charger les données fraîches depuis le backend si on a un ID
      if (user.id) {
        this.usersMgmtService.getUserById(user.id).subscribe({
          next: u => {
            const freshName = u.name || [u.firstName, u.lastName].filter(Boolean).join(' ') || this.profileData.name;
            this.currentUserName = freshName;
            this.profileData = {
              name: freshName,
              email: u.email || this.profileData.email,
              phone: u.phone || this.profileData.phone,
              address: this.profileData.address,
              bio: this.profileData.bio,
              department: u.department || this.profileData.department,
              specialization: this.profileData.specialization
            };
          },
          error: () => {}
        });
      }
    }
  }

  private loadPreferences(): void {
    const savedPreferences = this.preferencesService.getPreferences();
    this.preferences = {
      emailNotifications: savedPreferences.emailNotifications ?? true,
      pushNotifications: savedPreferences.notifications ?? false,
      language: savedPreferences.language || 'fr'
    };
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
    if (!this.profileDataEdit.name?.trim()) {
      this.isError = true;
      this.message = 'Le nom est obligatoire';
      return;
    }
    this.isSaving = true;
    const user = this.authService.getUser();
    const nameParts = this.profileDataEdit.name.trim().split(' ');
    const firstName = nameParts[0] || '';
    const lastName  = nameParts.slice(1).join(' ') || '';

    this.usersMgmtService.updateUser(user.id, {
      firstName,
      lastName,
      email:      this.profileDataEdit.email,
      phone:      this.profileDataEdit.phone,
      department: this.profileDataEdit.department,
      enabled:    true
    }).subscribe({
      next: (updated) => {
        this.profileData = { ...this.profileDataEdit };
        // Mettre à jour le cache local
        this.authService.setUser({ ...user, ...updated, name: this.profileDataEdit.name });
        this.isEditMode = false;
        this.isError = false;
        this.isSaving = false;
        this.message = 'Profil mis à jour avec succès';
        setTimeout(() => this.message = '', 3000);
      },
      error: () => {
        this.isSaving = false;
        this.isError = true;
        this.message = 'Erreur lors de la mise à jour du profil';
      }
    });
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
    this.isSaving = true;
    this.profileService.changePassword({
      currentPassword: this.passwordData.currentPassword,
      newPassword:     this.passwordData.newPassword,
      confirmPassword: this.passwordData.confirmPassword
    }).subscribe({
      next: () => {
        this.isSaving = false;
        this.isError = false;
        this.message = 'Mot de passe changé avec succès';
        this.passwordData = { currentPassword: '', newPassword: '', confirmPassword: '' };
        setTimeout(() => this.message = '', 3000);
      },
      error: () => {
        this.isSaving = false;
        this.isError = true;
        this.message = 'Mot de passe actuel incorrect';
      }
    });
  }

  savePreferences(): void {
    const userId = this.authService.getUser()?.id;
    // Sauvegarder localement
    this.preferencesService.updatePreferences({
      emailNotifications: this.preferences.emailNotifications,
      notifications:      this.preferences.pushNotifications,
      language:           this.preferences.language
    });
    // Persister au backend si userId disponible
    if (userId) {
      this.notifService.savePreferences(userId, {
        emailEnabled:           this.preferences.emailNotifications,
        pushEnabled:            this.preferences.pushNotifications,
        scheduleChanges:        true,
        conflictAlerts:         true,
        reservationUpdates:     true,
        reminderNotifications:  true,
        reminderMinutesBefore:  30
      }).subscribe({ error: () => {} });
    }
    this.isError = false;
    this.message = 'Préférences sauvegardées';
    setTimeout(() => this.message = '', 3000);
  }

  logout(): void { 
    this.authService.logout();
  }

  goBack(): void { this.router.navigate(['/dashboard']); }
  getInitials(): string { return this.profileData.name ? this.profileData.name.substring(0, 2).toUpperCase() : 'AD'; }
  
}