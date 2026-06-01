import { Injectable, signal, computed, effect } from '@angular/core';
import { EN_TRANSLATIONS } from '../i18n/translations.en';
import { FR_TRANSLATIONS } from '../i18n/translations.fr';

export type AppLanguage = 'en' | 'fr';

const STORAGE_KEY = 'ems-lang';

@Injectable({ providedIn: 'root' })
export class I18nService {
  readonly lang = signal<AppLanguage>(this.readInitial());

  readonly isFrench = computed(() => this.lang() === 'fr');

  constructor() {
    effect(() => {
      document.documentElement.lang = this.lang();
    });
  }

  t(key: string, params?: Record<string, string | number>): string {
    const map = this.lang() === 'fr' ? FR_TRANSLATIONS : EN_TRANSLATIONS;
    let text = map[key] ?? EN_TRANSLATIONS[key] ?? key;
    if (params) {
      for (const [k, v] of Object.entries(params)) {
        text = text.replaceAll(`{{${k}}}`, String(v));
      }
    }
    return text;
  }

  toggle(): void {
    this.lang.update((l) => (l === 'en' ? 'fr' : 'en'));
    localStorage.setItem(STORAGE_KEY, this.lang());
  }

  setLanguage(lang: AppLanguage): void {
    this.lang.set(lang);
    localStorage.setItem(STORAGE_KEY, lang);
  }

  private readInitial(): AppLanguage {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'fr' || stored === 'en') {
      return stored;
    }
    const browser = navigator.language?.toLowerCase() ?? '';
    return browser.startsWith('fr') ? 'fr' : 'en';
  }
}
