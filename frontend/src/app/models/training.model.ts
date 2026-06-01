import type { User } from './user.model';

export interface Training {
  id?: number;
  title?: string;
  description?: string | null;
  date?: string;
}

export interface TrainingAttendance {
  id?: number;
  training?: Training;
  user?: User;
  attended?: boolean;
}
