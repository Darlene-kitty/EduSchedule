import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

interface MenuItem {
  icon: string;
  label: string;
  href: string;
  key: string;
  badge?: number;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})

export class SidebarComponent {
  @Input() activePage: string = 'dashboard';

  menuItems: MenuItem[] = [
    { icon: 'dashboard',        label: 'Tableau de bord',     href: '/dashboard',            key: 'dashboard' },
    { icon: 'people',           label: 'Utilisateurs',        href: '/users',                key: 'users' },
    { icon: 'schedule',         label: 'Emplois du temps',    href: '/schedule',             key: 'schedule' },
    { icon: 'calendar_month',   label: 'Réservation salles',  href: '/reservations',         key: 'reservations' },
    { icon: 'menu_book',        label: 'Cours & Groupes',     href: '/courses',              key: 'courses' },
    { icon: 'meeting_room',     label: 'Ressources',          href: '/resources',            key: 'resources' },
    { icon: 'event_available',  label: 'Disponibilités',      href: '/teacher-availability', key: 'teacher-availability' },
    { icon: 'calendar_today',   label: 'Calendrier',          href: '/calendar',             key: 'calendar' },
    { icon: 'event',            label: 'Événements',          href: '/events',               key: 'events' },
    { icon: 'warning',          label: 'Conflits',            href: '/conflicts',            key: 'conflicts',     badge: 3 },
    { icon: 'notifications',    label: 'Notifications',       href: '/notifications',        key: 'notifications', badge: 2 },
    { icon: 'bar_chart',        label: 'Rapports',            href: '/reports',              key: 'reports' },
    { icon: 'trending_up',      label: 'Analytics',           href: '/analytics',            key: 'analytics' },
    { icon: 'psychology',       label: 'IA Prédictive',       href: '/ai-insights',          key: 'ai-insights' },
    { icon: 'settings',         label: 'Paramètres',          href: '/settings',             key: 'settings' },
  ];

  currentUser = {
    name: 'Admin Système',
    role: 'Administrateur',
    initials: 'AS',
    avatar: '/assets/avatar.png'
  };

  constructor(private router: Router) {}

  isActive(key: string): boolean {
    return this.activePage === key;
  }

  onLogout(): void {
    this.router.navigate(['/login']);
  }
}