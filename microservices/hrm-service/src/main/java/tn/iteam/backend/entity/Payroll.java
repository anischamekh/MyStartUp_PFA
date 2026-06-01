package tn.iteam.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payroll")
public class Payroll extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    @JsonIgnore
    private Long userId;

    @Column(name = "base_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal baseSalary = BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(name = "total_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalSalary = BigDecimal.ZERO;

    @Transient
    private Map<String, Object> user;

    @JsonProperty("user")
    public Map<String, Object> getUser() {
        return user;
    }

    public void enrichUser(UserSnapshot snapshot) {
        if (snapshot == null) {
            user = null;
            return;
        }
        user = new LinkedHashMap<>();
        user.put("id", snapshot.getId());
        user.put("fullName", snapshot.getFullName());
    }

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public BigDecimal getBaseSalary() {
		return baseSalary;
	}

	public void setBaseSalary(BigDecimal baseSalary) {
		this.baseSalary = baseSalary;
	}

	public BigDecimal getBonus() {
		return bonus;
	}

	public void setBonus(BigDecimal bonus) {
		this.bonus = bonus;
	}

	public BigDecimal getDeductions() {
		return deductions;
	}

	public void setDeductions(BigDecimal deductions) {
		this.deductions = deductions;
	}

	public BigDecimal getTotalSalary() {
		return totalSalary;
	}

	public void setTotalSalary(BigDecimal totalSalary) {
		this.totalSalary = totalSalary;
	}

	public void setUser(Map<String, Object> user) {
		this.user = user;
	}
    
    
}
