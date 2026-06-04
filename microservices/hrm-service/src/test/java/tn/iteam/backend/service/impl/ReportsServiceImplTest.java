package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.LeaveRequest;
import tn.iteam.backend.entity.LeaveStatus;
import tn.iteam.backend.entity.UserSnapshot;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.LeaveRequestRepository;
import tn.iteam.backend.repository.UserSnapshotRepository;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
class ReportsServiceImplTest {

    @Mock
    private UserSnapshotRepository userSnapshotRepository;
    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ReportsServiceImpl reportsService;

    @Test
    void summary_managerBuildsReport() {
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(1L, "m", "MANAGER", "Mgr"));
        UserSnapshot snap = new UserSnapshot();
        snap.setTeamName("Alpha");
        when(userSnapshotRepository.findAll()).thenReturn(List.of(snap));
        LeaveRequest lr = new LeaveRequest();
        lr.setStatus(LeaveStatus.PENDING);
        when(leaveRequestRepository.findAll()).thenReturn(List.of(lr));

        var dto = reportsService.summary();
        assertEquals(1L, dto.getEmployeesByTeam().get("Alpha"));
        assertEquals(1L, dto.getLeavesByStatus().get("PENDING"));
    }

    @Test
    void summary_rejectsEmployee() {
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(1L, "e", "EMPLOYEE", "E"));
        assertThrows(BusinessException.class, () -> reportsService.summary());
    }

    @Test
    void exportSummaryPdf_returnsBytes() {
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        when(userSnapshotRepository.findAll()).thenReturn(List.of());
        when(leaveRequestRepository.findAll()).thenReturn(List.of());
        assert reportsService.exportSummaryPdf().length > 0;
    }
}
