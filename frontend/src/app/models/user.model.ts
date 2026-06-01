import type { RoleName } from './role-name.model';

export type Speciality = 'FRONTEND' | 'BACKEND' | 'UI_UX' | 'INFRASTRUCTURE';

export interface Role {
  id?: number;
  name: RoleName;
}

export type ExperienceLevel = 'JUNIOR' | 'MID' | 'SENIOR' | 'LEAD';

export interface EmployeeProfile {
  id?: number;
  /** Present on HR/ADMIN list from {@code GET /api/employee-profiles}. */
  userId?: number;
  phone?: string;
  address?: string;
  hireDate?: string;
  salary?: number;
  speciality?: Speciality;
  experienceLevel?: ExperienceLevel;
  jobTitle?: string;
  remainingLeaveDays?: number;
  team?: { id?: number; name?: string } | null;
}

export interface User {
  id?: number;
  username: string;
  password?: string;
  fullName: string;
  email: string;
  role: Role;
  employeeProfile?: EmployeeProfile | null;
  /** Shallow team fields from API when full profile is omitted (cycle-safe). */
  teamId?: number | null;
  /** Flattened for PrimeNG table global filter (do not send to API). */
  roleName?: string;
  teamName?: string;
}
