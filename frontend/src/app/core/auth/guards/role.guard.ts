import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../../models/user.model';

export const roleGuard = (allowedRoles: Role[]): CanActivateFn => {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated) {
      router.navigate(['/login']);
      return false;
    }

    const user = authService.user();
    if (user && allowedRoles.includes(user.role)) {
      return true;
    }

    // Redirect to products if not authorized
    router.navigate(['/products']);
    return false;
  };
};
