import { Injectable, inject, signal } from '@angular/core';
import { NotificationService } from './notification.service';

@Injectable({ providedIn: 'root' })
export class NotificationBadgeService {
  private readonly api = inject(NotificationService);

  readonly unreadCount = signal(0);

  refresh(): void {
    this.api.mine().subscribe({
      next: (items) => {
        const n = items.filter((i) => !i.read).length;
        this.unreadCount.set(n);
      },
      error: () => this.unreadCount.set(0)
    });
  }
}
