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

  // Only prepend backend URL for relative paths (not already absolute URLs)
  let modifiedReq = req;
  if (!req.url.startsWith('http')) {
    modifiedReq = req.clone({
      url: `${env.backendUrl}${req.url}`,
    });
  }

  console.log(`[HTTP] ${req.method} ${modifiedReq.url}`);

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
