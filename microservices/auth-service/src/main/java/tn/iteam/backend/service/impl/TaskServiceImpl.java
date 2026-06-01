package tn.iteam.backend.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.NotificationType;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.Task;
import tn.iteam.backend.entity.TaskStatus;
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.repository.TaskRepository;
import tn.iteam.backend.repository.TeamRepository;
import tn.iteam.backend.service.TaskService;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationHelper notificationHelper;
    private final TeamRepository teamRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            CurrentUserProvider currentUserProvider,
            NotificationHelper notificationHelper,
            TeamRepository teamRepository,
            EmployeeProfileRepository employeeProfileRepository
    ) {
        this.taskRepository = taskRepository;
        this.currentUserProvider = currentUserProvider;
        this.notificationHelper = notificationHelper;
        this.teamRepository = teamRepository;
        this.employeeProfileRepository = employeeProfileRepository;
    }

    @Override
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Override
    public Task findById(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new BusinessException("Task not found"));
    }

    @Override
    public List<Task> findMyTasks() {
        User me = currentUserProvider.requireCurrentUser();
        return taskRepository.findByAssignedToId(me.getId());
    }

    @Override
    public Task create(Task task) {
        User me = currentUserProvider.requireCurrentUser();
        if (me.getRole() == null || me.getRole().getName() != RoleName.TEAM_LEADER) {
            throw new BusinessException("Only TEAM_LEADER can create and assign tasks");
        }

        if (task.getId() != null) {
            task.setId(null);
        }
        task.setCreatedBy(me);

        assertAssigneeInLeadersTeam(me, task.getAssignedTo());

        normalizeProgressAndStatus(task);

        Task saved = taskRepository.save(task);

        notificationHelper.notify(
                saved.getAssignedTo(),
                NotificationType.TASK_ASSIGNED,
                "You have been assigned task: " + saved.getTitle()
        );

        return saved;
    }

    @Override
    public Task update(Long id, Task task) {
        User me = currentUserProvider.requireCurrentUser();
        if (me.getRole() == null || me.getRole().getName() != RoleName.TEAM_LEADER) {
            throw new BusinessException("Only TEAM_LEADER can update tasks");
        }

        Task existing = findById(id);

        boolean fullyValidated =
                existing.getStatus() == TaskStatus.VALIDATED
                        && existing.getProgress() != null
                        && existing.getProgress() >= 100;
        if (fullyValidated) {
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

        if (task.getAssignedTo() != null
                && (existing.getAssignedTo() == null
                        || !task.getAssignedTo().getId().equals(existing.getAssignedTo().getId()))) {
            assertAssigneeInLeadersTeam(me, task.getAssignedTo());
            existing.setAssignedTo(task.getAssignedTo());
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
        return taskRepository.save(existing);
    }

    @Override
    public Task updateProgress(Long id, int progress) {
        User me = currentUserProvider.requireCurrentUser();
        Task task = findById(id);

        if (task.getAssignedTo() == null
                || task.getAssignedTo().getId() == null
                || !task.getAssignedTo().getId().equals(me.getId())) {
            throw new BusinessException("You can only update progress on tasks assigned to you");
        }

        if (task.getStatus() == TaskStatus.VALIDATED
                && task.getProgress() != null
                && task.getProgress() >= 100) {
            throw new BusinessException("Progress cannot be updated after the task is 100% complete and validated");
        }

        Integer floor = task.getValidatedProgressFloor();
        if (floor != null && progress < floor) {
            throw new BusinessException("Progress cannot be decreased below the validated level (" + floor + "%)");
        }

        task.setProgress(progress);
        normalizeProgressAndStatus(task);

        Task saved = taskRepository.save(task);

        if (saved.getProgress() == 100) {
            notificationHelper.notify(
                    saved.getCreatedBy(),
                    NotificationType.TASK_COMPLETED,
                    "Task completed by " + me.getFullName() + ": " + saved.getTitle()
            );
        }

        return saved;
    }

    @Override
    public Task validate(Long id) {
        User me = currentUserProvider.requireCurrentUser();
        if (me.getRole() == null || me.getRole().getName() != RoleName.TEAM_LEADER) {
            throw new BusinessException("Only TEAM_LEADER can validate tasks");
        }

        Task task = findById(id);
        int current = task.getProgress() == null ? 0 : task.getProgress();
        task.setValidatedProgressFloor(current);
        task.setStatus(TaskStatus.VALIDATED);
        Task saved = taskRepository.save(task);

        notificationHelper.notify(
                saved.getAssignedTo(),
                NotificationType.TASK_VALIDATED,
                "Your task was validated: " + saved.getTitle()
        );

        return saved;
    }

    @Override
    public void delete(Long id) {
        User me = currentUserProvider.requireCurrentUser();
        if (me.getRole() == null || me.getRole().getName() != RoleName.TEAM_LEADER) {
            throw new BusinessException("Only TEAM_LEADER can delete tasks");
        }
        findById(id);
        taskRepository.deleteById(id);
    }

    private void assertAssigneeInLeadersTeam(User leader, User assignee) {
        if (assignee == null || assignee.getId() == null) {
            return;
        }
        Team led = teamRepository
                .findByTeamLeader_Id(leader.getId())
                .orElseThrow(() -> new BusinessException("You are not a team leader of any team"));
        EmployeeProfile ep = employeeProfileRepository
                .findByUserId(assignee.getId())
                .orElseThrow(() -> new BusinessException("Assignee has no employee profile"));
        if (ep.getTeam() == null || ep.getTeam().getId() == null) {
            throw new BusinessException("Assignee is not a member of any team");
        }
        if (!ep.getTeam().getId().equals(led.getId())) {
            throw new BusinessException("Cannot assign task outside your team");
        }
    }

    private void normalizeProgressAndStatus(Task task) {
        int p = task.getProgress() == null ? 0 : task.getProgress();
        if (p < 0) {
            p = 0;
        }
        if (p > 100) {
            p = 100;
        }
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
