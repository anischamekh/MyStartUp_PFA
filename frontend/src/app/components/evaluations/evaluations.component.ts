import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { CardModule } from 'primeng/card';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { TextareaModule } from 'primeng/textarea';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

import { AuthService } from '../../services/auth.service';
import { EvaluationService } from '../../services/evaluation.service';
import { UserService } from '../../services/user.service';
import type { Evaluation } from '../../models/evaluation.model';
import type { User } from '../../models/user.model';
import { NotifyService } from '../../ui/notify.service';
import { apiErrorMessage } from '../../utils/api-error';
import { toYmd } from '../../utils/date-form';

@Component({
  selector: 'app-evaluations',
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
    TextareaModule,
    ProgressSpinnerModule
  ],
  templateUrl: './evaluations.component.html',
  styleUrl: './evaluations.component.css'
})
export class EvaluationsComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly api = inject(EvaluationService);
  private readonly usersApi = inject(UserService);
  private readonly notify = inject(NotifyService);

  rows: Evaluation[] = [];
  userOptions: { label: string; value: number }[] = [];
  loading = true;

  dialogOpen = false;
  editingId: number | null = null;
  form: Evaluation = {
    score: 0,
    technicalSkill: null,
    teamwork: null,
    deadlineRespect: null,
    comment: '',
    date: toYmd(new Date())
  };
  formEmployeeId: number | null = null;

  ngOnInit(): void {
    const users$ =
      this.auth.role === 'TEAM_LEADER' ? this.usersApi.teamMembers() : this.usersApi.all();
    users$.subscribe({
      next: (u) => {
        this.userOptions = u
          .filter((x) => x.id != null)
          .map((x) => ({ label: `${x.fullName} (${x.username})`, value: x.id as number }));
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Could not load users.'))
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
        this.notify.show('error', apiErrorMessage(err, 'Could not load evaluations.'));
      }
    });
  }

  canMutate(ev: Evaluation): boolean {
    if (this.auth.viewOnlyAdmin()) {
      return false;
    }
    const r = this.auth.role;
    if (r === 'HR' || r === 'MANAGER') {
      return true;
    }
    if (r === 'TEAM_LEADER') {
      const uid = this.auth.userId;
      return uid != null && ev.evaluator?.id === uid;
    }
    return false;
  }

  openNew(): void {
    this.editingId = null;
    this.form = {
      score: 0,
      technicalSkill: null,
      teamwork: null,
      deadlineRespect: null,
      comment: '',
      date: toYmd(new Date())
    };
    this.formEmployeeId = null;
    this.dialogOpen = true;
  }

  edit(ev: Evaluation): void {
    this.editingId = ev.id ?? null;
    this.form = {
      score: ev.score ?? 0,
      technicalSkill: ev.technicalSkill ?? null,
      teamwork: ev.teamwork ?? null,
      deadlineRespect: ev.deadlineRespect ?? null,
      comment: ev.comment ?? '',
      date: (ev.date as string) ?? toYmd(new Date())
    };
    this.formEmployeeId = ev.employee?.id ?? null;
    this.dialogOpen = true;
  }

  save(): void {
    if (this.editingId == null && !this.formEmployeeId) {
      this.notify.show('warn', 'Select an employee.');
      return;
    }
    if (this.form.score < 0 || this.form.score > 100) {
      this.notify.show('warn', 'Score must be between 0 and 100.');
      return;
    }
    const body: Evaluation = {
      score: this.form.score,
      technicalSkill: this.form.technicalSkill ?? undefined,
      teamwork: this.form.teamwork ?? undefined,
      deadlineRespect: this.form.deadlineRespect ?? undefined,
      comment: this.form.comment,
      date: this.form.date
    };
    if (this.editingId == null) {
      body.employee = { id: this.formEmployeeId as number } as User;
    }
    const req =
      this.editingId != null ? this.api.update(this.editingId, body) : this.api.create(body);
    req.subscribe({
      next: () => {
        this.dialogOpen = false;
        this.reload();
        this.notify.show('success', 'Evaluation saved.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Save failed.'))
    });
  }

  remove(ev: Evaluation): void {
    if (!ev.id) {
      return;
    }
    this.api.delete(ev.id).subscribe({
      next: () => {
        this.reload();
        this.notify.show('success', 'Deleted.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Delete failed.'))
    });
  }
}
