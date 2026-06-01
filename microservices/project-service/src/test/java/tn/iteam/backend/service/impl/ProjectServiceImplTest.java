package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.client.AuthServiceClient;
import tn.iteam.backend.entity.Project;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.messaging.EventPublisher;
import tn.iteam.backend.repository.ProjectRepository;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
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

    @Test
    void create_requiresManagerRole() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(2L, "emp", "Emp", "EMPLOYEE"));
        assertThrows(BusinessException.class, () -> projectService.create(new Project()));
    }
}
