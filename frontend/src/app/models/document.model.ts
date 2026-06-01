import type { User } from './user.model';

export interface EmployeeDocument {
  id?: number;
  user?: User;
  name?: string;
  type?: string | null;
  filePath?: string;
  uploadDate?: string;
}
