import type { User } from './user.model';

export interface Payroll {
  id?: number;
  user?: User;
  baseSalary?: number;
  bonus?: number;
  deductions?: number;
  totalSalary?: number;
}
