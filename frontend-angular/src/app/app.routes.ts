import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'forgot-password',
    loadComponent: () => import('./features/forgot-password/forgot-password').then(m => m.ForgotPasswordComponent)
  },
  {
    path: 'reset-password',
    loadComponent: () => import('./features/reset-password/reset-password').then(m => m.ResetPasswordComponent)
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
  
  // Protected routes — accessibles à tous les utilisateurs connectés
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

  // ADMIN + TEACHER
  {
    path: 'courses',
    loadComponent: () => import('./features/courses/courses.component').then(m => m.CoursesComponent),
    canActivate: [authGuard, roleGuard('ADMIN', 'TEACHER')]
  },
  {
    path: 'schedule',
    loadComponent: () => import('./features/schedule/schedule').then(m => m.SchedulesComponent),
    canActivate: [authGuard, roleGuard('ADMIN', 'TEACHER')]
  },
  {
    path: 'calendar',
    loadComponent: () => import('./features/calendar/calendar').then(m => m.CalendarComponent),
    canActivate: [authGuard, roleGuard('ADMIN', 'TEACHER')]
  },
  {
    path: 'reservations',
    loadComponent: () => import('./features/reservations/reservations').then(m => m.ReservationsComponent),
    canActivate: [authGuard, roleGuard('ADMIN', 'TEACHER')]
  },
  {
    path: 'teacher-availability',
    loadComponent: () => import('./features/teacher-availability/teacher-availability').then(m => m.TeacherAvailabilityComponent),
    canActivate: [authGuard, roleGuard('ADMIN', 'TEACHER')]
  },
  {
    path: 'notifications',
    loadComponent: () => import('./features/notifications/notifications').then(m => m.NotificationsComponent),
    canActivate: [authGuard, roleGuard('ADMIN', 'TEACHER')]
  },
  {
    path: 'reports',
    loadComponent: () => import('./features/reports/reports').then(m => m.ReportsComponent),
    canActivate: [authGuard, roleGuard('ADMIN', 'TEACHER')]
  },
  {
    path: 'rooms',
    loadComponent: () => import('./features/rooms/rooms').then(m => m.Rooms),
    canActivate: [authGuard, roleGuard('ADMIN', 'TEACHER')]
  },
  {
    path: 'resources',
    loadComponent: () => import('./features/resources/resources').then(m => m.ResourcesComponent),
    canActivate: [authGuard, roleGuard('ADMIN', 'TEACHER')]
  },
  {
    path: 'events',
    loadComponent: () => import('./features/events/events').then(m => m.EventsComponent),
    canActivate: [authGuard, roleGuard('ADMIN', 'TEACHER')]
  },

  // ADMIN uniquement
  {
    path: 'users',
    loadComponent: () => import('./features/users/users').then(m => m.UsersComponent),
    canActivate: [authGuard, roleGuard('ADMIN')]
  },
  {
    path: 'students',
    loadComponent: () => import('./features/students/students').then(m => m.StudentsComponent),
    canActivate: [authGuard, roleGuard('ADMIN')]
  },
  {
    path: 'schools',
    loadComponent: () => import('./features/schools/schools').then(m => m.SchoolsComponent),
    canActivate: [authGuard, roleGuard('ADMIN')]
  },
  {
    path: 'filieres',
    loadComponent: () => import('./features/filieres/filieres').then(m => m.FilieresComponent),
    canActivate: [authGuard, roleGuard('ADMIN')]
  },
  {
    path: 'niveaux',
    loadComponent: () => import('./features/niveaux/niveaux').then(m => m.NiveauxComponent),
    canActivate: [authGuard, roleGuard('ADMIN')]
  },
  {
    path: 'classes',
    loadComponent: () => import('./features/classes/classes').then(m => m.ClassesComponent),
    canActivate: [authGuard, roleGuard('ADMIN')]
  },
  {
    path: 'categories-ue',
    loadComponent: () => import('./features/categories-ue/categories-ue').then(m => m.CategoriesUeComponent),
    canActivate: [authGuard, roleGuard('ADMIN')]
  },
  {
    path: 'equipment-types',
    loadComponent: () => import('./features/equipment-types/equipment-types').then(m => m.EquipmentTypesComponent),
    canActivate: [authGuard, roleGuard('ADMIN')]
  },
  {
    path: 'equipment',
    loadComponent: () => import('./features/equipment/equipment').then(m => m.EquipmentComponent),
    canActivate: [authGuard, roleGuard('ADMIN')]
  },
  {
    path: 'conflicts',
    loadComponent: () => import('./features/conflicts/conflicts').then(m => m.ConflictsComponent),
    canActivate: [authGuard, roleGuard('ADMIN')]
  },
  {
    path: 'timetable-generator',
    loadComponent: () => import('./features/timetable-generator/timetable-generator')
      .then(m => m.TimetableGeneratorComponent),
    canActivate: [authGuard, roleGuard('ADMIN')]
  },
  {
    path: 'exam-scheduling',
    loadComponent: () => import('./features/exam-scheduling/exam-scheduling')
      .then(m => m.ExamSchedulingComponent),
    canActivate: [authGuard, roleGuard('ADMIN')]
  },
  // Fallback
  {
    path: '**',
    redirectTo: '/welcome'
  }
];
