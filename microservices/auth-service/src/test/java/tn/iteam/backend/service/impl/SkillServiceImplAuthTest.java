package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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

    @Test
    void findAll_delegatesToRepository() {
        when(skillRepository.findAll()).thenReturn(List.of());
        assertEquals(0, skillService.findAll().size());
    }

    @Test
    void findById_notFound() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> skillService.findById(99L));
    }

    @Test
    void save_hrSuccess_trimsName() {
        User hr = new User();
        Role role = new Role();
        role.setName(RoleName.HR);
        hr.setRole(role);
        when(currentUserProvider.requireCurrentUser()).thenReturn(hr);
        Skill input = new Skill();
        input.setName("  Java  ");
        when(skillRepository.findByNameIgnoreCase("Java")).thenReturn(Optional.empty());
        when(skillRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Skill saved = skillService.save(input);

        assertEquals("Java", saved.getName());
        verify(skillRepository).save(any());
    }

    @Test
    void save_rejectsBlankName() {
        User hr = new User();
        Role role = new Role();
        role.setName(RoleName.HR);
        hr.setRole(role);
        when(currentUserProvider.requireCurrentUser()).thenReturn(hr);
        Skill input = new Skill();
        input.setName("  ");
        assertThrows(BusinessException.class, () -> skillService.save(input));
    }

    @Test
    void delete_hrCanDelete() {
        User hr = new User();
        Role role = new Role();
        role.setName(RoleName.HR);
        hr.setRole(role);
        when(currentUserProvider.requireCurrentUser()).thenReturn(hr);
        skillService.delete(3L);
        verify(skillRepository).deleteById(3L);
    }

    @Test
    void update_hrRenamesSkill() {
        User hr = new User();
        Role role = new Role();
        role.setName(RoleName.HR);
        hr.setRole(role);
        when(currentUserProvider.requireCurrentUser()).thenReturn(hr);
        Skill existing = new Skill();
        existing.setId(2L);
        existing.setName("Old");
        when(skillRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(skillRepository.findByNameIgnoreCase("New")).thenReturn(Optional.empty());
        when(skillRepository.save(existing)).thenReturn(existing);

        Skill patch = new Skill();
        patch.setName("New");
        assertEquals("New", skillService.update(2L, patch).getName());
    }

    @Test
    void save_rejectsDuplicateName() {
        User hr = new User();
        Role role = new Role();
        role.setName(RoleName.HR);
        hr.setRole(role);
        when(currentUserProvider.requireCurrentUser()).thenReturn(hr);
        Skill input = new Skill();
        input.setName("Java");
        when(skillRepository.findByNameIgnoreCase("Java")).thenReturn(Optional.of(new Skill()));
        assertThrows(BusinessException.class, () -> skillService.save(input));
    }
}
