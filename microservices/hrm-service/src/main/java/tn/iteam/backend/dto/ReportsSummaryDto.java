package tn.iteam.backend.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReportsSummaryDto {

    private Map<String, Long> tasksByStatus = new LinkedHashMap<>();
    private Map<String, Long> employeesByTeam = new LinkedHashMap<>();
    private Map<String, Long> leavesByStatus = new LinkedHashMap<>();

    public Map<String, Long> getTasksByStatus() {
        return tasksByStatus;
    }

    public void setTasksByStatus(Map<String, Long> tasksByStatus) {
        this.tasksByStatus = tasksByStatus;
    }

    public Map<String, Long> getEmployeesByTeam() {
        return employeesByTeam;
    }

    public void setEmployeesByTeam(Map<String, Long> employeesByTeam) {
        this.employeesByTeam = employeesByTeam;
    }

    public Map<String, Long> getLeavesByStatus() {
        return leavesByStatus;
    }

    public void setLeavesByStatus(Map<String, Long> leavesByStatus) {
        this.leavesByStatus = leavesByStatus;
    }
}
