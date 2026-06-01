package tn.iteam.backend.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.client.AuthServiceClient;
import tn.iteam.backend.entity.NotificationType;
import tn.iteam.backend.entity.Task;
import tn.iteam.backend.entity.TaskStatus;
import tn.iteam.backend.entity.UserSnapshot;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.messaging.EventPublisher;
import tn.iteam.backend.repository.TaskRepository;
import tn.iteam.backend.service.TaskService;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.dto.UserSummaryDto;
import tn.iteam.common.events.TaskValidatedEvent;
import tn.iteam.common.security.JwtUserPrincipal;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationHelper notificationHelper;
    private final AuthServiceClient authServiceClient;
    private final UserSnapshotService userSnapshotService;
    private final EventPublisher eventPublisher;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            CurrentUserProvider currentUserProvider,
            NotificationHelper notificationHelper,
            AuthServiceClient authServiceClient,
            UserSnapshotService userSnapshotService,
            EventPublisher eventPublisher
    ) {
        this.taskRepository = taskRepository;
        this.currentUserProvider = currentUserProvider;
        this.notificationHelper = notificationHelper;
        this.authServiceClient = authServiceClient;
        this.userSnapshotService = userSnapshotService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<Task> findAll() {
        return enrich(taskRepository.findAll());
    }

    @Override
    public Task findById(Long id) {
        return enrich(taskRepository.findById(id).orElseThrow(() -> new BusinessException("Task not found")));
    }

    @Override
    public List<Task> findMyTasks() {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        return enrich(taskRepository.findByAssignedToUserId(me.userId()));
    }

    @Override
    public Task create(Task task) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!"TEAM_LEADER".equals(me.role())) {
            throw new BusinessException("Only TEAM_LEADER can create and assign tasks");
        }

        if (task.getId() != null) {
            task.setId(null);
        }
        task.setCreatedByUserId(me.userId());

        Long assigneeId = extractAssigneeId(task);
        assertAssigneeInLeadersTeam(me.userId(), assigneeId);
        task.setAssignedToUserId(assigneeId);

        normalizeProgressAndStatus(task);
        Task saved = enrich(taskRepository.save(task));

        notificationHelper.notify(
                saved.getAssignedToUserId(),
                NotificationType.TASK_ASSIGNED,
                "You have been assigned task: " + saved.getTitle()
        );
        return saved;
    }

    @Override
    public Task update(Long id, Task task) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!"TEAM_LEADER".equals(me.role())) {
            throw new BusinessException("Only TEAM_LEADER can update tasks");
        }

        Task existing = findById(id);
        if (existing.getStatus() == TaskStatus.VALIDATED
                && existing.getProgress() != null
                && existing.getProgress() >= 100) {
            throw new BusinessException("This task is fully validated and cannot be edited");
        }

        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setPriority(task.getPriority());
        existing.setDueDate(task.getDueDate());
        existing.setProject(task.getProject());
        if (task.getEstimatedHours() != null) {
            existing.setEstimatedHours(task.getEstimatedHours());
        }

        Long assigneeId = extractAssigneeId(task);
        if (assigneeId != null && !assigneeId.equals(existing.getAssignedToUserId())) {
            assertAssigneeInLeadersTeam(me.userId(), assigneeId);
            existing.setAssignedToUserId(assigneeId);
        }

        if (task.getProgress() != null) {
            Integer floor = existing.getValidatedProgressFloor();
            if (floor != null && task.getProgress() < floor) {
                throw new BusinessException("Progress cannot be decreased below the validated level (" + floor + "%)");
            }
            existing.setProgress(task.getProgress());
        }
        if (task.getStatus() != null) {
            existing.setStatus(task.getStatus());
        }

        normalizeProgressAndStatus(existing);
        return enrich(taskRepository.save(existing));
    }

    @Override
    public Task updateProgress(Long id, int progress) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        Task task = findById(id);

        if (task.getAssignedToUserId() == null || !task.getAssignedToUserId().equals(me.userId())) {
            throw new BusinessException("You can only update progress on tasks assigned to you");
        }

        if (task.getStatus() == TaskStatus.VALIDATED && task.getProgress() != null && task.getProgress() >= 100) {
            throw new BusinessException("Progress cannot be updated after the task is 100% complete and validated");
        }

        Integer floor = task.getValidatedProgressFloor();
        if (floor != null && progress < floor) {
            throw new BusinessException("Progress cannot be decreased below the validated level (" + floor + "%)");
        }

        task.setProgress(progress);
        normalizeProgressAndStatus(task);
        Task saved = enrich(taskRepository.save(task));

        if (saved.getProgress() == 100) {
            notificationHelper.notify(
                    saved.getCreatedByUserId(),
                    NotificationType.TASK_COMPLETED,
                    "Task completed: " + saved.getTitle()
            );
        }
        return saved;
    }

    @Override
    public Task validate(Long id) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!"TEAM_LEADER".equals(me.role())) {
            throw new BusinessException("Only TEAM_LEADER can validate tasks");
        }

        Task task = findById(id);
        int current = task.getProgress() == null ? 0 : task.getProgress();
        task.setValidatedProgressFloor(current);
        task.setStatus(TaskStatus.VALIDATED);
        Task saved = enrich(taskRepository.save(task));

        notificationHelper.notify(
                saved.getAssignedToUserId(),
                NotificationType.TASK_VALIDATED,
                "Your task was validated: " + saved.getTitle()
        );

        if (saved.getAssignedToUserId() != null) {
            eventPublisher.publishTaskValidated(new TaskValidatedEvent(
                    saved.getId(),
                    saved.getAssignedToUserId(),
                    me.userId(),
                    saved.getValidatedProgressFloor(),
                    Instant.now()
            ));
        }
        return saved;
    }

    @Override
    public void delete(Long id) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!"TEAM_LEADER".equals(me.role())) {
            throw new BusinessException("Only TEAM_LEADER can delete tasks");
        }
        findById(id);
        taskRepository.deleteById(id);
    }

    private void assertAssigneeInLeadersTeam(Long leaderUserId, Long assigneeId) {
        if (assigneeId == null) {
            return;
        }
        UserSummaryDto leader = authServiceClient.getUser(leaderUserId);
        UserSummaryDto assignee = authServiceClient.getUser(assigneeId);
        if (leader.teamId() == null || assignee.teamId() == null || !leader.teamId().equals(assignee.teamId())) {
            throw new BusinessException("Cannot assign task outside your team");
        }
    }

    private Long extractAssigneeId(Task task) {
        if (task.getAssignedToUserId() != null) {
            return task.getAssignedToUserId();
        }
        if (task.getAssignedTo() != null && task.getAssignedTo().get("id") != null) {
            return Long.valueOf(task.getAssignedTo().get("id").toString());
        }
        return null;
    }

    private List<Task> enrich(List<Task> tasks) {
        return tasks.stream().map(this::enrich).toList();
    }

    private Task enrich(Task task) {
        Map<Long, UserSnapshot> users = userSnapshotService.mapByIds(
                java.util.stream.Stream.of(task.getAssignedToUserId(), task.getCreatedByUserId())
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .toList()
        );
        task.enrichAssignedTo(users.get(task.getAssignedToUserId()));
        task.enrichCreatedBy(users.get(task.getCreatedByUserId()));
        return task;
    }

    private void normalizeProgressAndStatus(Task task) {
        int p = task.getProgress() == null ? 0 : task.getProgress();
        p = Math.max(0, Math.min(100, p));
        task.setProgress(p);
        if (task.getStatus() == TaskStatus.VALIDATED) {
            return;
        }
        if (p == 100) {
            task.setStatus(TaskStatus.DONE);
        } else if (p > 0 && (task.getStatus() == null || task.getStatus() == TaskStatus.TODO)) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        } else if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
    }
}
