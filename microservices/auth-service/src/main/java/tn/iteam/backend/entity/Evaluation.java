package tn.iteam.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "evaluations")
public class Evaluation extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @Column(nullable = false)
    private int score;

    /** 0–100; optional detailed dimensions */
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

	public User getEmployee() {
		return employee;
	}

	public void setEmployee(User employee) {
		this.employee = employee;
	}

	public User getEvaluator() {
		return evaluator;
	}

	public void setEvaluator(User evaluator) {
		this.evaluator = evaluator;
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
    
    
}
