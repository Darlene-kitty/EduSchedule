import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';

const ERROR_MESSAGES: Record<number, string> = {
  400: 'Requête invalide.',
  401: 'Session expirée, veuillez vous reconnecter.',
  403: 'Accès refusé.',
  404: 'Ressource introuvable.',
  409: 'Conflit : la ressource existe déjà.',
  422: 'Données invalides.',
  500: 'Erreur serveur interne.',
  502: 'Service temporairement indisponible.',
  503: 'Service indisponible.',
};

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Ne pas afficher de toast pour les requêtes de polling (status)
      const isPolling = req.url.includes('/status/');
      if (!isPolling) {
        const serverMsg = error.error?.message || error.error?.error;
        const message = serverMsg || ERROR_MESSAGES[error.status] || `Erreur ${error.status}`;
        toast.show(message, error.status >= 500 ? 'error' : 'warning');
      }
      return throwError(() => error);
    })
  );
};
