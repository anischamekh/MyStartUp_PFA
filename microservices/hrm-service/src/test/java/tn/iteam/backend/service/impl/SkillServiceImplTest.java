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
import tn.iteam.backend.entity.Skill;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.SkillRepository;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
class SkillServiceImplTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private SkillServiceImpl skillService;

    @Test
    void findAll_returnsCatalog() {
        when(skillRepository.findAll()).thenReturn(List.of(new Skill()));
        assertEquals(1, skillService.findAll().size());
    }

    @Test
    void findById_returnsSkill() {
        Skill skill = new Skill();
        skill.setId(1L);
        skill.setName("Java");
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        assertEquals("Java", skillService.findById(1L).getName());
    }

    @Test
    void save_requiresHr() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "u", "U", "EMPLOYEE"));
        assertThrows(BusinessException.class, () -> skillService.save(new Skill()));
    }

    @Test
    void save_hrPersistsSkill() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        Skill skill = new Skill();
        skill.setName("Docker");
        when(skillRepository.save(skill)).thenReturn(skill);
        assertEquals("Docker", skillService.save(skill).getName());
    }

    @Test
    void update_hrChangesName() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        Skill existing = new Skill();
        existing.setId(2L);
        existing.setName("Old");
        when(skillRepository.findById(2L)).thenReturn(Optional.of(existing));
        Skill patch = new Skill();
        patch.setName("Kubernetes");
        when(skillRepository.save(existing)).thenReturn(existing);
        assertEquals("Kubernetes", skillService.update(2L, patch).getName());
    }

    @Test
    void delete_hrRemovesSkill() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        skillService.delete(4L);
        org.mockito.Mockito.verify(skillRepository).deleteById(4L);
    }
}
