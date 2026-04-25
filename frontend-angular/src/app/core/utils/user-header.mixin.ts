/**
 * Mixin utilitaire pour les composants qui affichent l'utilisateur connecté dans leur header.
 * Usage : appeler initUserHeader(authService) dans ngOnInit().
 */
import { AuthService } from '../services/auth.service';

export interface UserHeaderFields {
  currentUserName: string;
  currentUserInitials: string;
  unreadCount: number;
}

export function initUserHeader(
  component: UserHeaderFields,
  authService: AuthService
): void {
  const user = authService.getUser();
  if (!user) return;

  const name =
    (user.name) ||
    ([user.firstName, user.lastName].filter(Boolean).join(' ')) ||
    user.username ||
    user.email ||
    'Utilisateur';

  component.currentUserName = name;

  const parts = name.trim().split(' ').filter(Boolean);
  component.currentUserInitials =
    parts.length >= 2
      ? (parts[0][0] + parts[1][0]).toUpperCase()
      : name.substring(0, 2).toUpperCase();
}
