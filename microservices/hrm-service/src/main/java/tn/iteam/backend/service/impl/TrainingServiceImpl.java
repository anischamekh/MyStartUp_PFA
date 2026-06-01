package tn.iteam.backend.service.impl;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.Training;
import tn.iteam.backend.entity.TrainingAttendance;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.messaging.EventPublisher;
import tn.iteam.backend.repository.TrainingAttendanceRepository;
import tn.iteam.backend.repository.TrainingRepository;
import tn.iteam.backend.service.TrainingService;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.events.NotificationEvent;
import tn.iteam.common.security.JwtUserPrincipal;

@Service
@Transactional
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainingAttendanceRepository trainingAttendanceRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserSnapshotService userSnapshotService;
    private final EventPublisher eventPublisher;

    public TrainingServiceImpl(
            TrainingRepository trainingRepository,
            TrainingAttendanceRepository trainingAttendanceRepository,
            CurrentUserProvider currentUserProvider,
            UserSnapshotService userSnapshotService,
            EventPublisher eventPublisher
    ) {
        this.trainingRepository = trainingRepository;
        this.trainingAttendanceRepository = trainingAttendanceRepository;
        this.currentUserProvider = currentUserProvider;
        this.userSnapshotService = userSnapshotService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<Training> findAll() {
        return trainingRepository.findAll();
    }

    @Override
    public Training findById(Long id) {
        return trainingRepository.findById(id).orElseThrow(() -> new BusinessException("Training not found"));
    }

    @Override
    public Training save(Training training) {
        requireHr();
        if (training.getId() != null) {
            training.setId(null);
        }
        validate(training);
        return trainingRepository.save(training);
    }

    @Override
    public Training update(Long id, Training training) {
        requireHr();
        Training existing = findById(id);
        existing.setTitle(training.getTitle());
        existing.setDescription(training.getDescription());
        existing.setDate(training.getDate());
        validate(existing);
        return trainingRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        requireHr();
        Training t = findById(id);
        trainingAttendanceRepository.deleteAll(
                trainingAttendanceRepository.findByTraining_IdOrderByIdAsc(t.getId()));
        trainingRepository.deleteById(id);
    }

    @Override
    public List<TrainingAttendance> listAttendance(Long trainingId) {
        findById(trainingId);
        return enrichAttendance(trainingAttendanceRepository.findByTraining_IdOrderByIdAsc(trainingId));
    }

    @Override
    public TrainingAttendance addAttendance(Long trainingId, Long userId) {
        requireHr();
        Training training = findById(trainingId);
        userSnapshotService.requireById(userId);
        if (trainingAttendanceRepository.findByTraining_IdAndUserId(trainingId, userId).isPresent()) {
            throw new BusinessException("Employee already assigned to this training");
        }
        TrainingAttendance row = new TrainingAttendance();
        row.setTraining(training);
        row.setUserId(userId);
        row.setAttended(false);
        TrainingAttendance saved = trainingAttendanceRepository.save(row);

        eventPublisher.publishNotification(new NotificationEvent(
                "TRAINING_ASSIGNED",
                userId,
                "Training assigned",
                "You were assigned to training: " + training.getTitle(),
                Instant.now()
        ));
        return enrich(saved);
    }

    @Override
    public TrainingAttendance setAttended(Long attendanceId, boolean attended) {
        requireHr();
        TrainingAttendance row = trainingAttendanceRepository
                .findById(attendanceId)
                .orElseThrow(() -> new BusinessException("Attendance record not found"));
        row.setAttended(attended);
        return enrich(trainingAttendanceRepository.save(row));
    }

    private void validate(Training t) {
        if (t.getTitle() == null || t.getTitle().isBlank()) {
            throw new BusinessException("Training title is required");
        }
        if (t.getDate() == null) {
            throw new BusinessException("Training date is required");
        }
    }

    private void requireHr() {
        if (!"HR".equals(currentUserProvider.requireCurrentUser().role())) {
            throw new BusinessException("Only HR can modify trainings");
        }
    }

    private List<TrainingAttendance> enrichAttendance(List<TrainingAttendance> rows) {
        return rows.stream().map(this::enrich).toList();
    }

    private TrainingAttendance enrich(TrainingAttendance row) {
        row.enrichUser(userSnapshotService.findById(row.getUserId()).orElse(null));
        return row;
    }
}
