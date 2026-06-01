import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { SelectModule } from 'primeng/select';
import { DatePickerModule } from 'primeng/datepicker';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

import { AuthService } from '../../services/auth.service';
import { SkillApiService } from '../../services/skill.service';
import { EmployeeSkillApiService } from '../../services/employee-skill.service';
import { TrainingApiService } from '../../services/training.service';
import { UserService } from '../../services/user.service';
import type { Skill } from '../../models/skill.model';
import type { EmployeeSkill, SkillProficiency } from '../../models/employee-skill.model';
import type { Training, TrainingAttendance } from '../../models/training.model';
import type { User } from '../../models/user.model';
import { NotifyService } from '../../ui/notify.service';
import { apiErrorMessage } from '../../utils/api-error';
import { parseYmd, toYmd } from '../../utils/date-form';

@Component({
  selector: 'app-skills-training',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    CardModule,
    InputTextModule,
    TextareaModule,
    SelectModule,
    DatePickerModule,
    ProgressSpinnerModule
  ],
  templateUrl: './skills-training.component.html',
  styleUrl: './skills-training.component.css'
})
export class SkillsTrainingComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly skillsApi = inject(SkillApiService);
  private readonly empSkillsApi = inject(EmployeeSkillApiService);
  private readonly trainingsApi = inject(TrainingApiService);
  private readonly usersApi = inject(UserService);
  private readonly notify = inject(NotifyService);

  isHr = false;
  skills: Skill[] = [];
  empSkills: EmployeeSkill[] = [];
  trainings: Training[] = [];
  users: User[] = [];
  userOptions: { label: string; value: number }[] = [];
  skillOptions: { label: string; value: number }[] = [];

  loadingSkills = true;
  loadingEmp = true;
  loadingTrain = true;

  skillDialog = false;
  skillForm: Skill = { name: '' };
  skillEditId: number | null = null;

  esDialog = false;
  esUserId: number | null = null;
  esSkillId: number | null = null;
  esLevel: SkillProficiency = 'INTERMEDIATE';
  levelOptions: { label: string; value: SkillProficiency }[] = [
    { label: 'Beginner', value: 'BEGINNER' },
    { label: 'Intermediate', value: 'INTERMEDIATE' },
    { label: 'Advanced', value: 'ADVANCED' },
    { label: 'Expert', value: 'EXPERT' }
  ];

  trDialog = false;
  trForm: Training = { title: '', description: '', date: '' };
  trEditId: number | null = null;
  trDate: Date | null = null;

  trainingFocusId: number | null = null;
  attendanceRows: TrainingAttendance[] = [];
  loadingAtt = false;
  taUserId: number | null = null;

  ngOnInit(): void {
    this.isHr = this.auth.role === 'HR';
    this.usersApi.all().subscribe((u) => {
      this.users = u;
      this.userOptions = u.filter((x) => x.id != null).map((x) => ({ label: `${x.fullName}`, value: x.id as number }));
    });
    this.reloadSkills();
    this.reloadEmpSkills();
    this.reloadTrainings();
  }

  reloadSkills(): void {
    this.loadingSkills = true;
    this.skillsApi.all().subscribe({
      next: (s) => {
        this.skills = s;
        this.skillOptions = s.filter((x) => x.id != null).map((x) => ({ label: x.name ?? '', value: x.id as number }));
        this.loadingSkills = false;
      },
      error: (err) => {
        this.loadingSkills = false;
        this.notify.show('error', apiErrorMessage(err, 'Could not load skills.'));
      }
    });
  }

  reloadEmpSkills(): void {
    this.loadingEmp = true;
    this.empSkillsApi.list().subscribe({
      next: (r) => {
        this.empSkills = r;
        this.loadingEmp = false;
      },
      error: (err) => {
        this.loadingEmp = false;
        this.notify.show('error', apiErrorMessage(err, 'Could not load employee skills.'));
      }
    });
  }

  reloadTrainings(): void {
    this.loadingTrain = true;
    this.trainingsApi.all().subscribe({
      next: (r) => {
        this.trainings = r;
        this.loadingTrain = false;
      },
      error: (err) => {
        this.loadingTrain = false;
        this.notify.show('error', apiErrorMessage(err, 'Could not load trainings.'));
      }
    });
  }

  openSkill(): void {
    this.skillEditId = null;
    this.skillForm = { name: '' };
    this.skillDialog = true;
  }

  editSkill(s: Skill): void {
    this.skillEditId = s.id ?? null;
    this.skillForm = { name: s.name ?? '' };
    this.skillDialog = true;
  }

  saveSkill(): void {
    const req =
      this.skillEditId != null
        ? this.skillsApi.update(this.skillEditId, this.skillForm)
        : this.skillsApi.create(this.skillForm);
    req.subscribe({
      next: () => {
        this.skillDialog = false;
        this.reloadSkills();
        this.notify.show('success', 'Skill saved.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Save failed.'))
    });
  }

  deleteSkill(s: Skill): void {
    if (!s.id) return;
    this.skillsApi.delete(s.id).subscribe({
      next: () => {
        this.reloadSkills();
        this.notify.show('success', 'Skill removed.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Delete failed.'))
    });
  }

  openEs(): void {
    this.esUserId = null;
    this.esSkillId = null;
    this.esLevel = 'INTERMEDIATE';
    this.esDialog = true;
  }

  saveEs(): void {
    if (!this.esUserId || !this.esSkillId) {
      this.notify.show('warn', 'Select user and skill.');
      return;
    }
    const body: EmployeeSkill = {
      user: { id: this.esUserId } as User,
      skill: { id: this.esSkillId } as Skill,
      level: this.esLevel
    };
    this.empSkillsApi.upsert(body).subscribe({
      next: () => {
        this.esDialog = false;
        this.reloadEmpSkills();
        this.notify.show('success', 'Skill assigned.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Assign failed.'))
    });
  }

  deleteEs(es: EmployeeSkill): void {
    if (!es.id) return;
    this.empSkillsApi.delete(es.id).subscribe({
      next: () => {
        this.reloadEmpSkills();
        this.notify.show('success', 'Removed.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Delete failed.'))
    });
  }

  openTr(): void {
    this.trEditId = null;
    this.trForm = { title: '', description: '', date: '' };
    this.trDate = null;
    this.trDialog = true;
  }

  editTr(t: Training): void {
    this.trEditId = t.id ?? null;
    this.trForm = { title: t.title ?? '', description: t.description ?? '', date: t.date ?? '' };
    this.trDate = parseYmd(t.date ?? undefined);
    this.trDialog = true;
  }

  saveTr(): void {
    const d = toYmd(this.trDate);
    if (!d) {
      this.notify.show('warn', 'Pick a date.');
      return;
    }
    this.trForm.date = d;
    const req =
      this.trEditId != null ? this.trainingsApi.update(this.trEditId, this.trForm) : this.trainingsApi.create(this.trForm);
    req.subscribe({
      next: () => {
        this.trDialog = false;
        this.reloadTrainings();
        this.notify.show('success', 'Training saved.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Save failed.'))
    });
  }

  deleteTr(t: Training): void {
    if (!t.id) return;
    this.trainingsApi.delete(t.id).subscribe({
      next: () => {
        this.reloadTrainings();
        if (this.trainingFocusId === t.id) {
          this.trainingFocusId = null;
          this.attendanceRows = [];
        }
        this.notify.show('success', 'Removed.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Delete failed.'))
    });
  }

  trainingOptions(): { label: string; value: number }[] {
    return this.trainings
      .filter((x) => x.id != null)
      .map((x) => ({ label: `${x.title} (${x.date})`, value: x.id as number }));
  }

  onTrainingFocusChange(): void {
    if (this.trainingFocusId == null) {
      this.attendanceRows = [];
      return;
    }
    this.loadingAtt = true;
    this.trainingsApi.listAttendance(this.trainingFocusId).subscribe({
      next: (r) => {
        this.attendanceRows = r;
        this.loadingAtt = false;
      },
      error: (err) => {
        this.loadingAtt = false;
        this.notify.show('error', apiErrorMessage(err, 'Could not load attendance.'));
      }
    });
  }

  addTrainingAttendance(): void {
    if (this.trainingFocusId == null || this.taUserId == null) {
      this.notify.show('warn', 'Select training and employee.');
      return;
    }
    this.trainingsApi.addAttendance(this.trainingFocusId, this.taUserId).subscribe({
      next: () => {
        this.taUserId = null;
        this.onTrainingFocusChange();
        this.notify.show('success', 'Employee assigned to training.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Assign failed.'))
    });
  }

  toggleAttendance(row: TrainingAttendance): void {
    if (!row.id) {
      return;
    }
    const next = !row.attended;
    this.trainingsApi.setAttended(row.id, next).subscribe({
      next: (u) => {
        row.attended = u.attended;
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Update failed.'))
    });
  }
}
