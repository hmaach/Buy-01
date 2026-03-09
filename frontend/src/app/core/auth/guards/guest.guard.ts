import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const guestGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const hasValidToken = authService.getToken() !== null;

  if (!authService.isAuthenticated && !hasValidToken) {
    return true;
  }

  router.navigate(['/products']);
  return false;
};
