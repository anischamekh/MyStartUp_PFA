import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { CardModule } from 'primeng/card';
import { DatePickerModule } from 'primeng/datepicker';
import { InputNumberModule } from 'primeng/inputnumber';

import { AuthService } from '../../services/auth.service';
import type { RoleName } from '../../models/role-name.model';
import type { CreateUserRequest, UpdateUserRequest } from '../../models/user-request.model';
import type { EmployeeProfile, ExperienceLevel, Speciality, User } from '../../models/user.model';
import type { Team } from '../../models/team.model';
import { UserService } from '../../services/user.service';
import { EmployeeProfileService } from '../../services/employee-profile.service';
import { TeamService } from '../../services/team.service';
import { NotifyService } from '../../ui/notify.service';
import { parseYmd, toYmd } from '../../utils/date-form';
import { apiErrorMessage } from '../../utils/api-error';

@Component({
  selector: 'app-employees',
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
    InputNumberModule
  ],
  templateUrl: './employees.component.html',
  styleUrl: './employees.component.css'
})
export class EmployeesComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly api = inject(UserService);
  private readonly profilesApi = inject(EmployeeProfileService);
  private readonly teamsApi = inject(TeamService);
  private readonly notify = inject(NotifyService);

  teams: Team[] = [];
  selectedTeamId: number | null = null;

  users: User[] = [];
  loading = true;

  editingId: number | null = null;
  editModalOpen = false;
  deleteModalOpen = false;
  toDelete: User | null = null;

  roleOptions: { label: string; value: RoleName }[] = [
    { label: 'Employee', value: 'EMPLOYEE' },
    { label: 'Team leader', value: 'TEAM_LEADER' },
    { label: 'Manager', value: 'MANAGER' },
    { label: 'HR', value: 'HR' },
    { label: 'Admin', value: 'ADMIN' }
  ];
  selectedRole: RoleName = 'EMPLOYEE';

  specialityOptions: { label: string; value: Speciality }[] = [
    { label: 'Frontend', value: 'FRONTEND' },
    { label: 'Backend', value: 'BACKEND' },
    { label: 'UI/UX', value: 'UI_UX' },
    { label: 'Infrastructure', value: 'INFRASTRUCTURE' }
  ];
  selectedSpeciality: Speciality = 'FRONTEND';

  experienceOptions: { label: string; value: ExperienceLevel }[] = [
    { label: 'Junior', value: 'JUNIOR' },
    { label: 'Mid', value: 'MID' },
    { label: 'Senior', value: 'SENIOR' },
    { label: 'Lead', value: 'LEAD' }
  ];
  selectedExperience: ExperienceLevel = 'MID';

  filterRole: RoleName | 'ALL' = 'ALL';
  query = '';

  roleFilterOptions: { label: string; value: RoleName | 'ALL' }[] = [
    { label: 'All roles', value: 'ALL' },
    { label: 'Employee', value: 'EMPLOYEE' },
    { label: 'Team leader', value: 'TEAM_LEADER' },
    { label: 'Manager', value: 'MANAGER' },
    { label: 'HR', value: 'HR' },
    { label: 'Admin', value: 'ADMIN' }
  ];

  form: User = {
    username: '',
    password: '',
    fullName: '',
    email: '',
    role: { name: 'EMPLOYEE' },
    employeeProfile: {}
  };

  hireDateModel: Date | null = null;

  filteredUsers: User[] = [];

  ngOnInit(): void {
    this.teamsApi.all().subscribe({
      next: (t) => (this.teams = t),
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Could not load teams.'))
    });
    this.reload();
  }

  reload(): void {
    this.loading = true;
    forkJoin({
      users: this.api.all(),
      profiles: this.profilesApi.all().pipe(catchError(() => of([] as EmployeeProfile[])))
    }).subscribe({
      next: ({ users, profiles }) => {
        const profileByUserId = new Map<number, EmployeeProfile>();
        for (const p of profiles) {
          if (p.userId != null) {
            profileByUserId.set(p.userId, p);
          }
        }
        this.users = users.map((u) => {
          const fromList = u.id != null ? profileByUserId.get(u.id) : undefined;
          const merged: EmployeeProfile = { ...(fromList ?? {}), ...(u.employeeProfile ?? {}) };
          const teamName = merged.team?.name ?? u.teamName ?? '';
          return {
            ...u,
            employeeProfile: Object.keys(merged).length ? merged : u.employeeProfile ?? {},
            roleName: u.role?.name ?? '',
            teamName
          };
        });
        this.filteredUsers = [...this.users];
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.notify.show('error', apiErrorMessage(err, 'Could not load employees.'));
      }
    });
  }

  applyFilters(): void {
    const q = (this.query ?? '').toLowerCase().trim();
    const role = this.filterRole;
    this.filteredUsers = this.users.filter((u) => {
      const roleOk = role === 'ALL' ? true : (u.role?.name ?? null) === role;
      const blob =
        `${u.fullName ?? ''} ${u.username ?? ''} ${u.email ?? ''} ${u.role?.name ?? ''} ${u.teamName ?? ''}`.toLowerCase();
      const qOk = !q || blob.includes(q);
      return roleOk && qOk;
    });
  }

  openNew(): void {
    this.editingId = null;
    this.form = {
      username: '',
      password: '',
      fullName: '',
      email: '',
      role: { name: 'EMPLOYEE' },
      employeeProfile: { remainingLeaveDays: 30 }
    };
    this.selectedRole = 'EMPLOYEE';
    this.selectedSpeciality = 'FRONTEND';
    this.selectedExperience = 'MID';
    this.selectedTeamId = null;
    this.hireDateModel = null;
    this.editModalOpen = true;
  }

  edit(u: User): void {
    this.editingId = u.id ?? null;
    this.form = {
      ...u,
      password: '',
      employeeProfile: { ...(u.employeeProfile ?? {}) }
    };
    this.selectedRole = (u.role?.name ?? 'EMPLOYEE') as RoleName;
    this.selectedSpeciality = (u.employeeProfile?.speciality as Speciality) ?? 'FRONTEND';
    this.selectedExperience = (u.employeeProfile?.experienceLevel as ExperienceLevel) ?? 'MID';
    this.selectedTeamId = u.teamId ?? u.employeeProfile?.team?.id ?? null;
    this.hireDateModel = parseYmd(u.employeeProfile?.hireDate ?? undefined);
    this.editModalOpen = true;
  }

  submit(): void {
    if (this.selectedRole !== 'ADMIN' && this.selectedTeamId == null) {
      this.notify.show('warn', 'Please select a team.');
      return;
    }

    const email = this.form.email?.trim() ?? '';
    const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    if (!this.form.username?.trim() || !this.form.fullName?.trim()) {
      this.notify.show('warn', 'Username and full name are required.');
      return;
    }
    if (!emailOk) {
      this.notify.show('warn', 'Enter a valid email address.');
      return;
    }
    if (!this.editingId && !this.form.password?.trim()) {
      this.notify.show('warn', 'Password is required for new users.');
      return;
    }

    const profile: EmployeeProfile = {
      phone: this.form.employeeProfile?.phone?.trim() || undefined,
      address: this.form.employeeProfile?.address?.trim() || undefined,
      jobTitle: this.form.employeeProfile?.jobTitle?.trim() || undefined,
      speciality: this.selectedSpeciality,
      experienceLevel: this.selectedExperience,
      hireDate: toYmd(this.hireDateModel) || undefined,
      salary: this.form.employeeProfile?.salary,
      remainingLeaveDays: this.form.employeeProfile?.remainingLeaveDays
    };

    const teamId = this.selectedRole !== 'ADMIN' ? this.selectedTeamId : null;

    if (this.editingId) {
      const updatePayload: UpdateUserRequest = {
        username: this.form.username.trim(),
        fullName: this.form.fullName.trim(),
        email,
        role: this.selectedRole,
        teamId,
        employeeProfile: profile
      };
      const pwd = this.form.password?.trim();
      if (pwd) {
        updatePayload.password = pwd;
      }
      this.api.update(this.editingId, updatePayload).subscribe({
        next: () => this.onSaveSuccess(true),
        error: (err) => this.notify.show('error', apiErrorMessage(err, 'Could not save employee.'))
      });
    } else {
      const createPayload: CreateUserRequest = {
        username: this.form.username.trim(),
        password: this.form.password!.trim(),
        fullName: this.form.fullName.trim(),
        email,
        role: this.selectedRole,
        teamId,
        employeeProfile: profile
      };
      this.api.create(createPayload).subscribe({
        next: () => this.onSaveSuccess(false),
        error: (err) => this.notify.show('error', apiErrorMessage(err, 'Could not save employee.'))
      });
    }
  }

  private onSaveSuccess(isEdit: boolean): void {
    this.closeEditModal();
    this.reload();
    this.notify.show('success', isEdit ? 'Employee updated.' : 'User created successfully.');
  }

  confirmRemove(u: User): void {
    this.toDelete = u;
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
    const u = this.toDelete;
    this.cancelDelete();
    if (u) this.remove(u);
  }

  private remove(u: User): void {
    if (!u.id) return;
    this.api.delete(u.id).subscribe({
      next: () => {
        this.reload();
        this.notify.show('success', 'Employee removed.');
      },
      error: (err) => {
        this.notify.show('error', apiErrorMessage(err, 'Could not delete employee.'));
      }
    });
  }
}
