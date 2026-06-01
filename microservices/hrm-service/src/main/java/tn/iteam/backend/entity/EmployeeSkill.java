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
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employee_skills")
public class EmployeeSkill extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    @JsonIgnore
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkillProficiency level = SkillProficiency.INTERMEDIATE;

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

	public Skill getSkill() {
		return skill;
	}

	public void setSkill(Skill skill) {
		this.skill = skill;
	}

	public SkillProficiency getLevel() {
		return level;
	}

	public void setLevel(SkillProficiency level) {
		this.level = level;
	}

	public void setUser(Map<String, Object> user) {
		this.user = user;
	}
    
    
}
