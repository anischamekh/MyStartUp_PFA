import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly api = inject(ApiService);

  mine() {
    return this.api.client.get<Notification[]>(`${this.api.baseUrl}/notifications/mine`);
  }

  markRead(id: number) {
    return this.api.client.put<Notification>(`${this.api.baseUrl}/notifications/${id}/read`, {});
  }

  delete(id: number) {
    return this.api.client.delete(`${this.api.baseUrl}/notifications/${id}`);
  }
}

