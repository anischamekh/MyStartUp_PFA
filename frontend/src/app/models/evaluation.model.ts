import type { User } from './user.model';

export interface Evaluation {
  id?: number;
  employee?: User | null;
  evaluator?: User | null;
  score: number;
  technicalSkill?: number | null;
  teamwork?: number | null;
  deadlineRespect?: number | null;
  comment?: string | null;
  date?: string;
}
