package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.Training;
import tn.iteam.backend.entity.TrainingAttendance;
import tn.iteam.backend.entity.UserSnapshot;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.messaging.EventPublisher;
import tn.iteam.backend.repository.TrainingAttendanceRepository;
import tn.iteam.backend.repository.TrainingRepository;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainingAttendanceRepository trainingAttendanceRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private UserSnapshotService userSnapshotService;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @Test
    void findAll_returnsTrainings() {
        when(trainingRepository.findAll()).thenReturn(List.of(new Training()));
        assertEquals(1, trainingService.findAll().size());
    }

    @Test
    void save_hrPersistsTraining() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        Training input = new Training();
        input.setTitle("Spring Boot");
        input.setDate(LocalDate.now());
        when(trainingRepository.save(input)).thenReturn(input);

        assertEquals("Spring Boot", trainingService.save(input).getTitle());
    }

    @Test
    void save_rejectsNonHr() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "e", "EMPLOYEE", "E"));
        Training input = new Training();
        input.setTitle("X");
        input.setDate(LocalDate.now());
        assertThrows(BusinessException.class, () -> trainingService.save(input));
    }

    @Test
    void addAttendance_hrAssignsEmployee() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        Training training = new Training();
        training.setId(2L);
        training.setTitle("DevOps");
        when(trainingRepository.findById(2L)).thenReturn(Optional.of(training));
        when(trainingAttendanceRepository.findByTraining_IdAndUserId(2L, 5L)).thenReturn(Optional.empty());
        UserSnapshot snap = new UserSnapshot();
        when(userSnapshotService.requireById(5L)).thenReturn(snap);
        when(userSnapshotService.findById(5L)).thenReturn(Optional.of(snap));

        TrainingAttendance saved = new TrainingAttendance();
        saved.setUserId(5L);
        when(trainingAttendanceRepository.save(any(TrainingAttendance.class))).thenReturn(saved);

        TrainingAttendance result = trainingService.addAttendance(2L, 5L);
        assertEquals(5L, result.getUserId());
        verify(eventPublisher).publishNotification(any());
    }

    @Test
    void findById_returnsTraining() {
        Training training = new Training();
        training.setId(4L);
        when(trainingRepository.findById(4L)).thenReturn(Optional.of(training));
        assertEquals(4L, trainingService.findById(4L).getId());
    }

    @Test
    void update_hrPersistsChanges() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        Training existing = new Training();
        existing.setId(5L);
        existing.setTitle("Old");
        when(trainingRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(trainingRepository.save(existing)).thenReturn(existing);

        Training patch = new Training();
        patch.setTitle("Updated");
        patch.setDate(LocalDate.now());
        assertEquals("Updated", trainingService.update(5L, patch).getTitle());
    }

    @Test
    void addAttendance_rejectsDuplicate() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        when(trainingRepository.findById(2L)).thenReturn(Optional.of(new Training()));
        when(trainingAttendanceRepository.findByTraining_IdAndUserId(2L, 5L))
                .thenReturn(Optional.of(new TrainingAttendance()));
        assertThrows(BusinessException.class, () -> trainingService.addAttendance(2L, 5L));
    }

    @Test
    void delete_hrRemovesTrainingAndAttendance() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        Training training = new Training();
        training.setId(3L);
        when(trainingRepository.findById(3L)).thenReturn(Optional.of(training));
        when(trainingAttendanceRepository.findByTraining_IdOrderByIdAsc(3L)).thenReturn(List.of());

        trainingService.delete(3L);

        verify(trainingRepository).deleteById(3L);
    }
}
