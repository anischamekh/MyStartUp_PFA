package tn.iteam.backend.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.Training;
import tn.iteam.backend.entity.TrainingAttendance;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.TrainingAttendanceRepository;
import tn.iteam.backend.repository.TrainingRepository;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.service.TrainingService;

@Service
@Transactional
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainingAttendanceRepository trainingAttendanceRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public TrainingServiceImpl(
            TrainingRepository trainingRepository,
            TrainingAttendanceRepository trainingAttendanceRepository,
            UserRepository userRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.trainingRepository = trainingRepository;
        this.trainingAttendanceRepository = trainingAttendanceRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
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
        return trainingAttendanceRepository.findByTraining_IdOrderByIdAsc(trainingId);
    }

    @Override
    public TrainingAttendance addAttendance(Long trainingId, Long userId) {
        requireHr();
        Training training = findById(trainingId);
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("User not found"));
        if (trainingAttendanceRepository.findByTraining_IdAndUser_Id(trainingId, userId).isPresent()) {
            throw new BusinessException("Employee already assigned to this training");
        }
        TrainingAttendance row = new TrainingAttendance();
        row.setTraining(training);
        row.setUser(user);
        row.setAttended(false);
        return trainingAttendanceRepository.save(row);
    }

    @Override
    public TrainingAttendance setAttended(Long attendanceId, boolean attended) {
        requireHr();
        TrainingAttendance row = trainingAttendanceRepository
                .findById(attendanceId)
                .orElseThrow(() -> new BusinessException("Attendance record not found"));
        row.setAttended(attended);
        return trainingAttendanceRepository.save(row);
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
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.HR) {
            throw new BusinessException("Only HR can modify trainings");
        }
    }
}
