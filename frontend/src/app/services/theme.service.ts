import { Injectable, signal, effect } from '@angular/core';

const STORAGE_KEY = 'ems-theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  /** When true, dark theme is active */
  readonly dark = signal(this.readInitial());

  constructor() {
    effect(() => {
      const isDark = this.dark();
      const body = document.body;
      body.classList.remove('light-mode', 'dark-mode');
      body.classList.add(isDark ? 'dark-mode' : 'light-mode');
      localStorage.setItem(STORAGE_KEY, isDark ? 'dark' : 'light');
    });
  }

  toggle(): void {
    this.dark.update((v) => !v);
  }

  private readInitial(): boolean {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'dark') return true;
    if (stored === 'light') return false;
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false;
  }
}
