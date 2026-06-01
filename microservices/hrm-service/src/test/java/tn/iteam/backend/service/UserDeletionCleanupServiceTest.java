package tn.iteam.backend.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.repository.EmployeeDocumentRepository;
import tn.iteam.backend.repository.EmployeeSkillRepository;
import tn.iteam.backend.repository.EvaluationRepository;
import tn.iteam.backend.repository.LeaveRequestRepository;
import tn.iteam.backend.repository.NotificationRepository;
import tn.iteam.backend.repository.PayrollRepository;
import tn.iteam.backend.repository.TrainingAttendanceRepository;

@ExtendWith(MockitoExtension.class)
class UserDeletionCleanupServiceTest {

    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private EmployeeDocumentRepository employeeDocumentRepository;
    @Mock private EmployeeSkillRepository employeeSkillRepository;
    @Mock private PayrollRepository payrollRepository;
    @Mock private EvaluationRepository evaluationRepository;
    @Mock private TrainingAttendanceRepository trainingAttendanceRepository;
    @Mock private UserSnapshotService userSnapshotService;

    @InjectMocks
    private UserDeletionCleanupService service;

    @Test
    void cleanup_invokesRepositories() {
        service.cleanup(9L);
        verify(leaveRequestRepository).clearManagerForUser(9L);
        verify(leaveRequestRepository).deleteByEmployeeId(9L);
        verify(userSnapshotService).deleteUser(9L);
    }
}
