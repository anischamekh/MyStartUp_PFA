package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.Role;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private TeamServiceImpl teamService;

    private User hrUser;

    @BeforeEach
    void setUp() {
        hrUser = new User();
        Role role = new Role();
        role.setName(RoleName.HR);
        hrUser.setRole(role);
    }

    @Test
    void findAll_returnsTeams() {
        when(teamRepository.findAll()).thenReturn(List.of(new Team()));
        assertEquals(1, teamService.findAll().size());
    }

    @Test
    void findById_notFound() {
        when(teamRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> teamService.findById(9L));
    }

    @Test
    void create_hrUser_savesTeam() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(hrUser);
        Team input = new Team();
        input.setName("Alpha");
        Team saved = new Team();
        saved.setId(1L);
        saved.setName("Alpha");
        when(teamRepository.save(any(Team.class))).thenReturn(saved);

        assertEquals("Alpha", teamService.create(input).getName());
    }

    @Test
    void update_hrUser_updatesFields() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(hrUser);
        Team existing = new Team();
        existing.setId(2L);
        existing.setName("Old");
        when(teamRepository.findById(2L)).thenReturn(Optional.of(existing));
        Team patch = new Team();
        patch.setName("New");
        when(teamRepository.save(existing)).thenReturn(existing);

        assertEquals("New", teamService.update(2L, patch).getName());
    }

    @Test
    void delete_hrUser_removesTeam() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(hrUser);
        teamService.delete(3L);
        verify(teamRepository).deleteById(3L);
    }

    @Test
    void delete_rejectsNonHr() {
        User employee = new User();
        Role role = new Role();
        role.setName(RoleName.EMPLOYEE);
        employee.setRole(role);
        when(currentUserProvider.requireCurrentUser()).thenReturn(employee);

        assertThrows(BusinessException.class, () -> teamService.delete(3L));
    }
}
