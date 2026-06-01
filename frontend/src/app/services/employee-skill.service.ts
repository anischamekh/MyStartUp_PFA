import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { EmployeeSkill } from '../models/employee-skill.model';

@Injectable({ providedIn: 'root' })
export class EmployeeSkillApiService {
  private readonly api = inject(ApiService);

  list() {
    return this.api.client.get<EmployeeSkill[]>(`${this.api.baseUrl}/employee-skills`);
  }

  forUser(userId: number) {
    return this.api.client.get<EmployeeSkill[]>(`${this.api.baseUrl}/employee-skills/user/${userId}`);
  }

  upsert(body: EmployeeSkill) {
    return this.api.client.post<EmployeeSkill>(`${this.api.baseUrl}/employee-skills`, body);
  }

  delete(id: number) {
    return this.api.client.delete(`${this.api.baseUrl}/employee-skills/${id}`);
  }
}
