import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../../models/user.model';

export const roleGuard = (allowedRoles: Role[]): CanActivateFn => {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const hasValidToken = authService.getToken() !== null;

    if (!authService.isAuthenticated && !hasValidToken) {
      router.navigate(['/login']);
      return false;
    }

    const user = authService.user;

    if (!user && hasValidToken) {
      router.navigate(['/login']);
      return false;
    }

    if (user && allowedRoles.includes(user.role)) {
      return true;
    }

    router.navigate(['/products']);
    return false;
  };
};
