package tn.iteam.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.Payroll;
import tn.iteam.backend.service.PayrollService;

@RestController
@RequestMapping("/api/payroll")
@Tag(name = "Payroll", description = "Payroll API")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping
    @Operation(summary = "List payroll records", description = "Visible payroll entries for current role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payroll list"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public List<Payroll> list() {
        return payrollService.findVisible();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Payroll for user")
    @ApiResponse(responseCode = "200", description = "User payroll history")
    public List<Payroll> forUser(@Parameter(description = "User id", example = "3") @PathVariable Long userId) {
        return payrollService.findForUser(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payroll by id")
    @ApiResponse(responseCode = "200", description = "Payroll record")
    public Payroll one(@Parameter(example = "1") @PathVariable Long id) {
        return payrollService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create payroll", description = "HR only")
    @ApiResponse(responseCode = "200", description = "Created")
    public Payroll create(@RequestBody Payroll payroll) {
        return payrollService.save(payroll);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update payroll")
    @ApiResponse(responseCode = "200", description = "Updated")
    public Payroll update(@Parameter(example = "1") @PathVariable Long id, @RequestBody Payroll payroll) {
        return payrollService.update(id, payroll);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payroll")
    @ApiResponse(responseCode = "200", description = "Deleted")
    public ResponseEntity<?> delete(@Parameter(example = "1") @PathVariable Long id) {
        payrollService.delete(id);
        return ResponseEntity.ok().build();
    }
}
