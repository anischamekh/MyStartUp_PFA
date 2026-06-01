package tn.iteam.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "training_attendance",
        uniqueConstraints = @UniqueConstraint(name = "uk_training_attendance_user", columnNames = {"training_id", "user_id"})
)
public class TrainingAttendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @Column(name = "user_id", nullable = false)
    @JsonIgnore
    private Long userId;

    @Column(nullable = false)
    private boolean attended = false;

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

	public Training getTraining() {
		return training;
	}

	public void setTraining(Training training) {
		this.training = training;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public boolean isAttended() {
		return attended;
	}

	public void setAttended(boolean attended) {
		this.attended = attended;
	}

	public void setUser(Map<String, Object> user) {
		this.user = user;
	}
    
    
}
