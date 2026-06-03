import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, catchError, finalize, map, Observable, of, shareReplay, tap } from 'rxjs';
import { ApiService } from './api.service';
import type { RoleName } from '../models/role-name.model';

export interface SessionUser {
  userId: number;
  username: string;
  fullName: string;
  role: RoleName;
}

/** API login/refresh payload (accessToken kept in memory only, not localStorage). */
interface SessionResponseDto extends SessionUser {
  accessToken?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);

  private readonly _auth$ = new BehaviorSubject<SessionUser | null>(null);
  readonly auth$ = this._auth$.asObservable();
  private refreshInFlight: Observable<SessionUser | null> | null = null;

  /** In-memory access token for Authorization header (complements HttpOnly cookies). */
  private accessToken: string | null = null;

  get token(): string | null {
    return this.accessToken;
  }

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
    return !!this._auth$.value && !!this.accessToken;
  }

  restoreSession() {
    return this.api.client
      .post<SessionResponseDto>(`${this.api.baseUrl}/auth/refresh`, {}, { withCredentials: true })
      .pipe(
        map((dto) => this.applySessionDto(dto)),
        tap((user) => this._auth$.next(user)),
        catchError(() => {
          this.clearSession();
          return of(null);
        })
      );
  }

  login(username: string, password: string) {
    return this.api.client
      .post<SessionResponseDto>(
        `${this.api.baseUrl}/auth/login`,
        { username, password },
        { withCredentials: true }
      )
      .pipe(
        map((dto) => this.applySessionDto(dto)),
        tap((user) => this._auth$.next(user))
      );
  }

  refreshSession() {
    if (this.refreshInFlight) {
      return this.refreshInFlight;
    }
    this.refreshInFlight = this.api.client
      .post<SessionResponseDto>(`${this.api.baseUrl}/auth/refresh`, {}, { withCredentials: true })
      .pipe(
        map((dto) => this.applySessionDto(dto)),
        tap((user) => this._auth$.next(user)),
        catchError(() => {
          this.clearSession();
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
        tap(() => this.clearSession()),
        catchError(() => {
          this.clearSession();
          return of(null);
        })
      );
  }

  hasAnyRole(roles: RoleName[]) {
    return this.auth$.pipe(map((a) => !!a && roles.includes(a.role)));
  }

  private applySessionDto(dto: SessionResponseDto): SessionUser {
    this.accessToken = dto.accessToken ?? null;
    return {
      userId: dto.userId,
      username: dto.username,
      fullName: dto.fullName,
      role: dto.role
    };
  }

  private clearSession(): void {
    this.accessToken = null;
    this._auth$.next(null);
  }
}
