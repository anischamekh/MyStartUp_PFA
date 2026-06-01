import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { ThemeService } from './services/theme.service';
import { I18nService } from './services/i18n.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  /** Initialize theme (body classes + PrimeNG dark palette) before any route. */
  private readonly _theme = inject(ThemeService);
  private readonly _i18n = inject(I18nService);
}
