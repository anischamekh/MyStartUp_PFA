import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { CardModule } from 'primeng/card';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';

import { AuthService } from '../../services/auth.service';
import { DocumentService } from '../../services/document.service';
import { UserService } from '../../services/user.service';
import type { EmployeeDocument } from '../../models/document.model';
import type { User } from '../../models/user.model';
import { NotifyService } from '../../ui/notify.service';
import { apiErrorMessage } from '../../utils/api-error';

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    CardModule,
    ProgressSpinnerModule,
    SelectModule,
    InputTextModule
  ],
  templateUrl: './documents.component.html',
  styleUrl: './documents.component.css'
})
export class DocumentsComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly api = inject(DocumentService);
  private readonly usersApi = inject(UserService);
  private readonly notify = inject(NotifyService);

  docs: EmployeeDocument[] = [];
  users: User[] = [];
  userOptions: { label: string; value: number }[] = [];
  loading = true;
  isHr = false;

  uploadOpen = false;
  uploadUserId: number | null = null;
  uploadName = '';
  uploadType = '';
  selectedFile: File | null = null;

  ngOnInit(): void {
    this.isHr = this.auth.role === 'HR';
    this.usersApi.all().subscribe((u) => {
      this.users = u;
      this.userOptions = u.filter((x) => x.id != null).map((x) => ({ label: `${x.fullName}`, value: x.id as number }));
    });
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.api.list().subscribe({
      next: (d) => {
        this.docs = d;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.notify.show('error', apiErrorMessage(err, 'Could not load documents.'));
      }
    });
  }

  openUpload(): void {
    this.uploadUserId = this.auth.userId;
    this.uploadName = '';
    this.uploadType = '';
    this.selectedFile = null;
    this.uploadOpen = true;
  }

  onNativeFile(ev: Event): void {
    const t = ev.target as HTMLInputElement;
    this.selectedFile = t.files?.[0] ?? null;
  }

  submitUpload(): void {
    if (!this.uploadUserId || !this.selectedFile) {
      this.notify.show('warn', 'Choose a user and file.');
      return;
    }
    this.api.upload(this.uploadUserId, this.selectedFile, this.uploadName || undefined, this.uploadType || undefined).subscribe({
      next: () => {
        this.uploadOpen = false;
        this.reload();
        this.notify.show('success', 'Document uploaded.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Upload failed.'))
    });
  }

  download(d: EmployeeDocument): void {
    if (!d.id) return;
    this.api.download(d.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = d.name || 'document';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Download failed.'))
    });
  }

  remove(d: EmployeeDocument): void {
    if (!d.id) return;
    this.api.delete(d.id).subscribe({
      next: () => {
        this.reload();
        this.notify.show('success', 'Document deleted.');
      },
      error: (err) => this.notify.show('error', apiErrorMessage(err, 'Delete failed.'))
    });
  }
}
