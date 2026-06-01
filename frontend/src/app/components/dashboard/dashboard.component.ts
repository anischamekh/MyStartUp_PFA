import { Component, OnInit, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ChartModule } from 'primeng/chart';

import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { LeaveService } from '../../services/leave.service';
import { ReportsService } from '../../services/reports.service';
import { ThemeService } from '../../services/theme.service';
import type { Task } from '../../models/task.model';
import type { LeaveRequest } from '../../models/leave-request.model';
import type { ReportsSummary } from '../../models/reports.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ButtonModule, CardModule, ChartModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly taskApi = inject(TaskService);
  private readonly leaveApi = inject(LeaveService);
  private readonly reportsApi = inject(ReportsService);
  private readonly theme = inject(ThemeService);

  loading = true;
  loadError: string | null = null;

  totalTasks = 0;
  completedTasks = 0;
  pendingTasks = 0;
  pendingLeaves = 0;

  tasks: Task[] = [];
  leaves: LeaveRequest[] = [];
  reportSummary: ReportsSummary | null = null;

  /** Rows for CSS bar chart (task counts by status) — non-manager roles. */
  taskChartRows: { key: string; label: string; count: number; pct: number; tone: string }[] = [];

  mgmtTasksDoughnut: { labels: string[]; datasets: { data: number[]; backgroundColor: string[] }[] } = {
    labels: [],
    datasets: [{ data: [], backgroundColor: [] }]
  };

  mgmtLeavesBar: { labels: string[]; datasets: { label: string; data: number[]; backgroundColor: string }[] } = {
    labels: [],
    datasets: [{ label: 'Requests', data: [], backgroundColor: '#3b82f6' }]
  };

  mgmtEmployeesBar: { labels: string[]; datasets: { label: string; data: number[]; backgroundColor: string }[] } = {
    labels: [],
    datasets: [{ label: 'Employees', data: [], backgroundColor: '#8b5cf6' }]
  };

  chartOpts: Record<string, unknown> = {};

  barOpts: Record<string, unknown> = {};

  constructor() {
    effect(() => {
      this.theme.dark();
      this.syncChartTheme();
    });
  }

  ngOnInit(): void {
    this.syncChartTheme();
    this.reloadAll();
  }

  private syncChartTheme(): void {
    const dark = this.theme.dark();
    const tick = dark ? '#cbd5e1' : '#64748b';
    const grid = dark ? 'rgba(148,163,184,0.14)' : 'rgba(148,163,184,0.22)';
    this.chartOpts = {
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom' as const,
          labels: { color: tick, boxWidth: 12 }
        }
      }
    };
    this.barOpts = {
      ...this.chartOpts,
      scales: {
        x: { ticks: { color: tick }, grid: { color: grid } },
        y: {
          ticks: { color: tick, stepSize: 1 },
          grid: { color: grid },
          beginAtZero: true
        }
      }
    };
  }

  reloadAll(): void {
    this.loading = true;
    this.loadError = null;

    const role = this.auth.role;
    const tasks$ =
      role === 'EMPLOYEE'
        ? this.taskApi.mine()
        : this.taskApi.all().pipe(catchError(() => of<Task[]>([])));

    const leaves$ =
      role === 'MANAGER' || role === 'ADMIN'
        ? this.leaveApi.all().pipe(catchError(() => of<LeaveRequest[]>([])))
        : this.leaveApi.mine().pipe(catchError(() => of<LeaveRequest[]>([])));

    const summary$ =
      role === 'MANAGER' || role === 'ADMIN'
        ? this.reportsApi.summary().pipe(catchError(() => of<ReportsSummary | null>(null)))
        : of<ReportsSummary | null>(null);

    forkJoin({ tasks: tasks$, leaves: leaves$, summary: summary$ }).subscribe({
      next: ({ tasks, leaves, summary }) => {
        this.tasks = tasks;
        this.leaves = leaves;
        this.reportSummary = summary;
        this.computeStats(tasks, leaves);
        this.updateTaskChart(tasks);
        this.applyMgmtSummary(summary);
        this.syncChartTheme();
        this.loading = false;
      },
      error: () => {
        this.loadError = 'Could not load dashboard data.';
        this.loading = false;
      }
    });
  }

  private computeStats(tasks: Task[], leaves: LeaveRequest[]): void {
    this.totalTasks = tasks.length;
    this.completedTasks = tasks.filter((t) => t.status === 'DONE' || t.status === 'VALIDATED').length;
    this.pendingTasks = tasks.filter((t) => t.status === 'TODO' || t.status === 'IN_PROGRESS').length;
    this.pendingLeaves = leaves.filter((l) => l.status === 'PENDING').length;
  }

  private updateTaskChart(tasks: Task[]): void {
    if (!tasks.length) {
      this.taskChartRows = [];
      return;
    }
    const tallies: Record<string, number> = { TODO: 0, IN_PROGRESS: 0, DONE: 0, VALIDATED: 0 };
    for (const t of tasks) {
      const s = t.status ?? 'TODO';
      if (s in tallies) {
        tallies[s]++;
      }
    }
    const total = tasks.length;
    const order: { key: string; label: string; tone: string }[] = [
      { key: 'TODO', label: 'To do', tone: '1' },
      { key: 'IN_PROGRESS', label: 'In progress', tone: '2' },
      { key: 'DONE', label: 'Done', tone: '3' },
      { key: 'VALIDATED', label: 'Validated', tone: '4' }
    ];
    this.taskChartRows = order.map((o) => ({
      ...o,
      count: tallies[o.key],
      pct: (tallies[o.key] / total) * 100
    }));
  }

  private applyMgmtSummary(summary: ReportsSummary | null): void {
    if (!summary) {
      this.mgmtTasksDoughnut = { labels: [], datasets: [{ data: [], backgroundColor: [] }] };
      this.mgmtLeavesBar = { labels: [], datasets: [{ label: 'Requests', data: [], backgroundColor: '#3b82f6' }] };
      this.mgmtEmployeesBar = { labels: [], datasets: [{ label: 'Employees', data: [], backgroundColor: '#8b5cf6' }] };
      return;
    }
    const tbs = summary.tasksByStatus ?? {};
    const todo = this.countMap(tbs, 'TODO');
    const inProgress = this.countMap(tbs, 'IN_PROGRESS');
    const done = this.countMap(tbs, 'DONE') + this.countMap(tbs, 'VALIDATED');
    this.mgmtTasksDoughnut = {
      labels: ['To do', 'In progress', 'Done'],
      datasets: [
        {
          data: [todo, inProgress, done],
          backgroundColor: ['#94a3b8', '#f59e0b', '#22c55e']
        }
      ]
    };

    const lbs = summary.leavesByStatus ?? {};
    this.mgmtLeavesBar = {
      labels: ['Pending', 'Approved', 'Rejected'],
      datasets: [
        {
          label: 'Leave requests',
          data: [
            this.countMap(lbs, 'PENDING'),
            this.countMap(lbs, 'APPROVED'),
            this.countMap(lbs, 'REJECTED')
          ],
          backgroundColor: '#3b82f6'
        }
      ]
    };

    const ebt = summary.employeesByTeam ?? {};
    const teamLabels = Object.keys(ebt);
    this.mgmtEmployeesBar = {
      labels: teamLabels,
      datasets: [
        {
          label: 'Employees',
          data: teamLabels.map((k) => this.countMap(ebt, k)),
          backgroundColor: '#8b5cf6'
        }
      ]
    };
  }

  private countMap(m: Record<string, number>, k: string): number {
    const v = m[k];
    return typeof v === 'number' ? v : 0;
  }

  role() {
    return this.auth.role;
  }
}
