import type { User } from './user.model';

export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export type LeaveType = 'ANNUAL' | 'SICK' | 'PERSONAL' | 'UNPAID' | 'OTHER';

export interface LeaveRequest {
  id?: number;
  employee?: User | null;
  manager?: User | null;
  startDate: string;
  endDate: string;
  days?: number;
  reason?: string | null;
  status?: LeaveStatus;
  leaveType?: LeaveType;
  /** Flattened for PrimeNG table global filter (do not send to API). */
  employeeName?: string;
}

