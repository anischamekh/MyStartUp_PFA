import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { CardModule } from 'primeng/card';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';

import { AuthService } from '../../services/auth.service';
import type { Team } from '../../models/team.model';
import type { Speciality } from '../../models/user.model';
import type { User } from '../../models/user.model';
import { TeamService } from '../../services/team.service';
import { UserService } from '../../services/user.service';
import { NotifyService } from '../../ui/notify.service';
import { apiErrorMessage } from '../../utils/api-error';

@Component({
  selector: 'app-teams',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    DialogModule,
    SelectModule,
    CardModule,
    ProgressSpinnerModule,
    IconFieldModule,
    InputIconModule
  ],
  templateUrl: './teams.component.html',
  styleUrl: './teams.component.css'
})
export class TeamsComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly teamsApi = inject(TeamService);
  private readonly usersApi = inject(UserService);
  private readonly notify = inject(NotifyService);

  teams: Team[] = [];
  leaders: User[] = [];
  loading = true;

  editingId: number | null = null;

  form: Team = { name: '', teamLeader: null, speciality: 'FRONTEND' };
  leaderOptions: { label: string; value: number | null }[] = [];
  selectedLeaderId: number | null = null;
  specialityOptions: { label: string; value: Speciality }[] = [
    { label: 'Frontend', value: 'FRONTEND' },
    { label: 'Backend', value: 'BACKEND' },
    { label: 'UI / UX', value: 'UI_UX' },
    { label: 'Infrastructure', value: 'INFRASTRUCTURE' }
  ];

  editModalOpen = false;
  deleteModalOpen = false;
  toDelete: Team | null = null;

  ngOnInit(): void {
    this.reload();
    this.usersApi.all().subscribe((u) => {
      this.leaders = u.filter((x) => x.role?.name === 'TEAM_LEADER');
      this.leaderOptions = [
        { label: 'No leader', value: null },
        ...this.leaders.map((l) => ({ label: `${l.fullName} (${l.username})`, value: l.id ?? null }))
      ];
    });

  }

  reload(): void {
    this.loading = true;
    this.teamsApi.all().subscribe({
      next: (data) => {
        this.teams = data.map((t) => ({
          ...t,
          leaderName:
            t.teamLeaderName ||
            t.teamLeader?.fullName ||
            t.teamLeader?.username ||
            ''
        }));
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.notify.show('error', apiErrorMessage(err, 'Could not load teams.'));
      }
    });
  }

  openNew(): void {
    this.editingId = null;
    this.form = { name: '', teamLeader: null, speciality: 'FRONTEND' };
    this.selectedLeaderId = null;
    this.editModalOpen = true;
  }

  edit(t: Team): void {
    this.editingId = t.id ?? null;
    this.form = { ...t, speciality: t.speciality ?? 'FRONTEND' };
    this.selectedLeaderId = (t.teamLeaderId ?? t.teamLeader?.id ?? null) as number | null;
    this.editModalOpen = true;
  }

  submit(): void {
    if (!this.form.name?.trim()) {
      this.notify.show('warn', 'Team name is required.');
      return;
    }
    const leader = this.leaders.find((l) => l.id === this.selectedLeaderId) ?? null;
    const payload: Team = {
      name: this.form.name.trim(),
      speciality: this.form.speciality ?? 'FRONTEND',
      teamLeader: leader ? ({ id: leader.id } as User) : null
    };
    const req = this.editingId ? this.teamsApi.update(this.editingId, payload) : this.teamsApi.create(payload);
    req.subscribe({
      next: () => {
        this.closeEditModal();
        this.reload();
        this.notify.show('success', 'Team saved.');
      },
      error: (err) => {
        this.notify.show('error', apiErrorMessage(err, 'Could not save team.'));
      }
    });
  }

  confirmRemove(t: Team): void {
    this.toDelete = t;
    this.deleteModalOpen = true;
  }

  closeEditModal(): void {
    this.editModalOpen = false;
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

  specialityLabel(s: Speciality | undefined): string {
    const found = this.specialityOptions.find((o) => o.value === s);
    return found?.label ?? (s ?? '—');
  }

  private remove(t: Team): void {
    if (!t.id) return;
    this.teamsApi.delete(t.id).subscribe({
      next: () => {
        this.reload();
        this.notify.show('success', 'Team removed.');
      },
      error: (err) => {
        this.notify.show('error', apiErrorMessage(err, 'Could not remove team.'));
      }
    });
  }
}
