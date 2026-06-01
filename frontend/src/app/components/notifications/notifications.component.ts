import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

import type { Notification } from '../../models/notification.model';
import { NotificationService } from '../../services/notification.service';
import { NotificationBadgeService } from '../../services/notification-badge.service';
import { NotifyService } from '../../ui/notify.service';
import { apiErrorMessage } from '../../utils/api-error';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, ButtonModule, CardModule, TagModule, DialogModule, ProgressSpinnerModule],
  templateUrl: './notifications.component.html',
  styleUrl: './notifications.component.css'
})
export class NotificationsComponent implements OnInit {
  private readonly api = inject(NotificationService);
  private readonly notify = inject(NotifyService);
  private readonly badge = inject(NotificationBadgeService);

  items: Notification[] = [];
  loading = true;

  deleteModalOpen = false;
  toDelete: Notification | null = null;

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.api.mine().subscribe({
      next: (data) => {
        this.items = data;
        this.loading = false;
        this.badge.refresh();
      },
      error: () => {
        this.loading = false;
        this.notify.show('error', 'Failed to load notifications.');
      }
    });
  }

  markRead(n: Notification): void {
    if (!n.id || n.read) return;
    this.api.markRead(n.id).subscribe({
      next: () => {
        this.reload();
        this.notify.show('success', 'Marked as read.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Could not update notification.'))
    });
  }

  openDelete(n: Notification): void {
    this.toDelete = n;
    this.deleteModalOpen = true;
  }

  closeDeleteModal(): void {
    this.deleteModalOpen = false;
    this.toDelete = null;
  }

  confirmRemove(): void {
    const n = this.toDelete;
    this.closeDeleteModal();
    if (n) this.remove(n);
  }

  private remove(n: Notification): void {
    if (!n.id) return;
    this.api.delete(n.id).subscribe({
      next: () => {
        this.reload();
        this.notify.show('success', 'Notification removed.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Could not remove notification.'))
    });
  }

  typeLabel(type: string): string {
    return type.replace(/_/g, ' ');
  }
}
