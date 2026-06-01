package tn.iteam.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.iteam.backend.entity.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByNameIgnoreCase(String name);
}
