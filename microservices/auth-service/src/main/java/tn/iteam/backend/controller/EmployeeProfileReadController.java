package tn.iteam.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.service.impl.CurrentUserProvider;

@RestController
@RequestMapping("/api/employee-profiles")
@Tag(name = "Employee Profiles", description = "Read employee profile data (HR/Admin)")
public class EmployeeProfileReadController {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final CurrentUserProvider currentUserProvider;

    public EmployeeProfileReadController(
            EmployeeProfileRepository employeeProfileRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.employeeProfileRepository = employeeProfileRepository;
        this.currentUserProvider = currentUserProvider;
    }

    /** HR / ADMIN: merge with {@code GET /api/users} when user JSON omits nested profile (cycle-safe). */
    @GetMapping
    @Operation(summary = "List employee profiles", description = "HR and ADMIN only")
    @ApiResponse(responseCode = "200", description = "Profile list")
    public List<EmployeeProfile> all() {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.HR && rn != RoleName.ADMIN) {
            throw new BusinessException("Not allowed to list employee profiles");
        }
        return employeeProfileRepository.findAll();
    }
}
