import type { User } from './user.model';
import type { Speciality } from './user.model';

export interface Team {
  id?: number;
  name: string;
  speciality?: Speciality;
  teamLeader?: User | null;
  teamLeaderId?: number | null;
  teamLeaderName?: string | null;
  /** Flattened for PrimeNG table global filter (do not send to API). */
  leaderName?: string;
}
