import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { StorageService } from '../services/storage.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const storageService = inject(StorageService);
  
  const token = storageService.getToken();

  // Clone the request and add authorization header if token exists
  const authReq = token
    ? req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      })
    : req;

  return next(authReq).pipe(
    catchError((error) => {
      if (error.status === 401) {
        storageService.removeToken();
        storageService.removeUser();
        router.navigate(['/login']);
      } else if (error.status === 403) {
        // Token valide mais accès refusé — log pour debug
        console.warn('[403] Accès refusé:', req.method, req.url);
      }
      return throwError(() => error);
    })
  );
};
