package tn.iteam.backend.controller;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.dto.CreateUserRequest;
import tn.iteam.backend.dto.UpdateUserRequest;
import tn.iteam.backend.dto.UserResponse;
import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.repository.TeamRepository;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.security.SecurityUtil;
import tn.iteam.backend.mapper.UserSummaryMapper;
import tn.iteam.backend.service.UserService;
import tn.iteam.backend.service.impl.CurrentUserProvider;
import tn.iteam.common.dto.UserSummaryDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User and employee profile management")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final TeamRepository teamRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    public UserController(
            UserService userService,
            UserRepository userRepository,
            CurrentUserProvider currentUserProvider,
            TeamRepository teamRepository,
            EmployeeProfileRepository employeeProfileRepository
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.teamRepository = teamRepository;
        this.employeeProfileRepository = employeeProfileRepository;
    }

    @GetMapping
    @Operation(summary = "List users")
    @ApiResponse(responseCode = "200", description = "Users")
    public List<User> all() {
        return userService.findAll();
    }

    /** Team leaders: members of the team they lead (for task assignment UI). */
    @GetMapping("/team-members")
    @Operation(summary = "Team members for leader", description = "Used by task assignment UI")
    @ApiResponse(responseCode = "200", description = "Team member users")
    public List<User> teamMembers() {
        User me = currentUserProvider.requireCurrentUser();
        if (me.getRole() == null || me.getRole().getName() != RoleName.TEAM_LEADER) {
            throw new BusinessException("Only TEAM_LEADER can load team members");
        }
        Team team = teamRepository
                .findByTeamLeader_Id(me.getId())
                .orElseThrow(() -> new BusinessException("You are not assigned as leader of any team"));
        return employeeProfileRepository.findByTeam_Id(team.getId()).stream()
                .map(EmployeeProfile::getUser)
                .filter(u -> u != null)
                .collect(Collectors.toList());
    }

    @GetMapping("/me")
    @Operation(summary = "Current authenticated user")
    @ApiResponse(responseCode = "200", description = "Current user")
    public User me() {
        String username = SecurityUtil.currentUsername();
        return userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("Current user not found"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    @ApiResponse(responseCode = "200", description = "User")
    public User one(@Parameter(example = "1") @PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "User summary for inter-service calls")
    @ApiResponse(responseCode = "200", description = "Compact user DTO")
    public UserSummaryDto summary(@Parameter(example = "1") @PathVariable Long id) {
        User user = userService.findById(id);
        EmployeeProfile profile = employeeProfileRepository.findByUserId(id).orElse(null);
        return UserSummaryMapper.toDto(user, profile);
    }

    @GetMapping("/summaries")
    @Operation(summary = "All user summaries")
    @ApiResponse(responseCode = "200", description = "Summary list")
    public List<UserSummaryDto> summaries() {
        return userService.findAll().stream()
                .map(u -> UserSummaryMapper.toDto(
                        u,
                        employeeProfileRepository.findByUserId(u.getId()).orElse(null)))
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Password policy enforced")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Created user"), @ApiResponse(responseCode = "400", description = "Validation error")})
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    @ApiResponse(responseCode = "200", description = "Updated user")
    public UserResponse update(@Parameter(example = "1") @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Publishes Kafka UserDeletedEvent for HRM/Project cleanup")
    @ApiResponse(responseCode = "204", description = "Deleted")
    public ResponseEntity<Void> delete(@Parameter(example = "1") @PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
