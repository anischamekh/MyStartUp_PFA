import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { EmployeeProfile } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class EmployeeProfileService {
  private readonly api = inject(ApiService);

  all() {
    return this.api.client.get<EmployeeProfile[]>(`${this.api.baseUrl}/employee-profiles`);
  }
}
