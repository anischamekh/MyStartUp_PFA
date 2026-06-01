import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { Project } from '../models/project.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly api = inject(ApiService);

  all() {
    return this.api.client.get<Project[]>(`${this.api.baseUrl}/projects`);
  }

  create(project: Project) {
    return this.api.client.post<Project>(`${this.api.baseUrl}/projects`, project);
  }

  update(id: number, project: Project) {
    return this.api.client.put<Project>(`${this.api.baseUrl}/projects/${id}`, project);
  }

  delete(id: number) {
    return this.api.client.delete(`${this.api.baseUrl}/projects/${id}`);
  }
}

