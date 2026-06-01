import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { DialogModule } from 'primeng/dialog';
import { DatePickerModule } from 'primeng/datepicker';
import { CardModule } from 'primeng/card';
import { SelectModule } from 'primeng/select';
import { MultiSelectModule } from 'primeng/multiselect';
import { InputNumberModule } from 'primeng/inputnumber';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';

import { AuthService } from '../../services/auth.service';
import { ProjectService } from '../../services/project.service';
import { TeamService } from '../../services/team.service';
import { parseYmd, toYmd } from '../../utils/date-form';
import type { Project, ProjectStatus } from '../../models/project.model';
import type { Team } from '../../models/team.model';
import { NotifyService } from '../../ui/notify.service';
import { I18nService } from '../../services/i18n.service';
import { apiErrorMessage } from '../../utils/api-error';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    TextareaModule,
    DialogModule,
    DatePickerModule,
    CardModule,
    SelectModule,
    MultiSelectModule,
    InputNumberModule,
    ProgressSpinnerModule,
    IconFieldModule,
    InputIconModule
  ],
  templateUrl: './projects.component.html',
  styleUrl: './projects.component.css'
})
export class ProjectsComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly projectsApi = inject(ProjectService);
  private readonly teamsApi = inject(TeamService);
  private readonly notify = inject(NotifyService);
  private readonly i18n = inject(I18nService);

  readonly parseYmd = parseYmd;
  readonly toYmd = toYmd;

  loading = true;
  projects: Project[] = [];
  teams: Team[] = [];
  teamOptions: Team[] = [];

  editingId: number | null = null;
  modalOpen = false;
  form: Project = {
    name: '',
    description: '',
    startDate: '',
    endDate: '',
    status: 'PLANNING',
    progress: 0,
    teams: []
  };
  startDateModel: Date | null = null;
  endDateModel: Date | null = null;
  selectedTeams: Team[] = [];

  statusOptions: { label: string; value: ProjectStatus }[] = [
    { label: 'Planning', value: 'PLANNING' },
    { label: 'Active', value: 'ACTIVE' },
    { label: 'On hold', value: 'ON_HOLD' },
    { label: 'Completed', value: 'COMPLETED' },
    { label: 'Cancelled', value: 'CANCELLED' }
  ];

  get readOnly(): boolean {
    return this.auth.viewOnlyAdmin();
  }

  ngOnInit(): void {
    this.teamsApi.all().subscribe({
      next: (t) => {
        this.teams = t;
        this.teamOptions = t;
      },
      error: () => this.notify.showKey('error', 'notify.loadFailed')
    });
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.projectsApi.all().subscribe({
      next: (data) => {
        this.projects = data.map((p) => ({
          ...p,
          teamSearch: (p.teams ?? []).map((t) => t.name).join(' '),
          leaderSearch: (p.teams ?? [])
            .map((t) => t.teamLeaderName || t.teamLeader?.fullName || t.teamLeader?.username || '')
            .join(' ')
        }));
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.notify.show('error', apiErrorMessage(err, this.i18n.t('notify.loadFailed')));
      }
    });
  }

  openNew(): void {
    if (this.readOnly) return;
    this.editingId = null;
    this.form = {
      name: '',
      description: '',
      startDate: '',
      endDate: '',
      status: 'PLANNING',
      progress: 0,
      teams: []
    };
    this.selectedTeams = [];
    this.startDateModel = null;
    this.endDateModel = null;
    this.modalOpen = true;
  }

  edit(p: Project): void {
    if (this.readOnly) return;
    this.editingId = p.id ?? null;
    this.form = {
      ...p,
      teams: p.teams ? [...p.teams] : []
    };
    this.startDateModel = parseYmd(p.startDate ?? undefined);
    this.endDateModel = parseYmd(p.endDate ?? undefined);
    this.selectedTeams = this.teamOptions.filter((opt) => (p.teams ?? []).some((t) => t.id === opt.id));
    this.modalOpen = true;
  }

  save(): void {
    if (!this.form.name?.trim()) {
      this.notify.showKey('warn', 'projects.nameRequired');
      return;
    }
    if (!this.selectedTeams.length) {
      this.notify.showKey('warn', 'projects.teamRequired');
      return;
    }
    this.form.startDate = toYmd(this.startDateModel) || undefined;
    this.form.endDate = toYmd(this.endDateModel) || undefined;
    this.form.teams = this.selectedTeams.map((t) => ({ id: t.id, name: t.name }));
    if (this.form.progress == null) {
      this.form.progress = 0;
    }

    const payload = { ...this.form };
    delete (payload as Project & { teamSearch?: string }).teamSearch;
    delete (payload as Project & { leaderSearch?: string }).leaderSearch;

    const req = this.editingId
      ? this.projectsApi.update(this.editingId, payload)
      : this.projectsApi.create(payload);

    req.subscribe({
      next: () => {
        this.modalOpen = false;
        this.reload();
        this.notify.showKey('success', 'projects.saved');
      },
      error: (err) => {
        this.notify.show('error', apiErrorMessage(err, this.i18n.t('notify.saveFailed')));
      }
    });
  }

  teamLabels(p: Project): string {
    return (p.teams ?? []).map((t) => t.name).join(', ') || '—';
  }
}
