import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { Skill } from '../models/skill.model';

@Injectable({ providedIn: 'root' })
export class SkillApiService {
  private readonly api = inject(ApiService);

  all() {
    return this.api.client.get<Skill[]>(`${this.api.baseUrl}/skills`);
  }

  create(body: Skill) {
    return this.api.client.post<Skill>(`${this.api.baseUrl}/skills`, body);
  }

  update(id: number, body: Skill) {
    return this.api.client.put<Skill>(`${this.api.baseUrl}/skills/${id}`, body);
  }

  delete(id: number) {
    return this.api.client.delete(`${this.api.baseUrl}/skills/${id}`);
  }
}
