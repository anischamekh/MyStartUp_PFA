package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tn.iteam.backend.client.AuthServiceClient;
import tn.iteam.backend.entity.Project;
import tn.iteam.backend.entity.UserSnapshot;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.messaging.EventPublisher;
import tn.iteam.backend.repository.ProjectRepository;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.dto.TeamSummaryDto;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private NotificationHelper notificationHelper;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private AuthServiceClient authServiceClient;
    @Mock
    private UserSnapshotService userSnapshotService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @BeforeEach
    void stubManagerSnapshot() {
        when(userSnapshotService.findById(any())).thenReturn(Optional.empty());
    }

    @Test
    void create_requiresManagerRole() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(2L, "emp", "EMPLOYEE", "Emp"));
        assertThrows(BusinessException.class, () -> projectService.create(new Project()));
    }

    @Test
    void findAll_returnsProjects() {
        Project p = new Project();
        p.setManagerUserId(1L);
        p.setTeamIds(Set.of());
        when(projectRepository.findAll()).thenReturn(List.of(p));
        assertEquals(1, projectService.findAll().size());
    }

    @Test
    void create_managerPersistsProject() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(3L, "mgr", "MANAGER", "Mgr"));
        Project input = new Project();
        input.setName("Portal");
        input.setTeamIds(Set.of(7L));
        when(authServiceClient.getTeam(7L)).thenReturn(new TeamSummaryDto(7L, "Team A", 10L));

        Project saved = new Project();
        saved.setId(1L);
        saved.setName("Portal");
        saved.setManagerUserId(3L);
        saved.setTeamIds(Set.of(7L));
        when(projectRepository.save(any(Project.class))).thenReturn(saved);

        UserSnapshot mgr = new UserSnapshot();
        mgr.setId(3L);
        when(userSnapshotService.findById(3L)).thenReturn(Optional.of(mgr));

        Project result = projectService.create(input);
        assertEquals("Portal", result.getName());
        assertEquals(3L, result.getManagerUserId());
        verify(eventPublisher).publishProjectCreated(any());
        verify(notificationHelper).notify(any(), any(), any());
    }

    @Test
    void findById_notFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> projectService.findById(99L));
    }

    @Test
    void update_rejectsNonOwnerManager() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(3L, "mgr", "MANAGER", "Mgr"));
        Project existing = new Project();
        existing.setId(1L);
        existing.setManagerUserId(99L);
        existing.setTeamIds(Set.of());
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userSnapshotService.findById(any())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> projectService.update(1L, new Project()));
    }

    @Test
    void update_managerUpdatesOwnProject() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(3L, "mgr", "MANAGER", "Mgr"));
        Project existing = new Project();
        existing.setId(5L);
        existing.setManagerUserId(3L);
        existing.setName("Old");
        existing.setTeamIds(Set.of(7L));
        when(projectRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(authServiceClient.getTeam(7L)).thenReturn(new TeamSummaryDto(7L, "Team A", 10L));
        when(projectRepository.save(existing)).thenReturn(existing);
        when(userSnapshotService.findById(3L)).thenReturn(Optional.of(new UserSnapshot()));

        Project patch = new Project();
        patch.setName("Updated");
        patch.setTeamIds(Set.of(7L));
        assertEquals("Updated", projectService.update(5L, patch).getName());
    }

    @Test
    void delete_managerRemovesOwnProject() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(3L, "mgr", "MANAGER", "Mgr"));
        Project existing = new Project();
        existing.setId(2L);
        existing.setManagerUserId(3L);
        existing.setTeamIds(Set.of());
        when(projectRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(userSnapshotService.findById(any())).thenReturn(Optional.empty());

        projectService.delete(2L);
        verify(projectRepository).deleteById(2L);
    }
}
