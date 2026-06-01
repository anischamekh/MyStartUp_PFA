import type { User } from './user.model';

export type NotificationType =
  | 'INFO'
  | 'TASK_ASSIGNED'
  | 'TASK_COMPLETED'
  | 'TASK_VALIDATED'
  | 'LEAVE_REQUESTED'
  | 'LEAVE_APPROVED'
  | 'LEAVE_REJECTED'
  | 'PROJECT_ASSIGNED'
  | 'EVALUATION_CREATED';

export interface Notification {
  id?: number;
  recipient?: User | null;
  type: NotificationType;
  message: string;
  createdAt?: string;
  read?: boolean;
}

