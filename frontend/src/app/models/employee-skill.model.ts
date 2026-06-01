import type { User } from './user.model';
import type { Skill } from './skill.model';

export type SkillProficiency = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';

export interface EmployeeSkill {
  id?: number;
  user?: User;
  skill?: Skill;
  level?: SkillProficiency;
}
