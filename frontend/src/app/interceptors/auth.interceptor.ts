import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const token = localStorage.getItem('token');
  const esLogin = req.url.includes('/api/auth/login');

  const peticion = token && !esLogin
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(peticion).pipe(
    catchError((error: HttpErrorResponse) => {
      if (!esLogin && (error.status === 401 || error.status === 403)) {
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
