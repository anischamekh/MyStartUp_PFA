package tn.iteam.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.iteam.backend.entity.EmployeeSkill;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {
    List<EmployeeSkill> findByUser_Id(Long userId);

    Optional<EmployeeSkill> findByUser_IdAndSkill_Id(Long userId, Long skillId);

    void deleteByUser_Id(Long userId);
}
