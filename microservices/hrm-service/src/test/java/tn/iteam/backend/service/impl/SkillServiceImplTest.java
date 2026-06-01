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
}
