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

      // Handle ProblemDetail error responses
      if (error.error && typeof error.error === 'object') {
        const problemDetail = error.error;
        const detail = problemDetail.detail || error.message;

        // Handle 401 Unauthorized with "Invalid or expired token"
        if (error.status === 401) {
          if (detail && detail.toLowerCase().includes('invalid') || detail.toLowerCase().includes('expired')) {
            console.log('[HTTP] Unauthorized - Invalid or expired token - logging out');
            localStorage.removeItem('auth_token');
            localStorage.removeItem('auth_expires_at');
            localStorage.removeItem('auth_user');
            router.navigate(['/login']);
          }
        }

        // Handle 429 Too Many Requests
        if (error.status === 429) {
          console.log('[HTTP] Too Many Requests - logging out');
          localStorage.removeItem('auth_token');
          localStorage.removeItem('auth_expires_at');
          localStorage.removeItem('auth_user');
          router.navigate(['/login']);
        }
      }

      // Legacy handling for non-ProblemDetail responses
      if (error.status === 401) {
        console.log('[HTTP] Unauthorized - redirecting to login');
        localStorage.removeItem('auth_token');
        localStorage.removeItem('auth_expires_at');
        localStorage.removeItem('auth_user');
        router.navigate(['/login']);
      }

      return throwError(() => error);
    })
  );
};
