package tn.iteam.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tasks")
public class Task extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(nullable = false)
    private Integer progress = 0;

    private LocalDate dueDate;

    @Column(name = "assigned_to_id")
    @JsonIgnore
    private Long assignedToUserId;

    @Column(name = "created_by_id")
    @JsonIgnore
    private Long createdByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "validated_progress_floor")
    private Integer validatedProgressFloor;

    @Column(name = "estimated_hours")
    private Double estimatedHours;

    @Transient
    private Map<String, Object> assignedTo;

    @Transient
    private Map<String, Object> createdBy;

    @JsonProperty("assignedTo")
    public Map<String, Object> getAssignedTo() {
        return assignedTo;
    }

    @JsonProperty("createdBy")
    public Map<String, Object> getCreatedBy() {
        return createdBy;
    }

    public void enrichAssignedTo(UserSnapshot snapshot) {
        assignedTo = snapshot == null ? null : userMap(snapshot);
    }

    public void enrichCreatedBy(UserSnapshot snapshot) {
        createdBy = snapshot == null ? null : userMap(snapshot);
    }

    private static Map<String, Object> userMap(UserSnapshot s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId());
        map.put("username", s.getUsername());
        map.put("fullName", s.getFullName());
        return map;
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

	public Long getAssignedToUserId() {
		return assignedToUserId;
	}

	public void setAssignedToUserId(Long assignedToUserId) {
		this.assignedToUserId = assignedToUserId;
	}

	public Long getCreatedByUserId() {
		return createdByUserId;
	}

	public void setCreatedByUserId(Long createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Integer getValidatedProgressFloor() {
		return validatedProgressFloor;
	}

	public void setValidatedProgressFloor(Integer validatedProgressFloor) {
		this.validatedProgressFloor = validatedProgressFloor;
	}

	public Double getEstimatedHours() {
		return estimatedHours;
	}

	public void setEstimatedHours(Double estimatedHours) {
		this.estimatedHours = estimatedHours;
	}

	public void setAssignedTo(Map<String, Object> assignedTo) {
		this.assignedTo = assignedTo;
	}

	public void setCreatedBy(Map<String, Object> createdBy) {
		this.createdBy = createdBy;
	}
    
    
}
