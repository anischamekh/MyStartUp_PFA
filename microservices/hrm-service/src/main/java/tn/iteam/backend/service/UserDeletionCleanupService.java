package tn.iteam.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.EmployeeDocument;
import tn.iteam.backend.repository.EmployeeDocumentRepository;
import tn.iteam.backend.repository.EmployeeSkillRepository;
import tn.iteam.backend.repository.EvaluationRepository;
import tn.iteam.backend.repository.LeaveRequestRepository;
import tn.iteam.backend.repository.NotificationRepository;
import tn.iteam.backend.repository.PayrollRepository;
import tn.iteam.backend.repository.TrainingAttendanceRepository;

@Service
@Transactional
public class UserDeletionCleanupService {

    private static final Logger log = LoggerFactory.getLogger(UserDeletionCleanupService.class);

    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationRepository notificationRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final PayrollRepository payrollRepository;
    private final EvaluationRepository evaluationRepository;
    private final TrainingAttendanceRepository trainingAttendanceRepository;
    private final UserSnapshotService userSnapshotService;

    public UserDeletionCleanupService(
            LeaveRequestRepository leaveRequestRepository,
            NotificationRepository notificationRepository,
            EmployeeDocumentRepository employeeDocumentRepository,
            EmployeeSkillRepository employeeSkillRepository,
            PayrollRepository payrollRepository,
            EvaluationRepository evaluationRepository,
            TrainingAttendanceRepository trainingAttendanceRepository,
            UserSnapshotService userSnapshotService
    ) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.notificationRepository = notificationRepository;
        this.employeeDocumentRepository = employeeDocumentRepository;
        this.employeeSkillRepository = employeeSkillRepository;
        this.payrollRepository = payrollRepository;
        this.evaluationRepository = evaluationRepository;
        this.trainingAttendanceRepository = trainingAttendanceRepository;
        this.userSnapshotService = userSnapshotService;
    }

    public void cleanup(Long userId) {
        leaveRequestRepository.clearManagerForUser(userId);
        leaveRequestRepository.deleteByEmployeeId(userId);
        notificationRepository.deleteByRecipientUserId(userId);
        evaluationRepository.deleteByEmployeeId(userId);
        evaluationRepository.deleteByEvaluatorId(userId);
        trainingAttendanceRepository.deleteByUserId(userId);
        employeeSkillRepository.deleteByUserId(userId);
        payrollRepository.deleteByUserId(userId);
        removeStoredFiles(userId);
        employeeDocumentRepository.deleteAll(employeeDocumentRepository.findByUserIdOrderByUploadDateDesc(userId));
        userSnapshotService.deleteUser(userId);
    }

    private void removeStoredFiles(Long userId) {
        List<EmployeeDocument> docs = employeeDocumentRepository.findByUserIdOrderByUploadDateDesc(userId);
        for (EmployeeDocument doc : docs) {
            if (doc.getFilePath() == null) {
                continue;
            }
            try {
                Files.deleteIfExists(Paths.get(doc.getFilePath()));
            } catch (IOException ex) {
                log.warn("Could not delete document file id={} path={}", doc.getId(), doc.getFilePath(), ex);
            }
        }
    }
}
