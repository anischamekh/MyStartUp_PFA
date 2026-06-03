import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

/** Sends cookies + Bearer token (memory only) on API requests. */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }
  const auth = inject(AuthService);
  const token = auth.token;
  const cloned = req.clone({
    withCredentials: true,
    setHeaders: token ? { Authorization: `Bearer ${token}` } : {}
  });
  return next(cloned);
};
