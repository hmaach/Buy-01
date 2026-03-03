import { HttpInterceptorFn } from '@angular/common/http';


export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  // Intercept the request here
  console.log('Intercepted request:', req.url); //

  const modifiedReq = req.clone({
    url: `http://localhost:8080${req.url}`,
  });

  // Pass the modified request to the next handler
  return next(modifiedReq);
};