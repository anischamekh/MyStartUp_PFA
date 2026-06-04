package tn.iteam.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.Training;
import tn.iteam.backend.entity.TrainingAttendance;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.service.TrainingService;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Training", description = "Training sessions and attendance")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping
    @Operation(summary = "List trainings")
    @ApiResponse(responseCode = "200", description = "Training list")
    public List<Training> all() {
        return trainingService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get training by id")
    @ApiResponse(responseCode = "200", description = "Training found")
    public Training one(@Parameter(example = "1") @PathVariable Long id) {
        return trainingService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create training", description = "HR only")
    @ApiResponse(responseCode = "200", description = "Created")
    @PreAuthorize("hasAuthority('HR')")
    public Training create(@RequestBody Training training) {
        return trainingService.save(training);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update training")
    @ApiResponse(responseCode = "200", description = "Updated")
    @PreAuthorize("hasAuthority('HR')")
    public Training update(@Parameter(example = "1") @PathVariable Long id, @RequestBody Training training) {
        return trainingService.update(id, training);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete training")
    @ApiResponse(responseCode = "200", description = "Deleted")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<?> delete(@Parameter(example = "1") @PathVariable Long id) {
        trainingService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/attendance")
    @Operation(summary = "List attendance for training")
    @ApiResponse(responseCode = "200", description = "Attendance rows")
    public List<TrainingAttendance> attendance(@Parameter(example = "1") @PathVariable Long id) {
        return trainingService.listAttendance(id);
    }

    @PostMapping("/{id}/attendance")
    @Operation(summary = "Add attendee", description = "Body: {\"userId\": 3}")
    @ApiResponse(responseCode = "200", description = "Attendance created")
    public TrainingAttendance addAttendance(@Parameter(example = "1") @PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long userId = body == null ? null : body.get("userId");
        if (userId == null) {
            throw new BusinessException("userId is required");
        }
        return trainingService.addAttendance(id, userId);
    }

    @PatchMapping("/attendance/{attendanceId}")
    @Operation(summary = "Mark attendance", description = "Body: {\"attended\": true}")
    @ApiResponse(responseCode = "200", description = "Attendance updated")
    public TrainingAttendance patchAttended(
            @Parameter(example = "10") @PathVariable Long attendanceId,
            @RequestBody Map<String, Boolean> body) {
        if (body == null || !body.containsKey("attended")) {
            throw new BusinessException("attended is required");
        }
        return trainingService.setAttended(attendanceId, Boolean.TRUE.equals(body.get("attended")));
    }
}
