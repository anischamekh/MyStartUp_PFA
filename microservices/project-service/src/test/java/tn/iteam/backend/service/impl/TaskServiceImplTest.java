package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.client.AuthServiceClient;
import tn.iteam.backend.entity.Task;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.messaging.EventPublisher;
import tn.iteam.backend.repository.TaskRepository;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
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

    @Test
    void findById_notFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> taskService.findById(1L));
    }

    @Test
    void create_requiresTeamLeader() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "e", "E", "EMPLOYEE"));
        assertThrows(BusinessException.class, () -> taskService.create(new Task()));
    }
}
