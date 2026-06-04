package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tn.iteam.backend.client.AuthServiceClient;
import tn.iteam.backend.entity.Evaluation;
import tn.iteam.backend.entity.UserSnapshot;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EvaluationRepository;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.dto.TeamSummaryDto;
import tn.iteam.common.dto.UserSummaryDto;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EvaluationServiceImplTest {

    @Mock
    private EvaluationRepository evaluationRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private NotificationHelper notificationHelper;
    @Mock
    private UserSnapshotService userSnapshotService;
    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private EvaluationServiceImpl evaluationService;

    @BeforeEach
    void stubSnapshots() {
        when(userSnapshotService.mapByIds(any())).thenReturn(Map.of());
    }

    @Test
    void findVisible_hrSeesAll() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        when(evaluationRepository.findAll()).thenReturn(List.of());
        assertEquals(0, evaluationService.findVisible().size());
    }

    @Test
    void findVisible_employeeDenied() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "e", "EMPLOYEE", "E"));
        when(evaluationRepository.findAll()).thenReturn(List.of());
        assertThrows(BusinessException.class, () -> evaluationService.findVisible());
    }

    @Test
    void create_hrPersistsEvaluation() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(2L, "hr", "HR", "HR"));
        when(userSnapshotService.requireById(5L)).thenReturn(new UserSnapshot());

        Evaluation input = new Evaluation();
        input.setEmployeeId(5L);
        input.setScore(80);
        input.setTechnicalSkill(70);
        input.setTeamwork(75);
        input.setDeadlineRespect(85);

        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userSnapshotService.mapByIds(any())).thenReturn(Map.of());

        Evaluation saved = evaluationService.create(input);
        assertEquals(5L, saved.getEmployeeId());
        assertEquals(2L, saved.getEvaluatorId());
        verify(notificationHelper).notify(any(), any(), any());
    }

    @Test
    void findById_hrCanAccess() {
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(2L, "hr", "HR", "HR"));
        Evaluation ev = new Evaluation();
        ev.setId(1L);
        ev.setEmployeeId(5L);
        ev.setEvaluatorId(2L);
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(ev));
        when(userSnapshotService.mapByIds(any())).thenReturn(Map.of());

        assertEquals(1L, evaluationService.findById(1L).getId());
    }

    @Test
    void update_hrPersistsChanges() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(2L, "hr", "HR", "HR"));
        Evaluation existing = new Evaluation();
        existing.setId(3L);
        existing.setEmployeeId(5L);
        existing.setEvaluatorId(2L);
        existing.setScore(70);
        when(evaluationRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(evaluationRepository.save(existing)).thenReturn(existing);
        when(userSnapshotService.mapByIds(any())).thenReturn(Map.of());

        Evaluation patch = new Evaluation();
        patch.setScore(85);
        patch.setTechnicalSkill(80);
        patch.setTeamwork(80);
        patch.setDeadlineRespect(80);
        Evaluation updated = evaluationService.update(3L, patch);
        assertEquals(85, updated.getScore());
    }

    @Test
    void delete_hrRemovesEvaluation() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(2L, "hr", "HR", "HR"));
        Evaluation existing = new Evaluation();
        existing.setId(4L);
        existing.setEmployeeId(5L);
        existing.setEvaluatorId(2L);
        when(evaluationRepository.findById(4L)).thenReturn(Optional.of(existing));
        evaluationService.delete(4L);
        verify(evaluationRepository).delete(existing);
    }

    @Test
    void create_employeeRoleRejected() {
        when(currentUserProvider.requireCurrentUser())
                .thenReturn(new JwtUserPrincipal(1L, "e", "EMPLOYEE", "E"));

        Evaluation input = new Evaluation();
        input.setEmployeeId(5L);
        input.setScore(70);
        input.setTechnicalSkill(70);
        input.setTeamwork(70);
        input.setDeadlineRespect(70);

        assertThrows(BusinessException.class, () -> evaluationService.create(input));
    }
}
