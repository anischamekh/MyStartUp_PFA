import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';

import { LeaveService } from '../../services/leave.service';
import { TeamService } from '../../services/team.service';
import type { LeaveRequest } from '../../models/leave-request.model';
import type { Team } from '../../models/team.model';
import type { User } from '../../models/user.model';
import { NotifyService } from '../../ui/notify.service';
import { apiErrorMessage } from '../../utils/api-error';

interface CalendarCell {
  iso: string;
  inMonth: boolean;
  dayNum: number | null;
  leaves: LeaveRequest[];
}

@Component({
  selector: 'app-leave-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, CardModule, ProgressSpinnerModule, SelectModule],
  templateUrl: './leave-calendar.component.html',
  styleUrl: './leave-calendar.component.css'
})
export class LeaveCalendarComponent implements OnInit {
  private readonly leaveApi = inject(LeaveService);
  private readonly teamApi = inject(TeamService);
  private readonly notify = inject(NotifyService);

  loading = true;
  /** First day of the displayed month (local). */
  monthStart = this.startOfMonth(new Date());
  leaves: LeaveRequest[] = [];
  teams: Team[] = [];
  teamFilterId: number | null = null;
  approvedOnly = false;

  readonly weekLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  get teamOptions(): { label: string; value: number }[] {
    return this.teams
      .filter((t) => t.id != null)
      .map((t) => ({ label: t.name, value: t.id as number }));
  }

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading = true;
    forkJoin({
      leaves: this.leaveApi.all(),
      teams: this.teamApi.all().pipe(catchError(() => of<Team[]>([])))
    }).subscribe({
      next: ({ leaves, teams }) => {
        this.leaves = leaves;
        this.teams = teams;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.notify.show('error', apiErrorMessage(err, 'Could not load leave requests.'));
      }
    });
  }

  monthTitle(): string {
    return this.monthStart.toLocaleString(undefined, { month: 'long', year: 'numeric' });
  }

  prevMonth(): void {
    const y = this.monthStart.getFullYear();
    const m = this.monthStart.getMonth();
    this.monthStart = new Date(y, m - 1, 1);
  }

  nextMonth(): void {
    const y = this.monthStart.getFullYear();
    const m = this.monthStart.getMonth();
    this.monthStart = new Date(y, m + 1, 1);
  }

  cellRows(): CalendarCell[][] {
    const y = this.monthStart.getFullYear();
    const m = this.monthStart.getMonth();
    const first = new Date(y, m, 1);
    const last = new Date(y, m + 1, 0);
    const leading = (first.getDay() + 6) % 7;
    const cells: CalendarCell[] = [];

    for (let i = 0; i < leading; i++) {
      const d = new Date(y, m, 1 - (leading - i));
      cells.push({
        iso: this.toIso(d),
        inMonth: false,
        dayNum: null,
        leaves: []
      });
    }

    for (let day = 1; day <= last.getDate(); day++) {
      const d = new Date(y, m, day);
      const iso = this.toIso(d);
      cells.push({
        iso,
        inMonth: true,
        dayNum: day,
        leaves: this.leavesForDay(iso)
      });
    }

    while (cells.length % 7 !== 0) {
      const lastCell = cells[cells.length - 1];
      const next = this.addDays(this.parseIso(lastCell.iso)!, 1);
      cells.push({
        iso: this.toIso(next),
        inMonth: false,
        dayNum: null,
        leaves: []
      });
    }

    const rows: CalendarCell[][] = [];
    for (let i = 0; i < cells.length; i += 7) {
      rows.push(cells.slice(i, i + 7));
    }
    return rows;
  }

  private visibleLeaves(): LeaveRequest[] {
    let list = [...this.leaves];
    if (this.approvedOnly) {
      list = list.filter((l) => l.status === 'APPROVED');
    }
    if (this.teamFilterId != null) {
      list = list.filter((l) => {
        const u = l.employee as User | undefined;
        return u?.teamId === this.teamFilterId;
      });
    }
    return list;
  }

  private leavesForDay(iso: string): LeaveRequest[] {
    return this.visibleLeaves().filter((l) => this.leaveCovers(l, iso));
  }

  private leaveCovers(l: LeaveRequest, iso: string): boolean {
    const a = (l.startDate ?? '').split('T')[0];
    const b = (l.endDate ?? '').split('T')[0];
    if (!a || !b) {
      return false;
    }
    return a <= iso && iso <= b;
  }

  private startOfMonth(d: Date): Date {
    return new Date(d.getFullYear(), d.getMonth(), 1);
  }

  private toIso(d: Date): string {
    const y = d.getFullYear();
    const mo = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${mo}-${day}`;
  }


private parseIso(s: string): Date | null {
  const head = s.split('T')[0];
  if (!/^\d{4}-\d{2}-\d{2}$/.test(head)) {
    return null;
  }
  const [y, m, d] = head.split('-').map(Number);
  return new Date(y, m - 1, d);
}

private addDays(d: Date, n: number): Date {
  const x = new Date(d.getTime());
  x.setDate(x.getDate() + n);
  return x;
}
}