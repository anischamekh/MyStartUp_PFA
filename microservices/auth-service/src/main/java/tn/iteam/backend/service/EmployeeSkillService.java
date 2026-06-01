package tn.iteam.backend.service;

import java.util.List;
import tn.iteam.backend.entity.EmployeeSkill;

public interface EmployeeSkillService {
    List<EmployeeSkill> findVisible();

    List<EmployeeSkill> findForUser(Long userId);

    EmployeeSkill upsert(EmployeeSkill employeeSkill);

    void delete(Long id);
}
