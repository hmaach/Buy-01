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

    const userRole = authService.getUserRole() as Role | null;

    if (!userRole) {
      router.navigate(['/login']);
      return false;
    }

    if (allowedRoles.includes(userRole)) {
      return true;
    }

    router.navigate(['/']);
    return false;
  };
};
