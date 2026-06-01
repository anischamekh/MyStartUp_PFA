import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

import { ReportsService } from '../../services/reports.service';
import type { ReportsSummary } from '../../models/reports.model';
import { NotifyService } from '../../ui/notify.service';
import { apiErrorMessage } from '../../utils/api-error';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, CardModule, ButtonModule, ProgressSpinnerModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css'
})
export class ReportsComponent implements OnInit {
  private readonly api = inject(ReportsService);
  private readonly notify = inject(NotifyService);

  loading = true;
  summary: ReportsSummary | null = null;

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.api.summary().subscribe({
      next: (s) => {
        this.summary = s;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.notify.show('error', apiErrorMessage(err, 'Could not load reports.'));
      }
    });
  }

  rows(map: Record<string, number> | undefined, max: number): { key: string; count: number; pct: number }[] {
    if (!map) return [];
    const entries = Object.entries(map);
    const m = max > 0 ? max : 1;
    return entries.map(([key, count]) => ({ key, count, pct: Math.round((count / m) * 100) }));
  }

  maxVal(map: Record<string, number> | undefined): number {
    const vals = Object.values(map || {});
    if (!vals.length) return 1;
    return Math.max(1, ...vals);
  }

  downloadPdf(): void {
    this.api.downloadPdf().subscribe({
      next: (blob) => this.saveBlob(blob, 'hrm-summary.pdf'),
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'PDF export failed.'))
    });
  }

  exportCsv(): void {
    const s = this.summary;
    if (!s) {
      return;
    }
    const lines: string[] = ['section,key,count'];
    const add = (section: string, map: Record<string, number> | undefined) => {
      for (const [k, v] of Object.entries(map || {})) {
        lines.push(`${section},${this.csvCell(k)},${v}`);
      }
    };
    add('tasks', s.tasksByStatus);
    add('teams', s.employeesByTeam);
    add('leaves', s.leavesByStatus);
    const blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8' });
    this.saveBlob(blob, 'hrm-summary.csv');
    this.notify.show('success', 'CSV downloaded.');
  }

  private csvCell(v: string): string {
    if (v.includes(',') || v.includes('"') || v.includes('\n')) {
      return `"${v.replace(/"/g, '""')}"`;
    }
    return v;
  }

  downloadExcel(): void {
    this.api.downloadExcel().subscribe({
      next: (blob) => this.saveBlob(blob, 'hrm-summary.xlsx'),
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Excel export failed.'))
    });
  }

  private saveBlob(blob: Blob, name: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = name;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
