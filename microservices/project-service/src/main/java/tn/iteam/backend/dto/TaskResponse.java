package tn.iteam.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import tn.iteam.backend.entity.TaskPriority;
import tn.iteam.backend.entity.TaskStatus;

public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Integer progress;
    private LocalDate dueDate;
    private Double estimatedHours;
    private Integer validatedProgressFloor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> assignedTo;
    private Map<String, Object> createdBy;
    private ProjectSummaryDto project;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Integer getValidatedProgressFloor() {
        return validatedProgressFloor;
    }

    public void setValidatedProgressFloor(Integer validatedProgressFloor) {
        this.validatedProgressFloor = validatedProgressFloor;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Object> getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Map<String, Object> assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Map<String, Object> getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Map<String, Object> createdBy) {
        this.createdBy = createdBy;
    }

    public ProjectSummaryDto getProject() {
        return project;
    }

    public void setProject(ProjectSummaryDto project) {
        this.project = project;
    }
}
