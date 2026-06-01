import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { ReportsSummary } from '../models/reports.model';

@Injectable({ providedIn: 'root' })
export class ReportsService {
  private readonly api = inject(ApiService);

  summary() {
    return this.api.client.get<ReportsSummary>(`${this.api.baseUrl}/reports/summary`);
  }

  downloadPdf() {
    return this.api.client.get(`${this.api.baseUrl}/reports/export/summary.pdf`, { responseType: 'blob' });
  }

  downloadExcel() {
    return this.api.client.get(`${this.api.baseUrl}/reports/export/summary.xlsx`, { responseType: 'blob' });
  }
}
