import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { CardModule } from 'primeng/card';
import { DatePickerModule } from 'primeng/datepicker';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';

import type { LeaveRequest, LeaveType } from '../../models/leave-request.model';
import type { User } from '../../models/user.model';
import { AuthService } from '../../services/auth.service';
import { LeaveService } from '../../services/leave.service';
import { UserService } from '../../services/user.service';
import { toYmd } from '../../utils/date-form';
import { NotifyService } from '../../ui/notify.service';
import { apiErrorMessage } from '../../utils/api-error';

@Component({
  selector: 'app-leaves',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    TextareaModule,
    DialogModule,
    SelectModule,
    TagModule,
    CardModule,
    DatePickerModule,
    ProgressSpinnerModule,
    IconFieldModule,
    InputIconModule
  ],
  templateUrl: './leaves.component.html',
  styleUrl: './leaves.component.css'
})
export class LeavesComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly api = inject(LeaveService);
  private readonly usersApi = inject(UserService);
  private readonly notify = inject(NotifyService);

  leaves: LeaveRequest[] = [];
  /** Personal leave rows for calendar (always own requests). */
  calendarLeaves: LeaveRequest[] = [];
  managers: User[] = [];
  allUsers: User[] = [];
  managerOptions: { label: string; value: number | null }[] = [];
  historyUserOptions: { label: string; value: number }[] = [];
  loading = true;

  isApprover = false;
  canViewAll = false;
  canViewUserHistory = false;

  /** EMPLOYEE and TEAM_LEADER may request leave (backend enforces the same). */
  get canRequestLeave(): boolean {
    const r = this.auth.role;
    return r === 'EMPLOYEE' || r === 'TEAM_LEADER';
  }

  form: LeaveRequest = { startDate: '', endDate: '', reason: '' };
  selectedLeaveType: LeaveType = 'ANNUAL';
  leaveTypeOptions: { label: string; value: LeaveType }[] = [
    { label: 'Annual', value: 'ANNUAL' },
    { label: 'Sick', value: 'SICK' },
    { label: 'Personal', value: 'PERSONAL' },
    { label: 'Unpaid', value: 'UNPAID' },
    { label: 'Other', value: 'OTHER' }
  ];
  selectedManagerId: number | null = null;
  startDateModel: Date | null = null;
  endDateModel: Date | null = null;

  historyUserId: number | null = null;

  requestModalOpen = false;
  confirmModalOpen = false;
  confirmKind: 'approve' | 'reject' | null = null;
  confirmLeave: LeaveRequest | null = null;

  calendarMonth = new Date();

  ngOnInit(): void {
    const r = this.auth.role;
    this.isApprover = r === 'MANAGER' || r === 'HR';
    this.canViewAll = r === 'MANAGER' || r === 'HR' || r === 'ADMIN';
    this.canViewUserHistory = r === 'HR' || r === 'MANAGER' || r === 'ADMIN';

    this.usersApi.all().subscribe((u) => {
      this.allUsers = u;
      this.managers = u.filter((x) => x.role?.name === 'MANAGER');
      this.managerOptions = [
        { label: 'No manager (optional)', value: null },
        ...this.managers.map((m) => ({ label: `${m.fullName} (${m.username})`, value: m.id ?? null }))
      ];
      this.historyUserOptions = u
        .filter((x) => x.id != null)
        .map((x) => ({ label: `${x.fullName} (${x.username})`, value: x.id as number }));
    });

    this.reload();
    this.api.mine().subscribe({
      next: (data) => (this.calendarLeaves = data),
      error: () => (this.calendarLeaves = [])
    });
  }

  reload(): void {
    this.loading = true;
    const req = this.canViewAll ? this.api.all() : this.api.mine();
    req.subscribe({
      next: (data) => {
        this.leaves = data.map((l) => ({
          ...l,
          employeeName: l.employee?.fullName || l.employee?.username || ''
        }));
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.notify.show('error', apiErrorMessage(err, 'Could not load leave requests.'));
      }
    });
  }

  loadHistoryForUser(): void {
    if (this.historyUserId == null) {
      this.reload();
      return;
    }
    this.loading = true;
    this.api.forUser(this.historyUserId).subscribe({
      next: (data) => {
        this.leaves = data.map((l) => ({
          ...l,
          employeeName: l.employee?.fullName || l.employee?.username || ''
        }));
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.notify.show('error', apiErrorMessage(err, 'Could not load leave history.'));
      }
    });
  }

  approvedBlocksMine(): { start: string; end: string; days?: number }[] {
    return this.calendarLeaves
      .filter((l) => l.status === 'APPROVED')
      .map((l) => ({ start: l.startDate, end: l.endDate, days: l.days }));
  }

  statusSeverity(s: string | undefined): 'success' | 'warn' | 'danger' | 'info' | 'secondary' | 'contrast' | undefined {
    switch (s) {
      case 'APPROVED':
        return 'success';
      case 'REJECTED':
        return 'danger';
      default:
        return 'warn';
    }
  }

  openNew(): void {
    this.form = { startDate: '', endDate: '', reason: '' };
    this.selectedLeaveType = 'ANNUAL';
    this.selectedManagerId = null;
    this.startDateModel = null;
    this.endDateModel = null;
    this.requestModalOpen = true;
  }

  closeRequestModal(): void {
    this.requestModalOpen = false;
  }

  submit(): void {
    const start = toYmd(this.startDateModel);
    const end = toYmd(this.endDateModel);
    if (!start || !end) {
      this.notify.show('warn', 'Please select start and end dates.');
      return;
    }
    if (!this.form.reason?.trim()) {
      this.notify.show('warn', 'Please enter a reason.');
      return;
    }
    const payload: LeaveRequest = {
      ...this.form,
      startDate: start,
      endDate: end,
      leaveType: this.selectedLeaveType
    };
    const m = this.managers.find((x) => x.id === this.selectedManagerId) ?? null;
    payload.manager = m ? ({ id: m.id } as User) : null;

    this.api.requestLeave(payload).subscribe({
      next: () => {
        this.closeRequestModal();
        this.reload();
        this.api.mine().subscribe((d) => (this.calendarLeaves = d));
        this.notify.show('success', 'Leave request submitted.');
      },
      error: (err) => {
        this.notify.show('error', apiErrorMessage(err, 'Could not submit leave request.'));
      }
    });
  }

  confirmApprove(l: LeaveRequest): void {
    this.confirmKind = 'approve';
    this.confirmLeave = l;
    this.confirmModalOpen = true;
  }

  confirmReject(l: LeaveRequest): void {
    this.confirmKind = 'reject';
    this.confirmLeave = l;
    this.confirmModalOpen = true;
  }

  closeConfirmModal(): void {
    this.confirmModalOpen = false;
    this.confirmKind = null;
    this.confirmLeave = null;
  }

  confirmAction(): void {
    const l = this.confirmLeave;
    const kind = this.confirmKind;
    this.closeConfirmModal();
    if (!l || !kind) return;
    if (kind === 'approve') this.approve(l);
    else this.reject(l);
  }

  private approve(l: LeaveRequest): void {
    if (!l.id) return;
    this.api.approve(l.id).subscribe({
      next: () => {
        this.reload();
        this.api.mine().subscribe((d) => (this.calendarLeaves = d));
        this.notify.show('success', 'Leave approved.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Could not approve leave request.'))
    });
  }

  private reject(l: LeaveRequest): void {
    if (!l.id) return;
    this.api.reject(l.id).subscribe({
      next: () => {
        this.reload();
        this.notify.show('success', 'Leave rejected.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Could not reject leave request.'))
    });
  }
}
