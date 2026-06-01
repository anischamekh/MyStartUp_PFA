import type { Project } from './project.model';
import type { User } from './user.model';

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE' | 'VALIDATED';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Task {
  id?: number;
  title: string;
  description?: string | null;
  status?: TaskStatus;
  priority?: TaskPriority;
  progress?: number;
  dueDate?: string | null;
  estimatedHours?: number | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  assignedTo?: User | null;
  createdBy?: User | null;
  project?: Project | null;
  /** After leader validation: progress cannot go below this (from API). */
  validatedProgressFloor?: number | null;
  /** Flattened for PrimeNG table global filter (do not send to API). */
  assigneeName?: string;
  projectTitle?: string;
  creatorName?: string;
}

