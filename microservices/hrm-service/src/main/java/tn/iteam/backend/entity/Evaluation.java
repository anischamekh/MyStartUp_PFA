package tn.iteam.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "evaluations")
public class Evaluation extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    @JsonIgnore
    private Long employeeId;

    @Column(name = "evaluator_id", nullable = false)
    @JsonIgnore
    private Long evaluatorId;

    @Column(nullable = false)
    private int score;

    @Column(name = "technical_skill")
    private Integer technicalSkill;

    @Column(name = "teamwork")
    private Integer teamwork;

    @Column(name = "deadline_respect")
    private Integer deadlineRespect;

    @Column(length = 2000)
    private String comment;

    @Column(nullable = false)
    private LocalDate date;

    @Transient
    private Map<String, Object> employee;

    @Transient
    private Map<String, Object> evaluator;

    @JsonProperty("employee")
    public Map<String, Object> getEmployee() {
        return employee;
    }

    @JsonProperty("evaluator")
    public Map<String, Object> getEvaluator() {
        return evaluator;
    }

    public void enrichEmployee(UserSnapshot snapshot) {
        employee = snapshot == null ? null : userMap(snapshot);
    }

    public void enrichEvaluator(UserSnapshot snapshot) {
        evaluator = snapshot == null ? null : userMap(snapshot);
    }

    private static Map<String, Object> userMap(UserSnapshot s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId());
        map.put("fullName", s.getFullName());
        return map;
        
        
    }

	public Long getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Long employeeId) {
		this.employeeId = employeeId;
	}

	public Long getEvaluatorId() {
		return evaluatorId;
	}

	public void setEvaluatorId(Long evaluatorId) {
		this.evaluatorId = evaluatorId;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public Integer getTechnicalSkill() {
		return technicalSkill;
	}

	public void setTechnicalSkill(Integer technicalSkill) {
		this.technicalSkill = technicalSkill;
	}

	public Integer getTeamwork() {
		return teamwork;
	}

	public void setTeamwork(Integer teamwork) {
		this.teamwork = teamwork;
	}

	public Integer getDeadlineRespect() {
		return deadlineRespect;
	}

	public void setDeadlineRespect(Integer deadlineRespect) {
		this.deadlineRespect = deadlineRespect;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public void setEmployee(Map<String, Object> employee) {
		this.employee = employee;
	}

	public void setEvaluator(Map<String, Object> evaluator) {
		this.evaluator = evaluator;
	}
}
