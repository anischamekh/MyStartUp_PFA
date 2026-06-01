package tn.iteam.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "documents")
public class EmployeeDocument extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    @JsonIgnore
    private Long userId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 80)
    private String type;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate = LocalDateTime.now();

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public LocalDateTime getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(LocalDateTime uploadDate) {
		this.uploadDate = uploadDate;
	}

	public void setUser(Map<String, Object> user) {
		this.user = user;
	}
    
    
}
