import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('[HTTP Error]', error.status, error.message);

      if (error.status === 401) {
        console.log('[HTTP] Unauthorized - redirecting to login');
        localStorage.removeItem('fake_jwt_token');
        localStorage.removeItem('fake_user');
        router.navigate(['/login']);
      }

      return throwError(() => error);
    })
  );
};
