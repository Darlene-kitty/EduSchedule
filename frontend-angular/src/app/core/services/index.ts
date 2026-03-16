// Export tous les services pour faciliter les imports
export * from './storage.service';
export * from './auth.service';
export * from './user.service';
export * from './users-management.service';
export * from './courses-management.service';
export * from './rooms-management.service';
export * from './reservations-management.service';
export * from './events-management.service';
export * from './resources-management.service';
export * from './notifications-management.service';
export * from './schedule-management.service';
export * from './preferences.service';
export * from './cache.service';
export * from './dashboard.service';
export * from './api.service';
// Export seulement le service, pas l'interface Course pour éviter le conflit
export { CourseService } from './course.service';
export * from './profile.service';
export * from './schedule.service';
