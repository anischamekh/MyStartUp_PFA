package tn.iteam.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "leave_requests")
public class LeaveRequest extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    @JsonIgnore
    private Long employeeId;

    @Column(name = "manager_id")
    @JsonIgnore
    private Long managerId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer days;

    @Column(length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LeaveStatus status = LeaveStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 30)
    private LeaveType leaveType = LeaveType.ANNUAL;

    @Transient
    private Map<String, Object> employee;

    @Transient
    private Map<String, Object> manager;

    @JsonProperty("employee")
    public Map<String, Object> getEmployee() {
        return employee;
    }

    @JsonProperty("manager")
    public Map<String, Object> getManager() {
        return manager;
    }

    public void enrichEmployee(UserSnapshot snapshot) {
        employee = snapshot == null ? null : toUserMap(snapshot);
    }

    public void enrichManager(UserSnapshot snapshot) {
        manager = snapshot == null ? null : toUserMap(snapshot);
    }

    private static Map<String, Object> toUserMap(UserSnapshot s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId());
        map.put("username", s.getUsername());
        map.put("fullName", s.getFullName());
        map.put("email", s.getEmail());
        if (s.getRoleName() != null) {
            Map<String, Object> role = new LinkedHashMap<>();
            role.put("name", s.getRoleName());
            map.put("role", role);
        }
        return map;
    }

	public Long getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Long employeeId) {
		this.employeeId = employeeId;
	}

	public Long getManagerId() {
		return managerId;
	}

	public void setManagerId(Long managerId) {
		this.managerId = managerId;
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

	public Integer getDays() {
		return days;
	}

	public void setDays(Integer days) {
		this.days = days;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public LeaveStatus getStatus() {
		return status;
	}

	public void setStatus(LeaveStatus status) {
		this.status = status;
	}

	public LeaveType getLeaveType() {
		return leaveType;
	}

	public void setLeaveType(LeaveType leaveType) {
		this.leaveType = leaveType;
	}

	public void setEmployee(Map<String, Object> employee) {
		this.employee = employee;
	}

	public void setManager(Map<String, Object> manager) {
		this.manager = manager;
	}
    
    
}
