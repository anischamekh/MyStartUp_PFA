package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.Role;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.Skill;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.SkillRepository;

@ExtendWith(MockitoExtension.class)
class SkillServiceImplAuthTest {

    @Mock
    private SkillRepository skillRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private SkillServiceImpl skillService;

    @Test
    void findById_ok() {
        Skill s = new Skill();
        s.setName("Java");
        when(skillRepository.findById(1L)).thenReturn(Optional.of(s));
        assertEquals("Java", skillService.findById(1L).getName());
    }

    @Test
    void save_requiresHr() {
        User emp = new User();
        Role role = new Role();
        role.setName(RoleName.EMPLOYEE);
        emp.setRole(role);
        when(currentUserProvider.requireCurrentUser()).thenReturn(emp);
        assertThrows(BusinessException.class, () -> skillService.save(new Skill()));
    }
}
