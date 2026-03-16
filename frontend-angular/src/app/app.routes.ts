import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'forgot-password',
    loadComponent: () => import('./features/forgot-password/forgot-password').then(m => m.ForgotPasswordComponent)
  },
  // Public routes
  {
    path: '',
    redirectTo: '/welcome',
    pathMatch: 'full'
  },
  {
    path: 'welcome',
    loadComponent: () => import('./features/welcome/welcome.component').then(m => m.WelcomeComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/register/register').then(m => m.Register)
  },
  
  // Protected routes
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'profile',
    loadComponent: () => import('./features/profile/profile').then(m => m.ProfileComponent),
    canActivate: [authGuard]
  },
  {
    path: 'users',
    loadComponent: () => import('./features/users/users').then(m => m.UsersComponent),
    canActivate: [authGuard]
  },
  {
    path: 'courses',
    loadComponent: () => import('./features/courses/courses.component').then(m => m.CoursesComponent),
    canActivate: [authGuard]
  },
  {
    path: 'schedule',
    loadComponent: () => import('./features/schedule/schedule').then(m => m.SchedulesComponent),
    canActivate: [authGuard]
  },
  {
    path: 'calendar',
    loadComponent: () => import('./features/calendar/calendar').then(m => m.CalendarComponent),
    canActivate: [authGuard]
  },
  {
    path: 'reservations',
    loadComponent: () => import('./features/reservations/reservations').then(m => m.ReservationsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'resources',
    loadComponent: () => import('./features/resources/resources').then(m => m.ResourcesComponent),
    canActivate: [authGuard]
  },
  {
    path: 'rooms',
    loadComponent: () => import('./features/rooms/rooms').then(m => m.Rooms),
    canActivate: [authGuard]
  },
  {
    path: 'conflicts',
    loadComponent: () => import('./features/conflicts/conflicts').then(m => m.ConflictsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'notifications',
    loadComponent: () => import('./features/notifications/notifications').then(m => m.NotificationsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'reports',
    loadComponent: () => import('./features/reports/reports').then(m => m.ReportsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'teacher-availability',
    loadComponent: () => import('./features/teacher-availability/teacher-availability').then(m => m.TeacherAvailabilityComponent),
    canActivate: [authGuard]
  },
  {
    path: 'events',
    loadComponent: () => import('./features/events/events').then(m => m.EventsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'students',
    loadComponent: () => import('./features/students/students').then(m => m.StudentsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'schools',
    loadComponent: () => import('./features/schools/schools').then(m => m.SchoolsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'filieres',
    loadComponent: () => import('./features/filieres/filieres').then(m => m.FilieresComponent),
    canActivate: [authGuard]
  },
  {
    path: 'niveaux',
    loadComponent: () => import('./features/niveaux/niveaux').then(m => m.NiveauxComponent),
    canActivate: [authGuard]
  },
  {
    path: 'classes',
    loadComponent: () => import('./features/classes/classes').then(m => m.ClassesComponent),
    canActivate: [authGuard]
  },
  {
    path: 'categories-ue',
    loadComponent: () => import('./features/categories-ue/categories-ue').then(m => m.CategoriesUeComponent),
    canActivate: [authGuard]
  },
  {
    path: 'competences',
    loadComponent: () => import('./features/competences/competences').then(m => m.CompetencesComponent),
    canActivate: [authGuard]
  },
  {
    path: 'equipment-types',
    loadComponent: () => import('./features/equipment-types/equipment-types').then(m => m.EquipmentTypesComponent),
    canActivate: [authGuard]
  },
  {
    path: 'equipment',
    loadComponent: () => import('./features/equipment/equipment').then(m => m.EquipmentComponent),
    canActivate: [authGuard]
  },

  // Fallback
  {
    path: '**',
    redirectTo: '/welcome'
  }
];
