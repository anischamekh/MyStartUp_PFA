package tn.iteam.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.LeaveRequest;
import tn.iteam.backend.service.LeaveRequestService;
import tn.iteam.common.openapi.OpenApiExamples;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leave Management", description = "Employee leave requests and approvals")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @GetMapping
    @Operation(summary = "List all leave requests", description = "HR/Manager view of all requests")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave list",
                    content = @Content(schema = @Schema(implementation = LeaveRequest.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public List<LeaveRequest> all() {
        return leaveRequestService.findAll();
    }

    @GetMapping("/mine")
    @Operation(summary = "My leave requests")
    public List<LeaveRequest> mine() {
        return leaveRequestService.findMine();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Leave history for a user")
    public List<LeaveRequest> forUser(
            @Parameter(description = "Employee user id", example = "3") @PathVariable Long userId) {
        return leaveRequestService.findForUser(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get leave request by id")
    public LeaveRequest one(@Parameter(example = "10") @PathVariable Long id) {
        return leaveRequestService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Submit leave request")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Created leave",
            content = @Content(examples = @ExampleObject(value = OpenApiExamples.LEAVE_REQUEST))))
    public LeaveRequest request(@RequestBody LeaveRequest leaveRequest) {
        return leaveRequestService.request(leaveRequest);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve leave request", description = "Manager or HR only")
    public LeaveRequest approve(@PathVariable Long id) {
        return leaveRequestService.approve(id);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject leave request")
    public LeaveRequest reject(@PathVariable Long id) {
        return leaveRequestService.reject(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete leave request")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leaveRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
