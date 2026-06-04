package tn.iteam.backend.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.EmployeeSkill;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeSkillRepository;
import tn.iteam.backend.repository.SkillRepository;
import tn.iteam.backend.service.EmployeeSkillService;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.security.JwtUserPrincipal;

@Service
@Transactional
public class EmployeeSkillServiceImpl implements EmployeeSkillService {

    private final EmployeeSkillRepository employeeSkillRepository;
    private final SkillRepository skillRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserSnapshotService userSnapshotService;

    public EmployeeSkillServiceImpl(
            EmployeeSkillRepository employeeSkillRepository,
            SkillRepository skillRepository,
            CurrentUserProvider currentUserProvider,
            UserSnapshotService userSnapshotService
    ) {
        this.employeeSkillRepository = employeeSkillRepository;
        this.skillRepository = skillRepository;
        this.currentUserProvider = currentUserProvider;
        this.userSnapshotService = userSnapshotService;
    }

    @Override
    public List<EmployeeSkill> findVisible() {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (List.of("HR", "ADMIN").contains(me.role())) {
            return enrich(employeeSkillRepository.findAll());
        }
        return enrich(employeeSkillRepository.findByUserId(me.userId()));
    }

    @Override
    public List<EmployeeSkill> findForUser(Long userId) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!List.of("HR", "ADMIN").contains(me.role()) && !me.userId().equals(userId)) {
            throw new BusinessException("Not allowed to view skills for other users");
        }
        return enrich(employeeSkillRepository.findByUserId(userId));
    }

    @Override
    public EmployeeSkill upsert(EmployeeSkill employeeSkill) {
        requireHr();
        Long userId = employeeSkill.getUserId();
        if (userId == null && employeeSkill.getUser() != null && employeeSkill.getUser().get("id") != null) {
            userId = Long.valueOf(employeeSkill.getUser().get("id").toString());
        }
        userSnapshotService.requireById(userId);
        employeeSkill.setUserId(userId);
        employeeSkill.setSkill(skillRepository.findById(employeeSkill.getSkill().getId())
                .orElseThrow(() -> new BusinessException("Skill not found")));
        return enrich(employeeSkillRepository.save(employeeSkill));
    }

    @Override
    public void delete(Long id) {
        requireHr();
        employeeSkillRepository.deleteById(id);
    }

    private void requireHr() {
        if (!"HR".equals(currentUserProvider.requireCurrentUser().role())) {
            throw new BusinessException("Only HR can modify employee skills");
        }
    }

    private List<EmployeeSkill> enrich(List<EmployeeSkill> rows) {
        return rows.stream().map(this::enrich).toList();
    }

    private EmployeeSkill enrich(EmployeeSkill row) {
        row.enrichUser(userSnapshotService.findById(row.getUserId()).orElse(null));
        return row;
    }
}
