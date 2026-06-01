package tn.iteam.backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.Evaluation;
import tn.iteam.backend.entity.NotificationType;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.repository.EvaluationRepository;
import tn.iteam.backend.repository.TeamRepository;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.service.EvaluationService;

@Service
@Transactional
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final TeamRepository teamRepository;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationHelper notificationHelper;

    public EvaluationServiceImpl(
            EvaluationRepository evaluationRepository,
            UserRepository userRepository,
            EmployeeProfileRepository employeeProfileRepository,
            TeamRepository teamRepository,
            CurrentUserProvider currentUserProvider,
            NotificationHelper notificationHelper
    ) {
        this.evaluationRepository = evaluationRepository;
        this.userRepository = userRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.teamRepository = teamRepository;
        this.currentUserProvider = currentUserProvider;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public List<Evaluation> findVisible() {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn == RoleName.HR || rn == RoleName.ADMIN || rn == RoleName.MANAGER) {
            return evaluationRepository.findAll();
        }
        if (rn == RoleName.TEAM_LEADER) {
            Team led = teamRepository
                    .findByTeamLeader_Id(me.getId())
                    .orElseThrow(() -> new BusinessException("You are not a team leader of any team"));
            Long teamId = led.getId();
            return evaluationRepository.findAll().stream()
                    .filter(ev -> isEmployeeInTeam(ev.getEmployee(), teamId))
                    .collect(Collectors.toList());
        }
        throw new BusinessException("Not allowed to view evaluations");
    }

    @Override
    public Evaluation findById(Long id) {
        Evaluation ev = evaluationRepository.findById(id).orElseThrow(() -> new BusinessException("Evaluation not found"));
        assertCanAccess(ev);
        return ev;
    }

    @Override
    public Evaluation create(Evaluation evaluation) {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.HR && rn != RoleName.MANAGER && rn != RoleName.TEAM_LEADER) {
            throw new BusinessException("Only HR, managers, or team leaders can create evaluations");
        }
        if (evaluation.getId() != null) {
            evaluation.setId(null);
        }
        if (evaluation.getEmployee() == null || evaluation.getEmployee().getId() == null) {
            throw new BusinessException("employee is required");
        }
        User employee = userRepository
                .findById(evaluation.getEmployee().getId())
                .orElseThrow(() -> new BusinessException("Employee not found"));
        validateScore(evaluation.getScore());
        validateDimensions(evaluation);
        if (rn == RoleName.TEAM_LEADER) {
            assertLeaderEvaluatesTeamMember(me, employee);
        }
        evaluation.setEmployee(employee);
        evaluation.setEvaluator(me);
        if (evaluation.getDate() == null) {
            evaluation.setDate(java.time.LocalDate.now());
        }
        Evaluation saved = evaluationRepository.save(evaluation);

        notificationHelper.notify(
                employee,
                NotificationType.EVALUATION_CREATED,
                "New performance evaluation from "
                        + (me.getFullName() != null && !me.getFullName().isBlank()
                                ? me.getFullName()
                                : me.getUsername()));

        return saved;
    }

    @Override
    public Evaluation update(Long id, Evaluation evaluation) {
        Evaluation existing = findById(id);
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn == RoleName.HR || rn == RoleName.MANAGER) {
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
            return evaluationRepository.save(existing);
        }
        if (rn == RoleName.TEAM_LEADER && existing.getEvaluator().getId().equals(me.getId())) {
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
            return evaluationRepository.save(existing);
        }
        throw new BusinessException("Not allowed to update this evaluation");
    }

    @Override
    public void delete(Long id) {
        Evaluation existing = evaluationRepository.findById(id).orElseThrow(() -> new BusinessException("Evaluation not found"));
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn == RoleName.HR || rn == RoleName.MANAGER) {
            evaluationRepository.delete(existing);
            return;
        }
        if (rn == RoleName.TEAM_LEADER && existing.getEvaluator().getId().equals(me.getId())) {
            evaluationRepository.delete(existing);
            return;
        }
        throw new BusinessException("Not allowed to delete this evaluation");
    }

    private void assertCanAccess(Evaluation ev) {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn == RoleName.HR || rn == RoleName.ADMIN || rn == RoleName.MANAGER) {
            return;
        }
        if (rn == RoleName.TEAM_LEADER) {
            Team led = teamRepository.findByTeamLeader_Id(me.getId()).orElse(null);
            if (led != null && isEmployeeInTeam(ev.getEmployee(), led.getId())) {
                return;
            }
        }
        throw new BusinessException("Not allowed to access this evaluation");
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
        if (v == null) {
            return;
        }
        if (v < 0 || v > 100) {
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

    private void assertLeaderEvaluatesTeamMember(User leader, User employee) {
        Team led = teamRepository
                .findByTeamLeader_Id(leader.getId())
                .orElseThrow(() -> new BusinessException("You are not a team leader of any team"));
        if (!isEmployeeInTeam(employee, led.getId())) {
            throw new BusinessException("You can only evaluate members of your team");
        }
    }

    private boolean isEmployeeInTeam(User employee, Long teamId) {
        if (employee == null || employee.getId() == null || teamId == null) {
            return false;
        }
        return employeeProfileRepository
                .findByUserId(employee.getId())
                .map(ep -> ep.getTeam() != null && teamId.equals(ep.getTeam().getId()))
                .orElse(false);
    }
}
