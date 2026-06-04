package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tn.iteam.backend.client.AuthServiceClient;
import tn.iteam.backend.entity.Task;
import tn.iteam.backend.entity.TaskStatus;
import tn.iteam.backend.entity.UserSnapshot;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.messaging.EventPublisher;
import tn.iteam.backend.repository.TaskRepository;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.dto.UserSummaryDto;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private NotificationHelper notificationHelper;
    @Mock
    private AuthServiceClient authServiceClient;
    @Mock
    private UserSnapshotService userSnapshotService;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private TaskServiceImpl taskService;

    @BeforeEach
    void stubEnrichment() {
        when(userSnapshotService.mapByIds(any())).thenReturn(Map.of());
    }

    @Test
    void findById_notFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> taskService.findById(1L));
    }

    @Test
    void create_requiresTeamLeader() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "e", "EMPLOYEE", "E"));
        assertThrows(BusinessException.class, () -> taskService.create(new Task()));
    }

    @Test
    void findMyTasks_returnsAssignedTasks() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(5L, "e", "EMPLOYEE", "E"));
        Task task = new Task();
        task.setAssignedToUserId(5L);
        task.setCreatedByUserId(10L);
        when(taskRepository.findByAssignedToUserId(5L)).thenReturn(List.of(task));

        assertEquals(1, taskService.findMyTasks().size());
    }

    @Test
    void updateProgress_assigneeUpdatesTask() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(5L, "e", "EMPLOYEE", "E"));
        Task task = new Task();
        task.setId(1L);
        task.setAssignedToUserId(5L);
        task.setCreatedByUserId(10L);
        task.setTitle("Task A");
        task.setProgress(20);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task updated = taskService.updateProgress(1L, 50);
        assertEquals(50, updated.getProgress());
        assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void updateProgress_deniedForNonAssignee() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "x", "EMPLOYEE", "X"));
        Task task = new Task();
        task.setId(1L);
        task.setAssignedToUserId(99L);
        task.setCreatedByUserId(10L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(BusinessException.class, () -> taskService.updateProgress(1L, 50));
    }

    @Test
    void validate_teamLeaderSetsValidated() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(10L, "lead", "TEAM_LEADER", "Lead"));
        Task task = new Task();
        task.setId(2L);
        task.setAssignedToUserId(5L);
        task.setCreatedByUserId(10L);
        task.setTitle("Validate me");
        task.setProgress(80);
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task validated = taskService.validate(2L);
        assertEquals(TaskStatus.VALIDATED, validated.getStatus());
        assertEquals(80, validated.getValidatedProgressFloor());
        verify(eventPublisher).publishTaskValidated(any());
    }

    @Test
    void update_teamLeaderReassignsWithinTeam() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(10L, "lead", "TEAM_LEADER", "Lead"));
        Task existing = new Task();
        existing.setId(3L);
        existing.setAssignedToUserId(5L);
        existing.setCreatedByUserId(10L);
        existing.setTitle("Old");
        existing.setProgress(10);
        when(taskRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(authServiceClient.getUser(10L)).thenReturn(new UserSummaryDto(10L, "l", "L", "l@test.com", "TEAM_LEADER", 7L, "T"));
        when(authServiceClient.getUser(6L)).thenReturn(new UserSummaryDto(6L, "a", "A", "a@test.com", "EMPLOYEE", 7L, "T"));
        when(taskRepository.save(existing)).thenReturn(existing);

        Task patch = new Task();
        patch.setTitle("New title");
        patch.setAssignedToUserId(6L);

        Task updated = taskService.update(3L, patch);
        assertEquals("New title", updated.getTitle());
        assertEquals(6L, updated.getAssignedToUserId());
    }

    @Test
    void findAll_returnsEnrichedTasks() {
        Task task = new Task();
        task.setAssignedToUserId(5L);
        task.setCreatedByUserId(10L);
        when(taskRepository.findAll()).thenReturn(List.of(task));
        assertEquals(1, taskService.findAll().size());
    }

    @Test
    void create_teamLeaderSavesTask() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(10L, "lead", "TEAM_LEADER", "Lead"));
        Task input = new Task();
        input.setTitle("New");
        input.setAssignedToUserId(6L);
        when(authServiceClient.getUser(10L)).thenReturn(new UserSummaryDto(10L, "l", "L", "l@test.com", "TEAM_LEADER", 7L, "T"));
        when(authServiceClient.getUser(6L)).thenReturn(new UserSummaryDto(6L, "a", "A", "a@test.com", "EMPLOYEE", 7L, "T"));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Task created = taskService.create(input);
        assertEquals("New", created.getTitle());
        verify(notificationHelper).notify(any(), any(), any());
    }

    @Test
    void update_rejectsEditOnFullyValidatedTask() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(10L, "lead", "TEAM_LEADER", "Lead"));
        Task existing = new Task();
        existing.setId(6L);
        existing.setAssignedToUserId(5L);
        existing.setCreatedByUserId(10L);
        existing.setStatus(TaskStatus.VALIDATED);
        existing.setProgress(100);
        when(taskRepository.findById(6L)).thenReturn(Optional.of(existing));

        assertThrows(BusinessException.class, () -> taskService.update(6L, new Task()));
    }

    @Test
    void findById_returnsTask() {
        Task task = new Task();
        task.setId(7L);
        task.setAssignedToUserId(5L);
        task.setCreatedByUserId(10L);
        when(taskRepository.findById(7L)).thenReturn(Optional.of(task));
        assertEquals(7L, taskService.findById(7L).getId());
    }

    @Test
    void delete_teamLeaderRemovesTask() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(10L, "lead", "TEAM_LEADER", "Lead"));
        Task task = new Task();
        task.setId(4L);
        task.setAssignedToUserId(5L);
        task.setCreatedByUserId(10L);
        when(taskRepository.findById(4L)).thenReturn(Optional.of(task));
        taskService.delete(4L);
        verify(taskRepository).deleteById(4L);
    }
}
