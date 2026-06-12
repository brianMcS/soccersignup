import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { UserService } from './user.service';
import { isJwtUsable } from '../utils/jwt-token';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const userService = inject(UserService);
  const isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  const storedToken = isBrowser ? localStorage.getItem('auth_token') : null;
  const token = storedToken && isJwtUsable(storedToken) ? storedToken : null;

  if (storedToken && !token) {
    localStorage.removeItem('auth_token');
    userService.clear();
  }

  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        if (isBrowser) {
          localStorage.removeItem('auth_token');
        }
        userService.clear();
        router.navigate(['/register']);
      }
      return throwError(() => error);
    })
  );
};
