package tn.iteam.backend.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.Skill;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.SkillRepository;
import tn.iteam.backend.service.SkillService;

@Service
@Transactional
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final CurrentUserProvider currentUserProvider;

    public SkillServiceImpl(SkillRepository skillRepository, CurrentUserProvider currentUserProvider) {
        this.skillRepository = skillRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public List<Skill> findAll() {
        return skillRepository.findAll();
    }

    @Override
    public Skill findById(Long id) {
        return skillRepository.findById(id).orElseThrow(() -> new BusinessException("Skill not found"));
    }

    @Override
    public Skill save(Skill skill) {
        requireHr();
        if (skill.getId() != null) {
            skill.setId(null);
        }
        if (skill.getName() == null || skill.getName().isBlank()) {
            throw new BusinessException("Skill name is required");
        }
        skillRepository.findByNameIgnoreCase(skill.getName().trim()).ifPresent(s -> {
            throw new BusinessException("Skill already exists: " + skill.getName());
        });
        skill.setName(skill.getName().trim());
        return skillRepository.save(skill);
    }

    @Override
    public Skill update(Long id, Skill skill) {
        requireHr();
        Skill existing = findById(id);
        if (skill.getName() == null || skill.getName().isBlank()) {
            throw new BusinessException("Skill name is required");
        }
        String n = skill.getName().trim();
        skillRepository.findByNameIgnoreCase(n).ifPresent(s -> {
            if (!s.getId().equals(id)) {
                throw new BusinessException("Skill already exists: " + n);
            }
        });
        existing.setName(n);
        return skillRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        requireHr();
        skillRepository.deleteById(id);
    }

    private void requireHr() {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.HR) {
            throw new BusinessException("Only HR can modify skills");
        }
    }
}
