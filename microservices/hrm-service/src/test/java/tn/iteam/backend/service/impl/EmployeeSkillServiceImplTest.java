package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.EmployeeSkill;
import tn.iteam.backend.entity.Skill;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeSkillRepository;
import tn.iteam.backend.repository.SkillRepository;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
class EmployeeSkillServiceImplTest {

    @Mock
    private EmployeeSkillRepository employeeSkillRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private UserSnapshotService userSnapshotService;

    @InjectMocks
    private EmployeeSkillServiceImpl employeeSkillService;

    @Test
    void findVisible_employeeSeesOwnSkills() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(4L, "e", "EMPLOYEE", "E"));
        EmployeeSkill row = new EmployeeSkill();
        row.setUserId(4L);
        when(employeeSkillRepository.findByUserId(4L)).thenReturn(List.of(row));
        when(userSnapshotService.findById(4L)).thenReturn(Optional.empty());

        assertEquals(1, employeeSkillService.findVisible().size());
    }

    @Test
    void findForUser_idorDenied() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "e", "EMPLOYEE", "E"));
        assertThrows(BusinessException.class, () -> employeeSkillService.findForUser(99L));
    }

    @Test
    void findForUser_hrCanViewOthers() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        when(employeeSkillRepository.findByUserId(99L)).thenReturn(List.of());
        assertEquals(0, employeeSkillService.findForUser(99L).size());
    }

    @Test
    void upsert_hrRequiresSnapshotAndSkill() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        when(userSnapshotService.requireById(5L)).thenReturn(new tn.iteam.backend.entity.UserSnapshot());

        Skill skill = new Skill();
        skill.setId(2L);
        EmployeeSkill input = new EmployeeSkill();
        input.setUserId(5L);
        input.setSkill(skill);
        when(skillRepository.findById(2L)).thenReturn(Optional.of(skill));
        when(employeeSkillRepository.save(input)).thenReturn(input);
        when(userSnapshotService.findById(5L)).thenReturn(Optional.empty());

        assertEquals(5L, employeeSkillService.upsert(input).getUserId());
    }

    @Test
    void upsert_rejectsEmployee() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "e", "EMPLOYEE", "E"));
        EmployeeSkill input = new EmployeeSkill();
        input.setUserId(1L);
        input.setSkill(new Skill());
        assertThrows(BusinessException.class, () -> employeeSkillService.upsert(input));
    }
}
