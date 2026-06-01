package tn.iteam.backend.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.EmployeeSkill;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeSkillRepository;
import tn.iteam.backend.repository.SkillRepository;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.service.EmployeeSkillService;

@Service
@Transactional
public class EmployeeSkillServiceImpl implements EmployeeSkillService {

    private final EmployeeSkillRepository employeeSkillRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final CurrentUserProvider currentUserProvider;

    public EmployeeSkillServiceImpl(
            EmployeeSkillRepository employeeSkillRepository,
            UserRepository userRepository,
            SkillRepository skillRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.employeeSkillRepository = employeeSkillRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public List<EmployeeSkill> findVisible() {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn == RoleName.HR || rn == RoleName.ADMIN) {
            return employeeSkillRepository.findAll();
        }
        return employeeSkillRepository.findByUser_Id(me.getId());
    }

    @Override
    public List<EmployeeSkill> findForUser(Long userId) {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.HR && rn != RoleName.ADMIN && !me.getId().equals(userId)) {
            throw new BusinessException("Not allowed to view skills for this user");
        }
        return employeeSkillRepository.findByUser_Id(userId);
    }

    @Override
    public EmployeeSkill upsert(EmployeeSkill employeeSkill) {
        requireHr();
        if (employeeSkill.getUser() == null || employeeSkill.getUser().getId() == null) {
            throw new BusinessException("user is required");
        }
        if (employeeSkill.getSkill() == null || employeeSkill.getSkill().getId() == null) {
            throw new BusinessException("skill is required");
        }
        User u = userRepository.findById(employeeSkill.getUser().getId())
                .orElseThrow(() -> new BusinessException("User not found"));
        var sk = skillRepository.findById(employeeSkill.getSkill().getId())
                .orElseThrow(() -> new BusinessException("Skill not found"));

        EmployeeSkill row = employeeSkillRepository
                .findByUser_IdAndSkill_Id(u.getId(), sk.getId())
                .orElseGet(EmployeeSkill::new);
        row.setUser(u);
        row.setSkill(sk);
        if (employeeSkill.getLevel() != null) {
            row.setLevel(employeeSkill.getLevel());
        }
        return employeeSkillRepository.save(row);
    }

    @Override
    public void delete(Long id) {
        requireHr();
        employeeSkillRepository.deleteById(id);
    }

    private void requireHr() {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.HR) {
            throw new BusinessException("Only HR can modify employee skills");
        }
    }
}
