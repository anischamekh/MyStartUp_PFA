import { Component, OnInit, OnDestroy, inject, HostListener, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet, NavigationEnd, ActivatedRouteSnapshot } from '@angular/router';
import { filter, Subscription } from 'rxjs';

import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';

import { AuthService } from '../../services/auth.service';
import { LayoutService } from '../../services/layout.service';
import { ThemeService } from '../../services/theme.service';
import { NotificationBadgeService } from '../../services/notification-badge.service';
import type { RoleName } from '../../models/role-name.model';
import { ChatbotWidgetComponent } from '../chatbot-widget/chatbot-widget.component';
import { I18nService } from '../../services/i18n.service';
import { TranslatePipe } from '../../pipes/translate.pipe';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    ButtonModule,
    ToastModule,
    ChatbotWidgetComponent,
    TranslatePipe
  ],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  readonly auth = inject(AuthService);
  readonly layout = inject(LayoutService);
  readonly theme = inject(ThemeService);
  readonly i18n = inject(I18nService);
  readonly badge = inject(NotificationBadgeService);
  private readonly router = inject(Router);

  pageTitle = '';
  private sub?: Subscription;
  private titleKey = 'page.dashboard';

  constructor() {
    effect(() => {
      this.i18n.lang();
      this.pageTitle = this.i18n.t(this.titleKey);
    });
  }

  ngOnInit(): void {
    this.layout.setMobile(this.checkMobile());
    this.updateTitle();
    this.badge.refresh();

    this.sub = this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => {
      this.updateTitle();
      this.badge.refresh();
      this.layout.closeMobileDrawer();
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  @HostListener('window:resize')
  onResize(): void {
    this.layout.setMobile(this.checkMobile());
  }

  private checkMobile(): boolean {
    return window.innerWidth < 960;
  }

  private updateTitle(): void {
    const snap = this.router.routerState.snapshot.root;
    const deepest = this.getDeepestChild(snap);
    this.titleKey = (deepest.data['titleKey'] as string) ?? 'page.dashboard';
    this.pageTitle = this.i18n.t(this.titleKey);
  }

  private getDeepestChild(route: ActivatedRouteSnapshot): ActivatedRouteSnapshot {
    let r = route;
    while (r.firstChild) {
      r = r.firstChild;
    }
    return r;
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }

  toggleSidebar(): void {
    this.layout.toggleSidebar();
  }

  showNavLink(roles: RoleName[] | null): boolean {
    if (!roles || roles.length === 0) return true;
    const role = this.auth.role;
    return !!role && roles.includes(role);
  }

  goNotifications(): void {
    void this.router.navigate(['/notifications']);
  }
}
