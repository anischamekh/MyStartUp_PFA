package tn.iteam.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.Project;
import tn.iteam.backend.repository.ProjectRepository;
import tn.iteam.backend.repository.TaskRepository;

@Service
@Transactional
public class UserDeletionCleanupService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserSnapshotService userSnapshotService;

    public UserDeletionCleanupService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserSnapshotService userSnapshotService
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userSnapshotService = userSnapshotService;
    }

    public void cleanup(Long userId) {
        taskRepository.clearAssignedToForUser(userId);
        taskRepository.clearCreatedByForUser(userId);
        for (Project project : projectRepository.findByManagerUserId(userId)) {
            project.setManagerUserId(null);
            projectRepository.save(project);
        }
        userSnapshotService.deleteUser(userId);
    }
}
