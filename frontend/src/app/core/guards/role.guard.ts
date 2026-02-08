import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import { inject } from '@angular/core';

import { AuthService } from '../../modules/auth/services/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const expectedRoles = route.data['roles'] as string[] | undefined;

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/auth/login']);
  }

  if (!expectedRoles || expectedRoles.length === 0) {
    return true;
  }

  const role = authService.getRole();
  if (role && expectedRoles.includes(role)) {
    return true;
  }
  return router.createUrlTree(['/profile']);
};
