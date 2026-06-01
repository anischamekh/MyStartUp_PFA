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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "teams")
public class Team extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Speciality speciality = Speciality.FRONTEND;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_leader_id")
    @JsonIgnore
    private User teamLeader;

    @OneToMany(mappedBy = "team")
    @JsonIgnore
    private List<EmployeeProfile> members = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Speciality getSpeciality() {
		return speciality;
	}

	public void setSpeciality(Speciality speciality) {
		this.speciality = speciality;
	}

	public User getTeamLeader() {
		return teamLeader;
	}

	public void setTeamLeader(User teamLeader) {
		this.teamLeader = teamLeader;
	}

	public List<EmployeeProfile> getMembers() {
		return members;
	}

	public void setMembers(List<EmployeeProfile> members) {
		this.members = members;
	}

	@JsonProperty("teamLeaderId")
	public Long getTeamLeaderIdForJson() {
		return teamLeader == null ? null : teamLeader.getId();
	}

	@JsonProperty("teamLeaderName")
	public String getTeamLeaderNameForJson() {
		if (teamLeader == null) {
			return null;
		}
		return teamLeader.getFullName() != null && !teamLeader.getFullName().isBlank()
				? teamLeader.getFullName()
				: teamLeader.getUsername();
	}
    
    
}

