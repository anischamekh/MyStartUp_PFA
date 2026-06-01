package tn.iteam.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.ExperienceLevel;
import tn.iteam.backend.entity.Speciality;

public class EmployeeProfilePayload {

    public static EmployeeProfilePayload fromEntity(EmployeeProfile profile) {
        if (profile == null) {
            return null;
        }
        EmployeeProfilePayload p = new EmployeeProfilePayload();
        if (profile.getTeam() != null) {
            p.setTeamId(profile.getTeam().getId());
        }
        p.setPhone(profile.getPhone());
        p.setAddress(profile.getAddress());
        p.setJobTitle(profile.getJobTitle());
        p.setSpeciality(profile.getSpeciality());
        p.setExperienceLevel(profile.getExperienceLevel());
        p.setSalary(profile.getSalary());
        p.setHireDate(profile.getHireDate());
        p.setRemainingLeaveDays(profile.getRemainingLeaveDays());
        return p;
    }

    public void applyTo(EmployeeProfile target) {
        target.setPhone(phone);
        target.setAddress(address);
        target.setJobTitle(jobTitle);
        target.setSpeciality(speciality);
        target.setExperienceLevel(experienceLevel);
        target.setSalary(salary);
        target.setHireDate(hireDate);
        if (remainingLeaveDays != null) {
            target.setRemainingLeaveDays(remainingLeaveDays);
        }
    }

    private Long teamId;
    private String phone;
    private String address;
    private String jobTitle;
    private Speciality speciality;
    private ExperienceLevel experienceLevel;
    private BigDecimal salary;
    private LocalDate hireDate;
    private Integer remainingLeaveDays;

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        this.speciality = speciality;
    }

    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(ExperienceLevel experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public Integer getRemainingLeaveDays() {
        return remainingLeaveDays;
    }

    public void setRemainingLeaveDays(Integer remainingLeaveDays) {
        this.remainingLeaveDays = remainingLeaveDays;
    }
}
