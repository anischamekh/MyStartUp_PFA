import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { Payroll } from '../models/payroll.model';

@Injectable({ providedIn: 'root' })
export class PayrollService {
  private readonly api = inject(ApiService);

  list() {
    return this.api.client.get<Payroll[]>(`${this.api.baseUrl}/payroll`);
  }

  forUser(userId: number) {
    return this.api.client.get<Payroll[]>(`${this.api.baseUrl}/payroll/user/${userId}`);
  }

  get(id: number) {
    return this.api.client.get<Payroll>(`${this.api.baseUrl}/payroll/${id}`);
  }

  create(body: Payroll) {
    return this.api.client.post<Payroll>(`${this.api.baseUrl}/payroll`, body);
  }

  update(id: number, body: Payroll) {
    return this.api.client.put<Payroll>(`${this.api.baseUrl}/payroll/${id}`, body);
  }

  delete(id: number) {
    return this.api.client.delete(`${this.api.baseUrl}/payroll/${id}`);
  }
}
