package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.EmployeeHrData;
import tn.iteam.backend.entity.LeaveRequest;
import tn.iteam.backend.messaging.EventPublisher;
import tn.iteam.backend.repository.EmployeeHrDataRepository;
import tn.iteam.backend.repository.LeaveRequestRepository;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private EmployeeHrDataRepository employeeHrDataRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private NotificationHelper notificationHelper;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private UserSnapshotService userSnapshotService;

    private LeaveRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new LeaveRequestServiceImpl(
                leaveRequestRepository,
                employeeHrDataRepository,
                currentUserProvider,
                notificationHelper,
                eventPublisher,
                userSnapshotService
        );
    }

    @Test
    void findMine_returnsEmployeeLeaves() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "john", "EMPLOYEE", "John"));
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployeeId(1L);
        when(leaveRequestRepository.findByEmployeeId(1L)).thenReturn(java.util.List.of(lr));
        when(userSnapshotService.mapByIds(any())).thenReturn(java.util.Map.of());

        assertEquals(1, service.findMine().size());
    }
}
