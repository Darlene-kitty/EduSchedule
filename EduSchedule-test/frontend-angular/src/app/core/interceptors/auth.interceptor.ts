import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError, BehaviorSubject, filter, take } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { StorageService } from '../services/storage.service';
import { environment } from '../../../environments/environment';

// État partagé du refresh (évite les appels parallèles)
let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

function addToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}

function handleRefresh(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  storageService: StorageService,
  router: Router,
  http: HttpClient
) {
  if (isRefreshing) {
    // Attendre que le refresh en cours se termine
    return refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap(token => next(addToken(req, token!)))
    );
  }

  isRefreshing = true;
  refreshTokenSubject.next(null);

  const refreshToken = storageService.getItem<string>('refreshToken');

  if (!refreshToken) {
    isRefreshing = false;
    storageService.removeToken();
    storageService.removeUser();
    router.navigate(['/login']);
    return throwError(() => new Error('Session expirée. Veuillez vous reconnecter.'));
  }

  return http.post<{ token: string; refreshToken?: string }>(
    `${environment.apiUrl}/auth/refresh`,
    { refreshToken }
  ).pipe(
    switchMap(response => {
      isRefreshing = false;
      storageService.setToken(response.token);
      if (response.refreshToken) {
        storageService.setItem('refreshToken', response.refreshToken);
      }
      refreshTokenSubject.next(response.token);
      return next(addToken(req, response.token));
    }),
    catchError(err => {
      isRefreshing = false;
      storageService.removeToken();
      storageService.removeUser();
      storageService.removeItem('refreshToken');
      router.navigate(['/login']);
      return throwError(() => new Error('Session expirée. Veuillez vous reconnecter.'));
    })
  );
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const storageService = inject(StorageService);
  const http = inject(HttpClient);

  // Ne pas intercepter les appels d'auth eux-mêmes
  const isAuthCall = req.url.includes('/auth/login')
    || req.url.includes('/auth/register')
    || req.url.includes('/auth/refresh')
    || req.url.includes('/auth/forgot-password')
    || req.url.includes('/auth/reset-password');

  const token = storageService.getToken();
  // Ne pas attacher le token sur les appels d'authentification
  const authReq = (token && !isAuthCall) ? addToken(req, token) : req;

  return next(authReq).pipe(
    catchError(error => {
      if (error.status === 401 && !isAuthCall) {
        return handleRefresh(req, next, storageService, router, http);
      }
      if (error.status === 403) {
        console.warn('[403] Accès refusé:', req.method, req.url);
      }
      return throwError(() => error);
    })
  );
};
