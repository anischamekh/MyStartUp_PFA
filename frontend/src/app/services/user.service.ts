import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { CreateUserRequest, UpdateUserRequest, UserResponse } from '../models/user-request.model';
import type { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly api = inject(ApiService);

  all() {
    return this.api.client.get<User[]>(`${this.api.baseUrl}/users`);
  }

  /** TEAM_LEADER only — members of the team they lead. */
  teamMembers() {
    return this.api.client.get<User[]>(`${this.api.baseUrl}/users/team-members`);
  }

  me() {
    return this.api.client.get<User>(`${this.api.baseUrl}/users/me`);
  }

  create(request: CreateUserRequest) {
    return this.api.client.post<UserResponse>(`${this.api.baseUrl}/users`, request);
  }

  update(id: number, request: UpdateUserRequest) {
    return this.api.client.put<UserResponse>(`${this.api.baseUrl}/users/${id}`, request);
  }

  delete(id: number) {
    return this.api.client.delete(`${this.api.baseUrl}/users/${id}`);
  }
}
