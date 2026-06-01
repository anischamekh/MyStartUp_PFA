package tn.iteam.backend.service;

import java.util.List;
import tn.iteam.backend.entity.Training;
import tn.iteam.backend.entity.TrainingAttendance;

public interface TrainingService {
    List<Training> findAll();

    Training findById(Long id);

    Training save(Training training);

    Training update(Long id, Training training);

    void delete(Long id);

    List<TrainingAttendance> listAttendance(Long trainingId);

    TrainingAttendance addAttendance(Long trainingId, Long userId);

    TrainingAttendance setAttended(Long attendanceId, boolean attended);
}
