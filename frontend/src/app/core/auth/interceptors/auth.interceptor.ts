import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { env } from '../../../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
) => {
  const token = localStorage.getItem('auth_token');

  let modifiedReq = req;

  if (!req.url.startsWith('http')) {
    modifiedReq = req.clone({
      url: `${env.backendUrl}${req.url}`,
    });
  }

  if (token) {
    modifiedReq = modifiedReq.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  return next(modifiedReq);
};
