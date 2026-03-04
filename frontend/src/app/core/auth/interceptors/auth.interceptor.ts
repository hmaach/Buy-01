import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { env } from '../../../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  console.log(`[HTTP] ${req.method} ${req.url}`);
  const modifiedReq = req.clone({
    url: `${env.backendUrl}${req.url}`,
  });

  if (token) {
    const authReq = modifiedReq.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    console.log('[HTTP] Added Authorization header');
    return next(authReq);
  }

  return next(modifiedReq);
};
