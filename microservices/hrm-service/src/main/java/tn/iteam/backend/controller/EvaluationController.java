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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.Evaluation;
import tn.iteam.backend.service.EvaluationService;

@RestController
@RequestMapping("/api/evaluations")
@Tag(name = "Evaluations", description = "Employee performance evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping
    @Operation(summary = "List evaluations", description = "Role-filtered evaluation list")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "401", description = "Unauthorized")})
    public List<Evaluation> list() {
        return evaluationService.findVisible();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get evaluation by id")
    @ApiResponse(responseCode = "200", description = "Evaluation found")
    public Evaluation one(@Parameter(example = "1") @PathVariable Long id) {
        return evaluationService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create evaluation")
    @ApiResponse(responseCode = "200", description = "Created")
    @PreAuthorize("hasAnyAuthority('HR','MANAGER','TEAM_LEADER')")
    public Evaluation create(@RequestBody Evaluation evaluation) {
        return evaluationService.create(evaluation);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update evaluation")
    @ApiResponse(responseCode = "200", description = "Updated")
    @PreAuthorize("hasAnyAuthority('HR','MANAGER','TEAM_LEADER')")
    public Evaluation update(@Parameter(example = "1") @PathVariable Long id, @RequestBody Evaluation evaluation) {
        return evaluationService.update(id, evaluation);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete evaluation")
    @ApiResponse(responseCode = "200", description = "Deleted")
    @PreAuthorize("hasAnyAuthority('HR','MANAGER','TEAM_LEADER')")
    public ResponseEntity<?> delete(@Parameter(example = "1") @PathVariable Long id) {
        evaluationService.delete(id);
        return ResponseEntity.ok().build();
    }
}
