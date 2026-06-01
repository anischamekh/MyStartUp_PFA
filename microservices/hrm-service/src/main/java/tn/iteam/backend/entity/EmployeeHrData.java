package tn.iteam.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employee_hr_data")
public class EmployeeHrData {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "remaining_leave_days", nullable = false)
    private Integer remainingLeaveDays = 30;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getRemainingLeaveDays() {
		return remainingLeaveDays;
	}

	public void setRemainingLeaveDays(Integer remainingLeaveDays) {
		this.remainingLeaveDays = remainingLeaveDays;
	}
    
    
}
