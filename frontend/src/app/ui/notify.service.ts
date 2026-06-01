import { Injectable, inject } from '@angular/core';
import { MessageService } from 'primeng/api';
import { I18nService } from '../services/i18n.service';

export type NotifyLevel = 'success' | 'error' | 'info' | 'warn';

@Injectable({ providedIn: 'root' })
export class NotifyService {
  private readonly messages = inject(MessageService);
  private readonly i18n = inject(I18nService);

  show(level: NotifyLevel, message: string, summaryKey?: string): void {
    const sev =
      level === 'success' ? 'success' : level === 'error' ? 'error' : level === 'warn' ? 'warn' : 'info';
    const summary =
      summaryKey != null
        ? this.i18n.t(summaryKey)
        : level === 'error'
          ? this.i18n.t('common.error')
          : level === 'success'
            ? this.i18n.t('common.success')
            : level === 'warn'
              ? this.i18n.t('common.warning')
              : this.i18n.t('common.notice');
    this.messages.add({
      severity: sev,
      summary,
      detail: message,
      life: 3500
    });
  }

  /** Show a translated message (detail) with optional summary key. */
  showKey(level: NotifyLevel, messageKey: string, summaryKey?: string): void {
    this.show(level, this.i18n.t(messageKey), summaryKey);
  }
}
