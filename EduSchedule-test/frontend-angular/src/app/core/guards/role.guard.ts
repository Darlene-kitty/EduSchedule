import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService, UserRole } from '../services/auth.service';

/**
 * Guard de rôle — usage dans les routes :
 *   canActivate: [authGuard, roleGuard('ADMIN')]
 *   canActivate: [authGuard, roleGuard('ADMIN', 'TEACHER')]
 */
export const roleGuard = (...allowedRoles: UserRole[]): CanActivateFn => {
  return () => {
    const auth   = inject(AuthService);
    const router = inject(Router);

    if (auth.hasRole(...allowedRoles)) {
      return true;
    }

    // Redirige vers le dashboard avec un message d'accès refusé
    router.navigate(['/dashboard'], { queryParams: { forbidden: true } });
    return false;
  };
};
