package tn.iteam.backend.service;

import java.util.List;
import tn.iteam.backend.entity.Skill;

public interface SkillService {
    List<Skill> findAll();

    Skill findById(Long id);

    Skill save(Skill skill);

    Skill update(Long id, Skill skill);

    void delete(Long id);
}
