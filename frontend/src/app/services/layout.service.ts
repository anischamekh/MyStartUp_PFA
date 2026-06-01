import { Injectable, signal, computed } from '@angular/core';

const SIDEBAR_EXPANDED_KEY = 'ems-sidebar-expanded';

@Injectable({ providedIn: 'root' })
export class LayoutService {
  /** Sidebar expanded (icons + labels). When false, icon-only rail. */
  readonly sidebarExpanded = signal(this.readSidebarExpanded());

  /** Mobile drawer open */
  readonly mobileDrawerOpen = signal(false);

  readonly isMobile = signal(false);

  readonly sidebarWidthClass = computed(() =>
    this.sidebarExpanded() ? 'layout-sidebar--expanded' : 'layout-sidebar--collapsed'
  );

  setMobile(isMobile: boolean): void {
    this.isMobile.set(isMobile);
    if (isMobile) {
      this.mobileDrawerOpen.set(false);
    }
  }

  toggleSidebar(): void {
    if (this.isMobile()) {
      this.mobileDrawerOpen.update((v) => !v);
    } else {
      this.sidebarExpanded.update((v) => {
        const next = !v;
        localStorage.setItem(SIDEBAR_EXPANDED_KEY, next ? 'true' : 'false');
        return next;
      });
    }
  }

  private readSidebarExpanded(): boolean {
    const v = localStorage.getItem(SIDEBAR_EXPANDED_KEY);
    if (v === 'false') return false;
    return true;
  }

  closeMobileDrawer(): void {
    this.mobileDrawerOpen.set(false);
  }
}
