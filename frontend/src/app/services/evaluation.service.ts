import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { Evaluation } from '../models/evaluation.model';

@Injectable({ providedIn: 'root' })
export class EvaluationService {
  private readonly api = inject(ApiService);

  list() {
    return this.api.client.get<Evaluation[]>(`${this.api.baseUrl}/evaluations`);
  }

  create(body: Evaluation) {
    return this.api.client.post<Evaluation>(`${this.api.baseUrl}/evaluations`, body);
  }

  update(id: number, body: Evaluation) {
    return this.api.client.put<Evaluation>(`${this.api.baseUrl}/evaluations/${id}`, body);
  }

  delete(id: number) {
    return this.api.client.delete<void>(`${this.api.baseUrl}/evaluations/${id}`);
  }
}
