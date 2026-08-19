import { HttpInterceptorFn } from '@angular/common/http';
import { env } from '../../../../environments/environment';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  console.log('Intercepted request:', req.url);

  const modifiedReq = req.clone({
    url: req.url.startsWith('http')
      ? req.url
      : `${env.backendUrl}${req.url}`,
  });

  return next(modifiedReq);
};