import type { RoleName } from './role-name.model';
import type { EmployeeProfile } from './user.model';

/** Payload for POST /api/users */
export interface CreateUserRequest {
  username: string;
  password: string;
  fullName: string;
  email: string;
  role: RoleName;
  teamId?: number | null;
  employeeProfile?: EmployeeProfile;
}

/** Payload for PUT /api/users/{id} */
export interface UpdateUserRequest {
  username: string;
  password?: string;
  fullName: string;
  email: string;
  role: RoleName;
  teamId?: number | null;
  employeeProfile?: EmployeeProfile;
}

/** Response from create/update user endpoints */
export interface UserResponse {
  id?: number;
  username: string;
  fullName: string;
  email: string;
  role: RoleName;
  teamId?: number | null;
  teamName?: string;
  employeeProfile?: EmployeeProfile;
}
