package tn.iteam.backend.controller;

import io.swagger.v3.oas.annotations.Hidden;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.TaskStatus;
import tn.iteam.backend.repository.TaskRepository;

@Hidden
@RestController
@RequestMapping("/api/internal/users")
public class InternalUserController {

    private final TaskRepository taskRepository;

    public InternalUserController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/{userId}/has-active-tasks")
    public boolean hasActiveTasks(@PathVariable Long userId) {
        return taskRepository.existsByAssignedToUserIdAndStatusIn(
                userId, List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS));
    }
}
