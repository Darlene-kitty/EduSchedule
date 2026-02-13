import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { 
  Calendar, LayoutDashboard, Users, Clock, BookOpen, 
  Package, AlertTriangle, BarChart3, Bell, LogOut, 
  User, UserCheck, Settings, School, Brain, TrendingUp 
} from 'lucide-angular';

interface MenuItem {
  icon: any;
  label: string;
  href: string;
  key: string;
  roles: string[];
  badge?: string | number;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  @Input() activePage: string = 'dashboard';
  
  menuItems: MenuItem[] = [
    {
      icon: LayoutDashboard,
      label: 'Tableau de bord',
      href: '/dashboard',
      key: 'dashboard',
      roles: ['admin', 'teacher', 'student']
    },
    {
      icon: User,
      label: 'Mon Profil',
      href: '/profile',
      key: 'profile',
      roles: ['admin', 'teacher', 'student']
    },
    {
      icon: Users,
      label: 'Utilisateurs',
      href: '/users',
      key: 'users',
      roles: ['admin']
    },
    {
      icon: School,
      label: 'Écoles',
      href: '/schools',
      key: 'schools',
      roles: ['admin']
    },
    {
      icon: Clock,
      label: 'Emplois du temps',
      href: '/schedule',
      key: 'schedule',
      roles: ['admin', 'teacher', 'student']
    },
    {
      icon: Calendar,
      label: 'Calendrier',
      href: '/calendar',
      key: 'calendar',
      roles: ['admin', 'teacher', 'student']
    },
    {
      icon: UserCheck,
      label: 'Disponibilités',
      href: '/teacher-availability',
      key: 'teacher-availability',
      roles: ['admin', 'teacher']
    },
    {
      icon: Calendar,
      label: 'Réservation salles',
      href: '/reservations',
      key: 'reservations',
      roles: ['admin', 'teacher']
    },
    {
      icon: Package,
      label: 'Ressources',
      href: '/resources',
      key: 'resources',
      roles: ['admin', 'teacher']
    },
    {
      icon: BookOpen,
      label: 'Cours & Groupes',
      href: '/courses',
      key: 'courses',
      roles: ['admin', 'teacher']
    },
    {
      icon: AlertTriangle,
      label: 'Conflits',
      href: '/conflicts',
      key: 'conflicts',
      roles: ['admin'],
      badge: 3
    },
    {
      icon: Bell,
      label: 'Notifications',
      href: '/notifications',
      key: 'notifications',
      roles: ['admin', 'teacher', 'student'],
      badge: 2
    },
    {
      icon: BarChart3,
      label: 'Rapports',
      href: '/reports',
      key: 'reports',
      roles: ['admin']
    },
    {
      icon: TrendingUp,
      label: 'Analytics',
      href: '/analytics',
      key: 'analytics',
      roles: ['admin']
    },
    {
      icon: Brain,
      label: 'IA Prédictive',
      href: '/ai-insights',
      key: 'ai-insights',
      roles: ['admin']
    },
    {
      icon: Settings,
      label: 'Paramètres',
      href: '/settings',
      key: 'settings',
      roles: ['admin']
    }
  ];

  currentUser = {
    name: 'Admin User',
    role: 'admin',
    avatar: '/assets/avatar.png'
  };

  onLogout() {
    // TODO: Implement logout logic
    console.log('Logout clicked');
  }
}
