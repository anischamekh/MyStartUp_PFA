package tn.iteam.backend.dto;

import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.User;

public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private RoleName role;
    private Long teamId;
    private String teamName;
    private EmployeeProfilePayload employeeProfile;

    public static UserResponse from(User user, EmployeeProfile profile) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setUsername(user.getUsername());
        r.setFullName(user.getFullName());
        r.setEmail(user.getEmail());
        r.setRole(user.getRole() != null ? user.getRole().getName() : null);
        if (profile != null) {
            if (profile.getTeam() != null) {
                r.setTeamId(profile.getTeam().getId());
                r.setTeamName(profile.getTeam().getName());
            }
            r.setEmployeeProfile(EmployeeProfilePayload.fromEntity(profile));
        }
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RoleName getRole() {
        return role;
    }

    public void setRole(RoleName role) {
        this.role = role;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public EmployeeProfilePayload getEmployeeProfile() {
        return employeeProfile;
    }

    public void setEmployeeProfile(EmployeeProfilePayload employeeProfile) {
        this.employeeProfile = employeeProfile;
    }
}
