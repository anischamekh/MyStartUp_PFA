import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { NotifyService } from '../ui/notify.service';
import { I18nService } from './i18n.service';
import { apiErrorMessage } from '../utils/api-error';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const notify = inject(NotifyService);
  const i18n = inject(I18nService);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !req.url.includes('/auth/login') && !req.url.includes('/auth/refresh')) {
        return auth.refreshSession().pipe(
          switchMap(() => {
            const token = auth.token;
            return next(
              req.clone({
                withCredentials: true,
                setHeaders: token ? { Authorization: `Bearer ${token}` } : {}
              })
            );
          }),
          catchError(() => {
            auth.logout().subscribe();
            notify.showKey('error', 'error.sessionExpired');
            return throwError(() => err);
          })
        );
      }
      if (err.status >= 500) {
        notify.show('error', apiErrorMessage(err, i18n.t('error.server')));
      }
      return throwError(() => err);
    })
  );
};
