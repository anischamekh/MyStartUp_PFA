package tn.iteam.backend.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.Skill;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.SkillRepository;
import tn.iteam.backend.service.SkillService;
import tn.iteam.common.security.JwtUserPrincipal;

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
        return skillRepository.save(skill);
    }

    @Override
    public Skill update(Long id, Skill skill) {
        requireHr();
        Skill existing = findById(id);
        existing.setName(skill.getName());
        return skillRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        requireHr();
        skillRepository.deleteById(id);
    }

    private void requireHr() {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!"HR".equals(me.role())) {
            throw new BusinessException("Only HR can modify skills");
        }
    }
}
