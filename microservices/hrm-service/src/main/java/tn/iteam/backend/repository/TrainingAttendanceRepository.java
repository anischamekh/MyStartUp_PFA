package tn.iteam.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.iteam.backend.entity.TrainingAttendance;

public interface TrainingAttendanceRepository extends JpaRepository<TrainingAttendance, Long> {

    List<TrainingAttendance> findByTraining_IdOrderByIdAsc(Long trainingId);

    Optional<TrainingAttendance> findByTraining_IdAndUserId(Long trainingId, Long userId);

    void deleteByUserId(Long userId);
}
