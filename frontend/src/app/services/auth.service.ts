import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, Observable, finalize, map, shareReplay, tap } from 'rxjs';
import { ApiService } from './api.service';
import type { RoleName } from '../models/role-name.model';

export interface LoginResponse {
  token: string;
  refreshToken?: string;
  userId: number;
  username: string;
  fullName: string;
  role: RoleName;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);

  private readonly _auth$ = new BehaviorSubject<LoginResponse | null>(this.readFromStorage());
  readonly auth$ = this._auth$.asObservable();
  private refreshInFlight: Observable<LoginResponse> | null = null;

  get token(): string | null {
    return this._auth$.value?.token ?? null;
  }

  get role(): RoleName | null {
    return this._auth$.value?.role ?? null;
  }

  get userId(): number | null {
    return this._auth$.value?.userId ?? null;
  }

  /** ADMIN may only browse; all mutations are blocked in API and should be hidden in UI. */
  viewOnlyAdmin(): boolean {
    return this.role === 'ADMIN';
  }

  get isLoggedIn(): boolean {
    return !!this._auth$.value?.token;
  }

  login(username: string, password: string) {
    return this.api.client
      .post<LoginResponse>(`${this.api.baseUrl}/auth/login`, { username, password }, { withCredentials: true })
      .pipe(
        tap((resp) => {
          localStorage.setItem('auth', JSON.stringify(resp));
          this._auth$.next(resp);
        })
      );
  }

  refreshSession() {
    if (this.refreshInFlight) {
      return this.refreshInFlight;
    }
    const refreshToken = this._auth$.value?.refreshToken;
    this.refreshInFlight = this.api.client
      .post<LoginResponse>(
        `${this.api.baseUrl}/auth/refresh`,
        refreshToken ? { refreshToken } : {},
        { withCredentials: true }
      )
      .pipe(
        tap((resp) => {
          localStorage.setItem('auth', JSON.stringify(resp));
          this._auth$.next(resp);
        }),
        finalize(() => {
          this.refreshInFlight = null;
        }),
        shareReplay(1)
      );
    return this.refreshInFlight;
  }

  logout() {
    localStorage.removeItem('auth');
    this._auth$.next(null);
  }

  hasAnyRole(roles: RoleName[]) {
    return this.auth$.pipe(map((a) => !!a && roles.includes(a.role)));
  }

  private readFromStorage(): LoginResponse | null {
    const raw = localStorage.getItem('auth');
    if (!raw) return null;
    try {
      return JSON.parse(raw) as LoginResponse;
    } catch {
      return null;
    }
  }
}

