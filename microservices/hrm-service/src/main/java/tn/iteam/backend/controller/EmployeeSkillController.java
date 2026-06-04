package tn.iteam.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.EmployeeSkill;
import tn.iteam.backend.service.EmployeeSkillService;

@RestController
@RequestMapping("/api/employee-skills")
@Tag(name = "Employee Skills", description = "Skills assigned to employees")
public class EmployeeSkillController {

    private final EmployeeSkillService employeeSkillService;

    public EmployeeSkillController(EmployeeSkillService employeeSkillService) {
        this.employeeSkillService = employeeSkillService;
    }

    @GetMapping
    @Operation(summary = "List employee skills")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "401", description = "Unauthorized")})
    public List<EmployeeSkill> list() {
        return employeeSkillService.findVisible();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Skills for user")
    @ApiResponse(responseCode = "200", description = "User skills")
    public List<EmployeeSkill> forUser(@Parameter(example = "3") @PathVariable Long userId) {
        return employeeSkillService.findForUser(userId);
    }

    @PostMapping
    @Operation(summary = "Upsert employee skill")
    @ApiResponse(responseCode = "200", description = "Saved")
    @PreAuthorize("hasAuthority('HR')")
    public EmployeeSkill upsert(@RequestBody EmployeeSkill body) {
        return employeeSkillService.upsert(body);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete employee skill")
    @ApiResponse(responseCode = "200", description = "Deleted")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<?> delete(@Parameter(example = "1") @PathVariable Long id) {
        employeeSkillService.delete(id);
        return ResponseEntity.ok().build();
    }
}
