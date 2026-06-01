import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { Training, TrainingAttendance } from '../models/training.model';

@Injectable({ providedIn: 'root' })
export class TrainingApiService {
  private readonly api = inject(ApiService);

  all() {
    return this.api.client.get<Training[]>(`${this.api.baseUrl}/trainings`);
  }

  create(body: Training) {
    return this.api.client.post<Training>(`${this.api.baseUrl}/trainings`, body);
  }

  update(id: number, body: Training) {
    return this.api.client.put<Training>(`${this.api.baseUrl}/trainings/${id}`, body);
  }

  delete(id: number) {
    return this.api.client.delete(`${this.api.baseUrl}/trainings/${id}`);
  }

  listAttendance(trainingId: number) {
    return this.api.client.get<TrainingAttendance[]>(
      `${this.api.baseUrl}/trainings/${trainingId}/attendance`
    );
  }

  addAttendance(trainingId: number, userId: number) {
    return this.api.client.post<TrainingAttendance>(
      `${this.api.baseUrl}/trainings/${trainingId}/attendance`,
      { userId }
    );
  }

  setAttended(attendanceId: number, attended: boolean) {
    return this.api.client.patch<TrainingAttendance>(
      `${this.api.baseUrl}/trainings/attendance/${attendanceId}`,
      { attended }
    );
  }
}
