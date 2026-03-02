import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { ModuleAccessService } from '../services/module-access.service';
import { AuthService } from '../../modules/auth/services/auth.service';

export const moduleAccessGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const moduleAccessService = inject(ModuleAccessService);
  const router = inject(Router);
  const moduleKey = route.data['moduleKey'] as string | undefined;

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/auth/login']);
  }

  if (!moduleKey) {
    return true;
  }

  return moduleAccessService.fetchMyAccess().pipe(
    map((modules) => modules.has(moduleKey) || authService.getRole() === 'SUPERADMINISTRADOR'
      ? true
      : router.createUrlTree(['/profile'])),
    catchError(() => of(router.createUrlTree(['/profile'])))
  );
};
