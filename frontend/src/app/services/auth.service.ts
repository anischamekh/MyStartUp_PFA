import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, catchError, finalize, map, Observable, of, shareReplay, tap } from 'rxjs';
import { ApiService } from './api.service';
import type { User } from '../models/user.model';
import type { RoleName } from '../models/role-name.model';

export interface SessionUser {
  userId: number;
  username: string;
  fullName: string;
  role: RoleName;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);

  private readonly _auth$ = new BehaviorSubject<SessionUser | null>(null);
  readonly auth$ = this._auth$.asObservable();
  private refreshInFlight: Observable<SessionUser | null> | null = null;

  get role(): RoleName | null {
    return this._auth$.value?.role ?? null;
  }

  get userId(): number | null {
    return this._auth$.value?.userId ?? null;
  }

  viewOnlyAdmin(): boolean {
    return this.role === 'ADMIN';
  }

  get isLoggedIn(): boolean {
    return !!this._auth$.value;
  }

  /** Restore session from HttpOnly cookies (no localStorage). */
  restoreSession() {
    return this.api.client
      .get<User>(`${this.api.baseUrl}/users/me`, { withCredentials: true })
      .pipe(
        map((u) => this.toSessionUser(u)),
        tap((user) => this._auth$.next(user)),
        catchError(() => {
          this._auth$.next(null);
          return of(null);
        })
      );
  }

  login(username: string, password: string) {
    return this.api.client
      .post<SessionUser>(`${this.api.baseUrl}/auth/login`, { username, password }, { withCredentials: true })
      .pipe(tap((user) => this._auth$.next(user)));
  }

  refreshSession() {
    if (this.refreshInFlight) {
      return this.refreshInFlight;
    }
    this.refreshInFlight = this.api.client
      .post<SessionUser>(`${this.api.baseUrl}/auth/refresh`, {}, { withCredentials: true })
      .pipe(
        tap((user) => this._auth$.next(user)),
        catchError(() => {
          this._auth$.next(null);
          return of(null);
        }),
        finalize(() => {
          this.refreshInFlight = null;
        }),
        shareReplay(1)
      );
    return this.refreshInFlight;
  }

  logout() {
    return this.api.client
      .post(`${this.api.baseUrl}/auth/logout`, {}, { withCredentials: true })
      .pipe(
        tap(() => this._auth$.next(null)),
        catchError(() => {
          this._auth$.next(null);
          return of(null);
        })
      );
  }

  hasAnyRole(roles: RoleName[]) {
    return this.auth$.pipe(map((a) => !!a && roles.includes(a.role)));
  }

  private toSessionUser(u: User): SessionUser {
    return {
      userId: u.id ?? 0,
      username: u.username,
      fullName: u.fullName,
      role: u.role.name
    };
  }
}
