import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { LeaveRequest } from '../models/leave-request.model';

@Injectable({ providedIn: 'root' })
export class LeaveService {
  private readonly api = inject(ApiService);

  all() {
    return this.api.client.get<LeaveRequest[]>(`${this.api.baseUrl}/leaves`);
  }

  mine() {
    return this.api.client.get<LeaveRequest[]>(`${this.api.baseUrl}/leaves/mine`);
  }

  forUser(userId: number) {
    return this.api.client.get<LeaveRequest[]>(`${this.api.baseUrl}/leaves/user/${userId}`);
  }

  requestLeave(lr: LeaveRequest) {
    return this.api.client.post<LeaveRequest>(`${this.api.baseUrl}/leaves`, lr);
  }

  approve(id: number) {
    return this.api.client.post<LeaveRequest>(`${this.api.baseUrl}/leaves/${id}/approve`, {});
  }

  reject(id: number) {
    return this.api.client.post<LeaveRequest>(`${this.api.baseUrl}/leaves/${id}/reject`, {});
  }
}

