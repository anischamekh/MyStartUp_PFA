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
import { SliderModule } from 'primeng/slider';
import { ProgressBarModule } from 'primeng/progressbar';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputNumberModule } from 'primeng/inputnumber';

import type { Task, TaskPriority, TaskStatus } from '../../models/task.model';
import type { User } from '../../models/user.model';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { UserService } from '../../services/user.service';
import { parseYmd, toYmd } from '../../utils/date-form';
import { NotifyService } from '../../ui/notify.service';
import { apiErrorMessage } from '../../utils/api-error';

@Component({
  selector: 'app-tasks',
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
    SliderModule,
    ProgressBarModule,
    ProgressSpinnerModule,
    IconFieldModule,
    InputIconModule,
    InputNumberModule
  ],
  templateUrl: './tasks.component.html',
  styleUrl: './tasks.component.css'
})
export class TasksComponent implements OnInit {
  readonly parseYmd = parseYmd;
  readonly toYmd = toYmd;
  readonly auth = inject(AuthService);
  private readonly api = inject(TaskService);
  private readonly usersApi = inject(UserService);
  private readonly notify = inject(NotifyService);

  tasks: Task[] = [];
  users: User[] = [];
  loading = true;
  isLeader = false;

  editingId: number | null = null;
  editingProgressId: number | null = null;
  progressValue = 0;

  selectedUserId: number | null = null;
  priorities: { label: string; value: TaskPriority }[] = [
    { label: 'Low', value: 'LOW' },
    { label: 'Medium', value: 'MEDIUM' },
    { label: 'High', value: 'HIGH' }
  ];
  selectedPriority: TaskPriority = 'MEDIUM';

  assigneeOptions: { label: string; value: number | null }[] = [];

  form: Task = { title: '', description: '', priority: 'MEDIUM', progress: 0, estimatedHours: null };
  dueDateModel: Date | null = null;

  editModalOpen = false;
  progressModalOpen = false;
  progressEditTask: Task | null = null;
  deleteModalOpen = false;
  toDelete: Task | null = null;

  ngOnInit(): void {
    this.isLeader = this.auth.role === 'TEAM_LEADER';
    if (this.auth.role === 'ADMIN') {
      this.usersApi.all().subscribe((u) => {
        this.users = u;
        this.assigneeOptions = u.map((x) => ({ label: `${x.fullName} (${x.username})`, value: x.id ?? null }));
      });
    } else if (this.isLeader) {
      this.usersApi.teamMembers().subscribe({
        next: (u) => {
          this.users = u;
          this.assigneeOptions = [
            { label: 'Unassigned', value: null },
            ...u.map((x) => ({ label: `${x.fullName} (${x.username})`, value: x.id ?? null }))
          ];
        },
        error: () => {
          this.usersApi.all().subscribe((u) => {
            this.users = u;
            this.assigneeOptions = [
              { label: 'Unassigned', value: null },
              ...u.map((x) => ({ label: `${x.fullName} (${x.username})`, value: x.id ?? null }))
            ];
          });
        }
      });
    } else {
      this.usersApi.all().subscribe((u) => {
        this.users = u;
        this.assigneeOptions = [
          { label: 'Unassigned', value: null },
          ...u.map((x) => ({ label: `${x.fullName} (${x.username})`, value: x.id ?? null }))
        ];
      });
    }
    this.reload();
  }

  reload(): void {
    this.loading = true;
    const role = this.auth.role;
    const req =
      role === 'ADMIN' || this.isLeader ? this.api.all() : this.api.mine();
    req.subscribe({
      next: (data) => {
        this.tasks = data.map((t) => ({
          ...t,
          assigneeName: t.assignedTo?.fullName || t.assignedTo?.username || '',
          projectTitle: t.project?.name ?? '',
          creatorName: t.createdBy?.fullName || t.createdBy?.username || ''
        }));
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.notify.show('error', apiErrorMessage(err, 'Could not load tasks.'));
      }
    });
  }

  statusSeverity(status: TaskStatus | undefined): 'success' | 'warn' | 'info' | 'danger' | 'secondary' | 'contrast' | undefined {
    switch (status) {
      case 'DONE':
      case 'VALIDATED':
        return 'success';
      case 'IN_PROGRESS':
        return 'warn';
      case 'TODO':
      default:
        return 'info';
    }
  }

  prioritySeverity(p: TaskPriority | undefined): 'success' | 'warn' | 'danger' | 'secondary' | 'info' | 'contrast' | undefined {
    switch (p) {
      case 'HIGH':
        return 'danger';
      case 'MEDIUM':
        return 'warn';
      default:
        return 'secondary';
    }
  }

  openNew(): void {
    this.editingId = null;
    this.form = { title: '', description: '', priority: 'MEDIUM', progress: 0, estimatedHours: null };
    this.selectedPriority = 'MEDIUM';
    this.selectedUserId = null;
    this.dueDateModel = null;
    this.editModalOpen = true;
  }

  edit(t: Task): void {
    if (this.isTaskLocked(t)) {
      this.notify.show('warn', 'This task is fully validated and cannot be edited.');
      return;
    }
    this.editingId = t.id ?? null;
    this.form = { ...t };
    this.selectedPriority = (t.priority ?? 'MEDIUM') as TaskPriority;
    this.selectedUserId = (t.assignedTo?.id ?? null) as number | null;
    this.dueDateModel = parseYmd(t.dueDate ?? undefined);
    this.editModalOpen = true;
  }

  submitEdit(): void {
    if (!this.form.title?.trim()) {
      this.notify.show('warn', 'Title is required.');
      return;
    }
    this.form.priority = this.selectedPriority;
    this.form.dueDate = toYmd(this.dueDateModel) || null;
    const assigned = this.users.find((u) => u.id === this.selectedUserId) ?? null;
    this.form.assignedTo = assigned ? ({ id: assigned.id } as User) : null;

    const { assigneeName: _assigneeName, ...taskPayload } = this.form;
    const req = this.editingId
      ? this.api.update(this.editingId, taskPayload)
      : this.api.create(taskPayload);
    req.subscribe({
      next: () => {
        this.closeEditModal();
        this.reload();
        this.notify.show('success', 'Task saved.');
      },
      error: (err) => {
        this.notify.show('error', apiErrorMessage(err, 'Could not save task.'));
      }
    });
  }

  openProgress(t: Task): void {
    this.progressEditTask = t;
    this.editingProgressId = t.id ?? null;
    const floor = t.validatedProgressFloor ?? 0;
    this.progressValue = Math.max(t.progress ?? 0, floor);
    this.progressModalOpen = true;
  }

  progressSliderMin(): number {
    const t = this.progressEditTask;
    if (t == null) return 0;
    const f = t.validatedProgressFloor;
    if (f == null) return 0;
    return Math.max(0, f);
  }

  isTaskLocked(t: Task): boolean {
    return t.status === 'VALIDATED' && (t.progress ?? 0) >= 100;
  }

  saveProgress(): void {
    if (!this.editingProgressId) return;
    this.api.updateProgress(this.editingProgressId, this.progressValue).subscribe({
      next: () => {
        this.closeProgressModal();
        this.reload();
        this.notify.show('success', 'Progress updated.');
      },
      error: (err) => {
        this.notify.show('error', apiErrorMessage(err, 'Could not update progress.'));
      }
    });
  }

  validate(t: Task): void {
    if (!t.id) return;
    this.api.validate(t.id).subscribe({
      next: () => {
        this.reload();
        this.notify.show('success', 'Task validated.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Could not validate task.'))
    });
  }

  confirmRemove(t: Task): void {
    this.toDelete = t;
    this.deleteModalOpen = true;
  }

  closeEditModal(): void {
    this.editModalOpen = false;
  }

  closeProgressModal(): void {
    this.progressModalOpen = false;
    this.progressEditTask = null;
  }

  cancelDelete(): void {
    this.deleteModalOpen = false;
    this.toDelete = null;
  }

  confirmDelete(): void {
    const t = this.toDelete;
    this.cancelDelete();
    if (t) this.remove(t);
  }

  private remove(t: Task): void {
    if (!t.id) return;
    this.api.delete(t.id).subscribe({
      next: () => {
        this.reload();
        this.notify.show('success', 'Task removed.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Could not remove task.'))
    });
  }
}
