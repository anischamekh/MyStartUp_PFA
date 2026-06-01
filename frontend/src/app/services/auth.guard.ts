import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import type { RoleName } from '../models/role-name.model';

export const authGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn) {
    return router.parseUrl('/login');
  }

  const allowedRoles = (route.data?.['roles'] as RoleName[] | undefined) ?? undefined;
  if (!allowedRoles || allowedRoles.length === 0) return true;

  const role = auth.role;
  if (role && allowedRoles.includes(role)) return true;

  return router.parseUrl('/dashboard');
};

