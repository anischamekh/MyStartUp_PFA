import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { CardModule } from 'primeng/card';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

import { AuthService } from '../../services/auth.service';
import { PayrollService } from '../../services/payroll.service';
import { UserService } from '../../services/user.service';
import type { Payroll } from '../../models/payroll.model';
import type { User } from '../../models/user.model';
import { NotifyService } from '../../ui/notify.service';
import { apiErrorMessage } from '../../utils/api-error';

@Component({
  selector: 'app-payroll',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    CardModule,
    InputNumberModule,
    SelectModule,
    ProgressSpinnerModule
  ],
  templateUrl: './payroll.component.html',
  styleUrl: './payroll.component.css'
})
export class PayrollComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly api = inject(PayrollService);
  private readonly usersApi = inject(UserService);
  private readonly notify = inject(NotifyService);

  rows: Payroll[] = [];
  users: User[] = [];
  userOptions: { label: string; value: number }[] = [];
  loading = true;
  canManage = false;

  dialogOpen = false;
  editingId: number | null = null;
  form: Payroll = { baseSalary: 0, bonus: 0, deductions: 0 };
  formUserId: number | null = null;

  payslipOpen = false;
  payslip: Payroll | null = null;

  ngOnInit(): void {
    this.canManage = this.auth.role === 'HR';
    this.usersApi.all().subscribe((u) => {
      this.users = u;
      this.userOptions = u.filter((x) => x.id != null).map((x) => ({ label: `${x.fullName}`, value: x.id as number }));
    });
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.api.list().subscribe({
      next: (r) => {
        this.rows = r;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.notify.show('error', apiErrorMessage(err, 'Could not load payroll.'));
      }
    });
  }

  openNew(): void {
    this.editingId = null;
    this.form = { baseSalary: 0, bonus: 0, deductions: 0 };
    this.formUserId = null;
    this.dialogOpen = true;
  }

  edit(p: Payroll): void {
    this.editingId = p.id ?? null;
    this.form = {
      baseSalary: p.baseSalary ?? 0,
      bonus: p.bonus ?? 0,
      deductions: p.deductions ?? 0
    };
    this.formUserId = p.user?.id ?? null;
    this.dialogOpen = true;
  }

  save(): void {
    if (!this.formUserId) {
      this.notify.show('warn', 'Select an employee.');
      return;
    }
    const body: Payroll = {
      ...this.form,
      user: { id: this.formUserId } as User
    };
    const req =
      this.editingId != null ? this.api.update(this.editingId, body) : this.api.create(body);
    req.subscribe({
      next: () => {
        this.dialogOpen = false;
        this.reload();
        this.notify.show('success', 'Payroll saved.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Save failed.'))
    });
  }

  openPayslip(p: Payroll): void {
    this.payslip = p;
    this.payslipOpen = true;
  }

  printPayslip(): void {
    const el = document.getElementById('payslip-print');
    if (!el) {
      return;
    }
    const w = window.open('', '_blank', 'width=640,height=520');
    if (!w) {
      this.notify.show('warn', 'Allow pop-ups to print the payslip.');
      return;
    }
    const styles =
      'body{font-family:system-ui,Segoe UI,sans-serif;padding:1.25rem;color:#111;}table{width:100%;border-collapse:collapse}th{text-align:left;padding:0.35rem 0}td{text-align:right;padding:0.35rem 0;border-bottom:1px solid #e5e7eb}.total th,.total td{font-weight:700;border:none;padding-top:0.75rem}h2{margin:0 0 0.25rem}p{margin:0 0 1rem;color:#64748b}';
    w.document.write(
      `<!DOCTYPE html><html><head><title>Payslip</title><style>${styles}</style></head><body>${el.innerHTML}</body></html>`
    );
    w.document.close();
    w.focus();
    w.print();
    w.close();
  }

  remove(p: Payroll): void {
    if (!p.id) return;
    this.api.delete(p.id).subscribe({
      next: () => {
        this.reload();
        this.notify.show('success', 'Deleted.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Delete failed.'))
    });
  }
}
