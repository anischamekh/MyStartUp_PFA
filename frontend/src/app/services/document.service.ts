import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { EmployeeDocument } from '../models/document.model';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly api = inject(ApiService);

  list() {
    return this.api.client.get<EmployeeDocument[]>(`${this.api.baseUrl}/documents`);
  }

  listForUser(userId: number) {
    return this.api.client.get<EmployeeDocument[]>(`${this.api.baseUrl}/documents/user/${userId}`);
  }

  upload(userId: number, file: File, name?: string, type?: string) {
    const fd = new FormData();
    fd.append('userId', String(userId));
    fd.append('file', file);
    if (name) fd.append('name', name);
    if (type) fd.append('type', type);
    return this.api.client.post<EmployeeDocument>(`${this.api.baseUrl}/documents`, fd);
  }

  download(id: number) {
    return this.api.client.get(`${this.api.baseUrl}/documents/${id}/download`, { responseType: 'blob' });
  }

  delete(id: number) {
    return this.api.client.delete(`${this.api.baseUrl}/documents/${id}`);
  }
}
