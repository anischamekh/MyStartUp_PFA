package tn.iteam.backend.mapper;

import tn.iteam.backend.dto.ProjectSummaryDto;
import tn.iteam.backend.dto.TaskResponse;
import tn.iteam.backend.entity.Project;
import tn.iteam.backend.entity.Task;

public final class TaskMapper {

    private TaskMapper() {
    }

    public static TaskResponse toResponse(Task task) {
        TaskResponse dto = new TaskResponse();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setProgress(task.getProgress());
        dto.setDueDate(task.getDueDate());
        dto.setEstimatedHours(task.getEstimatedHours());
        dto.setValidatedProgressFloor(task.getValidatedProgressFloor());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setAssignedTo(task.getAssignedTo());
        dto.setCreatedBy(task.getCreatedBy());

        Project project = task.getProject();
        if (project != null) {
            dto.setProject(new ProjectSummaryDto(project.getId(), project.getName()));
        }
        return dto;
    }
}
