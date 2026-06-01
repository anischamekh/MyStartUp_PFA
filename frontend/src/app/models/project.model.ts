import type { Team } from './team.model';
import type { User } from './user.model';

export type ProjectStatus = 'PLANNING' | 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'CANCELLED';

export interface Project {
  id?: number;
  name: string;
  description?: string | null;
  startDate?: string | null;
  endDate?: string | null;
  status?: ProjectStatus;
  progress?: number;
  manager?: User | null;
  teams?: Team[];
  /** Flattened for global search (not sent to API). */
  teamSearch?: string;
  leaderSearch?: string;
}
