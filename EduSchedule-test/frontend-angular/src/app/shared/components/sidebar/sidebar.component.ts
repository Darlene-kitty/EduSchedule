import { Component, Input, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';

interface MenuItem {
  icon: string;
  label: string;
  href: string;
  key: string;
  badge?: number;
  section?: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  @Input() activePage: string = 'dashboard';
  private authService = inject(AuthService);

  menuItems: MenuItem[] = [
    // Général
    { section: 'Général',       icon: 'dashboard',        label: 'Tableau de bord',   href: '/dashboard',        key: 'dashboard' },

    // Administration
    { section: 'Administration', icon: 'manage_accounts',  label: 'Utilisateurs',      href: '/users',            key: 'users' },
    {                            icon: 'school',            label: 'Étudiants',         href: '/students',         key: 'students' },
    {                            icon: 'account_balance',   label: 'Écoles',            href: '/schools',          key: 'schools' },
    {                            icon: 'fork_right',        label: 'Filières',          href: '/filieres',         key: 'filieres' },
    {                            icon: 'layers',            label: 'Niveaux',           href: '/niveaux',          key: 'niveaux' },
    {                            icon: 'groups',            label: 'Classes',           href: '/classes',          key: 'classes' },

    // Pédagogie
    { section: 'Pédagogie',     icon: 'category',          label: 'Catégories UE',     href: '/categories-ue',    key: 'categories-ue' },
    {                            icon: 'psychology',        label: 'Compétences',       href: '/competences',      key: 'competences' },

    // Ressources
    { section: 'Ressources',    icon: 'meeting_room',      label: 'Salles',            href: '/rooms',            key: 'rooms' },
    {                            icon: 'devices',           label: 'Types de matériel', href: '/equipment-types',  key: 'equipment-types' },
    {                            icon: 'inventory_2',       label: 'Matériel',          href: '/equipment',        key: 'equipment' },

    // Planning
    { section: 'Planning',      icon: 'calendar_month',    label: 'Planification',     href: '/schedule',         key: 'schedule' },
    {                            icon: 'event_available',   label: 'Réservation',       href: '/reservations',     key: 'reservations' },
    {                            icon: 'event_note',        label: 'Disponibilités',    href: '/teacher-availability', key: 'teacher-availability' },
    {                            icon: 'auto_awesome',      label: 'Génération EDT',    href: '/timetable-generator', key: 'timetable-generator' },

    // Analyse
    { section: 'Analyse',       icon: 'bar_chart',         label: 'Rapports',          href: '/reports',          key: 'reports' },
  ];

  currentUser = {
    name: 'Utilisateur',
    role: '',
    initials: 'U',
    avatar: '/assets/avatar.png'
  };

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.authService.user$.subscribe((user: any) => {
      if (user) {
        const name = user.name || user.username || 'Utilisateur';
        const role = user.role || '';
        this.currentUser = {
          name,
          role: role.charAt(0) + role.slice(1).toLowerCase(),
          initials: name.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2),
          avatar: user.avatar || '/assets/avatar.png'
        };
      }
    });
  }

  isActive(key: string): boolean {
    return this.activePage === key;
  }

  onLogout(): void {
    this.authService.logout();
  }
}
