import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environment';

/** Sends cookies (HttpOnly JWT) on every API request; no Bearer token in localStorage. */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }
  return next(req.clone({ withCredentials: true }));
};
