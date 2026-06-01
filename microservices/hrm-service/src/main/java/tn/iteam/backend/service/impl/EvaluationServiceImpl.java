package tn.iteam.backend.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.client.AuthServiceClient;
import tn.iteam.backend.entity.Evaluation;
import tn.iteam.backend.entity.NotificationType;
import tn.iteam.backend.entity.UserSnapshot;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EvaluationRepository;
import tn.iteam.backend.service.EvaluationService;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.dto.TeamSummaryDto;
import tn.iteam.common.dto.UserSummaryDto;
import tn.iteam.common.security.JwtUserPrincipal;

@Service
@Transactional
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationHelper notificationHelper;
    private final UserSnapshotService userSnapshotService;
    private final AuthServiceClient authServiceClient;

    public EvaluationServiceImpl(
            EvaluationRepository evaluationRepository,
            CurrentUserProvider currentUserProvider,
            NotificationHelper notificationHelper,
            UserSnapshotService userSnapshotService,
            AuthServiceClient authServiceClient
    ) {
        this.evaluationRepository = evaluationRepository;
        this.currentUserProvider = currentUserProvider;
        this.notificationHelper = notificationHelper;
        this.userSnapshotService = userSnapshotService;
        this.authServiceClient = authServiceClient;
    }

    @Override
    public List<Evaluation> findVisible() {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        List<Evaluation> all = enrichAll(evaluationRepository.findAll());
        return switch (me.role()) {
            case "HR", "ADMIN", "MANAGER" -> all;
            case "TEAM_LEADER" -> {
                TeamSummaryDto led = findLedTeam(me.userId());
                yield all.stream().filter(ev -> isInTeam(ev.getEmployeeId(), led.id())).toList();
            }
            default -> throw new BusinessException("Not allowed to view evaluations");
        };
    }

    @Override
    public Evaluation findById(Long id) {
        Evaluation ev = enrich(evaluationRepository.findById(id).orElseThrow(() -> new BusinessException("Evaluation not found")));
        assertCanAccess(ev);
        return ev;
    }

    @Override
    public Evaluation create(Evaluation evaluation) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!List.of("HR", "MANAGER", "TEAM_LEADER").contains(me.role())) {
            throw new BusinessException("Only HR, managers, or team leaders can create evaluations");
        }
        if (evaluation.getId() != null) {
            evaluation.setId(null);
        }
        Long employeeId = extractEmployeeId(evaluation);
        userSnapshotService.requireById(employeeId);
        validateScore(evaluation.getScore());
        validateDimensions(evaluation);
        if ("TEAM_LEADER".equals(me.role())) {
            assertLeaderEvaluatesTeamMember(me.userId(), employeeId);
        }
        evaluation.setEmployeeId(employeeId);
        evaluation.setEvaluatorId(me.userId());
        if (evaluation.getDate() == null) {
            evaluation.setDate(java.time.LocalDate.now());
        }
        Evaluation saved = enrich(evaluationRepository.save(evaluation));

        notificationHelper.notify(
                employeeId,
                NotificationType.EVALUATION_CREATED,
                "New performance evaluation was created for you"
        );
        return saved;
    }

    @Override
    public Evaluation update(Long id, Evaluation evaluation) {
        Evaluation existing = findById(id);
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if ("HR".equals(me.role()) || "MANAGER".equals(me.role())
                || ("TEAM_LEADER".equals(me.role()) && me.userId().equals(existing.getEvaluatorId()))) {
            validateScore(evaluation.getScore());
            validateDimensions(evaluation);
            existing.setScore(evaluation.getScore());
            applyDimensions(existing, evaluation);
            if (evaluation.getComment() != null) {
                existing.setComment(evaluation.getComment());
            }
            if (evaluation.getDate() != null) {
                existing.setDate(evaluation.getDate());
            }
            return enrich(evaluationRepository.save(existing));
        }
        throw new BusinessException("Not allowed to update this evaluation");
    }

    @Override
    public void delete(Long id) {
        Evaluation existing = evaluationRepository.findById(id).orElseThrow(() -> new BusinessException("Evaluation not found"));
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if ("HR".equals(me.role()) || "MANAGER".equals(me.role())
                || ("TEAM_LEADER".equals(me.role()) && me.userId().equals(existing.getEvaluatorId()))) {
            evaluationRepository.delete(existing);
            return;
        }
        throw new BusinessException("Not allowed to delete this evaluation");
    }

    private void assertCanAccess(Evaluation ev) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (List.of("HR", "ADMIN", "MANAGER").contains(me.role())) {
            return;
        }
        if ("TEAM_LEADER".equals(me.role())) {
            TeamSummaryDto led = findLedTeam(me.userId());
            if (isInTeam(ev.getEmployeeId(), led.id())) {
                return;
            }
        }
        throw new BusinessException("Not allowed to access this evaluation");
    }

    private TeamSummaryDto findLedTeam(Long leaderUserId) {
        return authServiceClient.getTeams().stream()
                .filter(t -> leaderUserId.equals(t.teamLeaderUserId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("You are not a team leader of any team"));
    }

    private void assertLeaderEvaluatesTeamMember(Long leaderUserId, Long employeeId) {
        TeamSummaryDto led = findLedTeam(leaderUserId);
        UserSummaryDto employee = authServiceClient.getUser(employeeId);
        if (employee.teamId() == null || !employee.teamId().equals(led.id())) {
            throw new BusinessException("You can only evaluate members of your team");
        }
    }

    private boolean isInTeam(Long employeeId, Long teamId) {
        return userSnapshotService.findById(employeeId)
                .map(s -> teamId.equals(s.getTeamId()))
                .orElse(false);
    }

    private Long extractEmployeeId(Evaluation evaluation) {
        if (evaluation.getEmployeeId() != null) {
            return evaluation.getEmployeeId();
        }
        if (evaluation.getEmployee() != null && evaluation.getEmployee().get("id") != null) {
            return Long.valueOf(evaluation.getEmployee().get("id").toString());
        }
        throw new BusinessException("employee is required");
    }

    private List<Evaluation> enrichAll(List<Evaluation> items) {
        List<Long> ids = items.stream()
                .flatMap(e -> java.util.stream.Stream.of(e.getEmployeeId(), e.getEvaluatorId()))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, UserSnapshot> map = userSnapshotService.mapByIds(ids);
        return items.stream().map(e -> enrich(e, map)).toList();
    }

    private Evaluation enrich(Evaluation evaluation) {
        Map<Long, UserSnapshot> map = userSnapshotService.mapByIds(
                java.util.stream.Stream.of(evaluation.getEmployeeId(), evaluation.getEvaluatorId())
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .toList()
        );
        return enrich(evaluation, map);
    }

    private Evaluation enrich(Evaluation evaluation, Map<Long, UserSnapshot> map) {
        evaluation.enrichEmployee(map.get(evaluation.getEmployeeId()));
        evaluation.enrichEvaluator(map.get(evaluation.getEvaluatorId()));
        return evaluation;
    }

    private static void validateScore(int score) {
        if (score < 0 || score > 100) {
            throw new BusinessException("score must be between 0 and 100");
        }
    }

    private static void validateDimensions(Evaluation e) {
        checkDim(e.getTechnicalSkill(), "technicalSkill");
        checkDim(e.getTeamwork(), "teamwork");
        checkDim(e.getDeadlineRespect(), "deadlineRespect");
    }

    private static void checkDim(Integer v, String field) {
        if (v != null && (v < 0 || v > 100)) {
            throw new BusinessException(field + " must be between 0 and 100");
        }
    }

    private static void applyDimensions(Evaluation target, Evaluation source) {
        if (source.getTechnicalSkill() != null) {
            target.setTechnicalSkill(source.getTechnicalSkill());
        }
        if (source.getTeamwork() != null) {
            target.setTeamwork(source.getTeamwork());
        }
        if (source.getDeadlineRespect() != null) {
            target.setDeadlineRespect(source.getDeadlineRespect());
        }
    }
}
