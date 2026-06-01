import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { Task } from '../models/task.model';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly api = inject(ApiService);

  all() {
    return this.api.client.get<Task[]>(`${this.api.baseUrl}/tasks`);
  }

  mine() {
    return this.api.client.get<Task[]>(`${this.api.baseUrl}/tasks/mine`);
  }

  create(task: Task) {
    return this.api.client.post<Task>(`${this.api.baseUrl}/tasks`, task);
  }

  update(id: number, task: Task) {
    return this.api.client.put<Task>(`${this.api.baseUrl}/tasks/${id}`, task);
  }

  updateProgress(id: number, progress: number) {
    return this.api.client.put<Task>(`${this.api.baseUrl}/tasks/${id}/progress`, { progress });
  }

  validate(id: number) {
    return this.api.client.put<Task>(`${this.api.baseUrl}/tasks/${id}/validate`, {});
  }

  delete(id: number) {
    return this.api.client.delete(`${this.api.baseUrl}/tasks/${id}`);
  }
}

