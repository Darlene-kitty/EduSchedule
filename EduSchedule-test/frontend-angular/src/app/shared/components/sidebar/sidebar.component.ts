import { Component, Input, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';

interface MenuItem {
  icon: string;
  label: string;
  href: string;
  key: string;
  badge?: number;
  section?: string;
  roles?: string[]; // si absent = visible par tous
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

  private allMenuItems: MenuItem[] = [
    // Général
    { section: 'Général',        icon: 'dashboard',         label: 'Tableau de bord',   href: '/dashboard',            key: 'dashboard' },

    // Administration — ADMIN uniquement
    { section: 'Administration', icon: 'manage_accounts',   label: 'Utilisateurs',      href: '/users',                key: 'users',               roles: ['ADMIN'] },
    {                             icon: 'school',             label: 'Étudiants',         href: '/students',             key: 'students',            roles: ['ADMIN'] },
    {                             icon: 'account_balance',    label: 'Écoles',            href: '/schools',              key: 'schools',             roles: ['ADMIN'] },
    {                             icon: 'fork_right',         label: 'Filières',          href: '/filieres',             key: 'filieres',            roles: ['ADMIN'] },
    {                             icon: 'layers',             label: 'Niveaux',           href: '/niveaux',              key: 'niveaux',             roles: ['ADMIN'] },
    {                             icon: 'groups',             label: 'Classes',           href: '/classes',              key: 'classes',             roles: ['ADMIN'] },

    // Pédagogie — ADMIN uniquement
    { section: 'Pédagogie',      icon: 'category',           label: 'Catégories UE',     href: '/categories-ue',        key: 'categories-ue',       roles: ['ADMIN'] },
    {                             icon: 'menu_book',          label: 'Cours',             href: '/courses',              key: 'courses',             roles: ['ADMIN', 'TEACHER'] },

    // Ressources — ADMIN + TEACHER pour salles et ressources
    { section: 'Ressources',     icon: 'meeting_room',       label: 'Salles',            href: '/rooms',                key: 'rooms',               roles: ['ADMIN', 'TEACHER'] },
    {                             icon: 'devices',            label: 'Types de matériel', href: '/equipment-types',      key: 'equipment-types',     roles: ['ADMIN'] },
    {                             icon: 'inventory_2',        label: 'Matériel',          href: '/equipment',            key: 'equipment',           roles: ['ADMIN'] },
    {                             icon: 'category',           label: 'Ressources',        href: '/resources',            key: 'resources',           roles: ['ADMIN', 'TEACHER'] },

    // Planning — ADMIN + TEACHER
    { section: 'Planning',       icon: 'calendar_month',     label: 'Planification',     href: '/schedule',             key: 'schedule',            roles: ['ADMIN', 'TEACHER'] },
    {                             icon: 'event_available',    label: 'Réservations',      href: '/reservations',         key: 'reservations',        roles: ['ADMIN', 'TEACHER'] },
    {                             icon: 'calendar_view_week', label: 'Calendrier',        href: '/calendar',             key: 'calendar',            roles: ['ADMIN', 'TEACHER'] },
    {                             icon: 'event',              label: 'Événements',        href: '/events',               key: 'events',              roles: ['ADMIN', 'TEACHER'] },
    {                             icon: 'warning',            label: 'Conflits',          href: '/conflicts',            key: 'conflicts',           roles: ['ADMIN'] },
    {                             icon: 'event_note',         label: 'Disponibilités',    href: '/teacher-availability', key: 'teacher-availability',roles: ['ADMIN', 'TEACHER'] },
    {                             icon: 'auto_awesome',       label: 'Génération EDT',    href: '/timetable-generator',  key: 'timetable-generator', roles: ['ADMIN'] },
    {                             icon: 'quiz',               label: 'Planning examens',  href: '/exam-scheduling',      key: 'exam-scheduling',     roles: ['ADMIN'] },

    // Analyse
    { section: 'Analyse',        icon: 'bar_chart',          label: 'Rapports',          href: '/reports',              key: 'reports',             roles: ['ADMIN', 'TEACHER'] },
    {                             icon: 'notifications',      label: 'Notifications',     href: '/notifications',        key: 'notifications',       roles: ['ADMIN', 'TEACHER'] },

    // Compte
    { section: 'Compte',         icon: 'person',             label: 'Profil',            href: '/profile',              key: 'profile' },
    // ENT Étudiant
    {                             icon: 'calendar_view_week', label: 'Mon emploi du temps', href: '/student-schedule',   key: 'student-schedule', roles: ['STUDENT'] },
  ];

  menuItems: MenuItem[] = [];

  currentUser = { name: 'Utilisateur', role: '', initials: 'U', avatar: '/assets/avatar.png' };

  ngOnInit(): void {
    this.authService.user$.subscribe((user: any) => {
      if (user) {
        const name = user.name || user.username || 'Utilisateur';
        const role = (user.role || '').toString().toUpperCase().replace('ROLE_', '');
        this.currentUser = {
          name,
          role: role.charAt(0) + role.slice(1).toLowerCase(),
          initials: name.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2),
          avatar: user.avatar || '/assets/avatar.png'
        };
        this.menuItems = this.allMenuItems.filter(item =>
          !item.roles || item.roles.includes(role)
        );
      }
    });
  }

  /** Retourne true si cet item est le premier de sa section (pour afficher le header) */
  isSectionStart(item: MenuItem, index: number): boolean {
    if (!item.section) return false;
    const prev = this.menuItems[index - 1];
    return !prev || prev.section !== item.section;
  }

  isActive(key: string): boolean { return this.activePage === key; }

  onLogout(): void { this.authService.logout(); }
}
