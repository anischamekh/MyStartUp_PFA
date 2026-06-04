package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tn.iteam.backend.entity.EmployeeHrData;
import tn.iteam.backend.entity.LeaveRequest;
import tn.iteam.backend.entity.LeaveStatus;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.messaging.EventPublisher;
import tn.iteam.backend.repository.EmployeeHrDataRepository;
import tn.iteam.backend.repository.LeaveRequestRepository;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        when(userSnapshotService.mapByIds(any())).thenReturn(Map.of());
    }

    @Test
    void findMine_returnsEmployeeLeaves() {
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(1L, "john", "EMPLOYEE", "John"));
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployeeId(1L);
        when(leaveRequestRepository.findByEmployeeId(1L)).thenReturn(List.of(lr));

        assertEquals(1, service.findMine().size());
    }

    @Test
    void findAll_deniedForEmployee() {
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(1L, "john", "EMPLOYEE", "John"));
        assertThrows(BusinessException.class, () -> service.findAll());
    }

    @Test
    void findAll_allowedForHr() {
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(2L, "hr", "HR", "HR User"));
        when(leaveRequestRepository.findAll()).thenReturn(List.of());

        assertEquals(0, service.findAll().size());
    }

    @Test
    void findById_idorDeniedForOtherEmployee() {
        LeaveRequest lr = new LeaveRequest();
        lr.setId(10L);
        lr.setEmployeeId(99L);
        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(lr));
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(1L, "john", "EMPLOYEE", "John"));

        assertThrows(BusinessException.class, () -> service.findById(10L));
    }

    @Test
    void request_persistsPendingLeave() {
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(5L, "emp", "EMPLOYEE", "Emp"));

        LeaveRequest input = new LeaveRequest();
        input.setStartDate(LocalDate.of(2026, 6, 1));
        input.setEndDate(LocalDate.of(2026, 6, 3));

        EmployeeHrData hrData = new EmployeeHrData();
        hrData.setUserId(5L);
        hrData.setRemainingLeaveDays(30);
        when(employeeHrDataRepository.findById(5L)).thenReturn(Optional.of(hrData));

        LeaveRequest saved = new LeaveRequest();
        saved.setId(1L);
        saved.setEmployeeId(5L);
        saved.setDays(3);
        saved.setStatus(LeaveStatus.PENDING);
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(saved);

        LeaveRequest result = service.request(input);
        assertEquals(LeaveStatus.PENDING, result.getStatus());
        assertEquals(5L, result.getEmployeeId());
    }

    @Test
    void approve_managerUpdatesBalance() {
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(20L, "mgr", "MANAGER", "Manager"));

        LeaveRequest lr = new LeaveRequest();
        lr.setId(8L);
        lr.setEmployeeId(5L);
        lr.setDays(2);
        lr.setStatus(LeaveStatus.PENDING);
        when(leaveRequestRepository.findById(8L)).thenReturn(Optional.of(lr));

        EmployeeHrData hrData = new EmployeeHrData();
        hrData.setUserId(5L);
        hrData.setRemainingLeaveDays(10);
        when(employeeHrDataRepository.findById(5L)).thenReturn(Optional.of(hrData));
        when(leaveRequestRepository.save(lr)).thenReturn(lr);

        LeaveRequest approved = service.approve(8L);
        assertEquals(LeaveStatus.APPROVED, approved.getStatus());
        verify(eventPublisher).publishLeaveApproved(any());
    }

    @Test
    void findById_employeeCanViewOwn() {
        LeaveRequest lr = new LeaveRequest();
        lr.setId(3L);
        lr.setEmployeeId(1L);
        when(leaveRequestRepository.findById(3L)).thenReturn(Optional.of(lr));
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(1L, "john", "EMPLOYEE", "John"));

        assertEquals(3L, service.findById(3L).getId());
    }

    @Test
    void findForUser_hrCanList() {
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(2L, "hr", "HR", "HR"));
        when(leaveRequestRepository.findByEmployeeId(5L)).thenReturn(List.of());
        assertEquals(0, service.findForUser(5L).size());
    }

    @Test
    void request_rejectsInvalidDateRange() {
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(5L, "emp", "EMPLOYEE", "Emp"));
        LeaveRequest input = new LeaveRequest();
        input.setStartDate(LocalDate.of(2026, 6, 10));
        input.setEndDate(LocalDate.of(2026, 6, 1));
        assertThrows(BusinessException.class, () -> service.request(input));
    }

    @Test
    void reject_managerSetsRejected() {
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(20L, "mgr", "HR", "HR User"));

        LeaveRequest lr = new LeaveRequest();
        lr.setId(9L);
        lr.setEmployeeId(5L);
        lr.setStatus(LeaveStatus.PENDING);
        when(leaveRequestRepository.findById(9L)).thenReturn(Optional.of(lr));
        when(leaveRequestRepository.save(lr)).thenReturn(lr);

        assertEquals(LeaveStatus.REJECTED, service.reject(9L).getStatus());
    }
}
