package tn.iteam.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "projects")
public class Project extends BaseEntity {

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectStatus status = ProjectStatus.PLANNING;

    @Column(nullable = false)
    private Integer progress = 0;

    @Column(name = "manager_id")
    @JsonIgnore
    private Long managerUserId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_team_ids", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "team_id")
    private Set<Long> teamIds = new HashSet<>();

    @OneToMany(mappedBy = "project")
    @JsonIgnore
    private List<Task> tasks = new ArrayList<>();

    @Transient
    private Map<String, Object> manager;

    @Transient
    private Set<Map<String, Object>> teams = new HashSet<>();

    @JsonProperty("manager")
    public Map<String, Object> getManager() {
        return manager;
    }

    @JsonProperty("teams")
    public Set<Map<String, Object>> getTeams() {
        return teams;
    }

    public void enrichManager(UserSnapshot snapshot) {
        if (snapshot == null) {
            manager = null;
            return;
        }
        manager = new LinkedHashMap<>();
        manager.put("id", snapshot.getId());
        manager.put("fullName", snapshot.getFullName());
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public ProjectStatus getStatus() {
		return status;
	}

	public void setStatus(ProjectStatus status) {
		this.status = status;
	}

	public Integer getProgress() {
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}

	public Long getManagerUserId() {
		return managerUserId;
	}

	public void setManagerUserId(Long managerUserId) {
		this.managerUserId = managerUserId;
	}

	public Set<Long> getTeamIds() {
		return teamIds;
	}

	public void setTeamIds(Set<Long> teamIds) {
		this.teamIds = teamIds;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public void setManager(Map<String, Object> manager) {
		this.manager = manager;
	}

	public void setTeams(Set<Map<String, Object>> teams) {
		this.teams = teams;
	}
    
    
}
