import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { Team } from '../models/team.model';

@Injectable({ providedIn: 'root' })
export class TeamService {
  private readonly api = inject(ApiService);

  all() {
    return this.api.client.get<Team[]>(`${this.api.baseUrl}/teams`);
  }

  create(team: Team) {
    return this.api.client.post<Team>(`${this.api.baseUrl}/teams`, team);
  }

  update(id: number, team: Team) {
    return this.api.client.put<Team>(`${this.api.baseUrl}/teams/${id}`, team);
  }

  delete(id: number) {
    return this.api.client.delete(`${this.api.baseUrl}/teams/${id}`);
  }
}

