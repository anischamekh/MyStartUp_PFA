package tn.iteam.backend.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.client.ProjectServiceClient;
import tn.iteam.backend.dto.CreateUserRequest;
import tn.iteam.backend.dto.EmployeeProfilePayload;
import tn.iteam.backend.dto.UpdateUserRequest;
import tn.iteam.backend.dto.UserResponse;
import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.Role;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.messaging.UserEventPublisher;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.repository.RoleRepository;
import tn.iteam.backend.repository.TeamRepository;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.security.PasswordPolicyValidator;
import tn.iteam.backend.service.UserService;
import tn.iteam.backend.service.email.EmailService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CurrentUserProvider currentUserProvider;
    private final UserEventPublisher userEventPublisher;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final ProjectServiceClient projectServiceClient;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmployeeProfileRepository employeeProfileRepository,
            TeamRepository teamRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            CurrentUserProvider currentUserProvider,
            UserEventPublisher userEventPublisher,
            PasswordPolicyValidator passwordPolicyValidator,
            ProjectServiceClient projectServiceClient
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.currentUserProvider = currentUserProvider;
        this.userEventPublisher = userEventPublisher;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.projectServiceClient = projectServiceClient;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new BusinessException("User not found"));
    }

    @Override
    public UserResponse create(CreateUserRequest request) {
        requireHrOrAdmin();
        passwordPolicyValidator.validate(request.getPassword());
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        User creator = currentUserProvider.requireCurrentUser();
        boolean hrFlow = creator.getRole() != null && creator.getRole().getName() == RoleName.HR;

        RoleName roleName = request.getRole() != null ? request.getRole() : RoleName.EMPLOYEE;
        Role role = roleRepository
                .findByName(roleName)
                .orElseThrow(() -> new BusinessException("Role not found: " + roleName));

        Team team = null;
        if (roleName != RoleName.ADMIN) {
            Long teamId = request.resolveTeamId();
            if (hrFlow && teamId == null) {
                throw new BusinessException("Please select a team");
            }
            if (teamId != null) {
                team = teamRepository.findById(teamId).orElseThrow(() -> new BusinessException("Team not found"));
            }
        }

        String rawPassword = request.getPassword();
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);

        User saved = userRepository.save(user);

        EmployeeProfile profile = null;
        if (roleName != RoleName.ADMIN) {
            profile = new EmployeeProfile();
            profile.setUser(saved);
            profile.setRemainingLeaveDays(30);
            if (request.getEmployeeProfile() != null) {
                request.getEmployeeProfile().applyTo(profile);
            }
            profile.setTeam(team);
            profile = employeeProfileRepository.save(profile);
            saved.setEmployeeProfile(profile);
        }

        sendWelcomeEmailIfHr(creator, saved, rawPassword);
        userEventPublisher.publishUserUpdated(saved, profile, "CREATED");
        return UserResponse.from(saved, profile);
    }

    @Override
    public UserResponse update(Long id, UpdateUserRequest request) {
        requireHrOrAdmin();
        User existing = findById(id);
        existing.setFullName(request.getFullName().trim());
        existing.setEmail(request.getEmail().trim());
        existing.setUsername(request.getUsername().trim());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            passwordPolicyValidator.validate(request.getPassword());
            existing.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        RoleName roleName = request.getRole();
        Role role = roleRepository
                .findByName(roleName)
                .orElseThrow(() -> new BusinessException("Role not found: " + roleName));
        existing.setRole(role);

        User saved = userRepository.save(existing);

        EmployeeProfile profile = null;
        if (roleName != RoleName.ADMIN) {
            profile = employeeProfileRepository
                    .findByUserId(saved.getId())
                    .orElseGet(() -> {
                        EmployeeProfile ep = new EmployeeProfile();
                        ep.setUser(saved);
                        ep.setRemainingLeaveDays(30);
                        return ep;
                    });

            EmployeeProfilePayload payload = request.getEmployeeProfile();
            if (payload != null) {
                payload.applyTo(profile);
            }

            Long teamId = request.resolveTeamId();
            if (teamId != null) {
                Team team = teamRepository.findById(teamId).orElseThrow(() -> new BusinessException("Team not found"));
                profile.setTeam(team);
            }

            profile = employeeProfileRepository.save(profile);
            saved.setEmployeeProfile(profile);
        } else {
            employeeProfileRepository.findByUserId(saved.getId()).ifPresent(employeeProfileRepository::delete);
        }

        userEventPublisher.publishUserUpdated(saved, profile, "UPDATED");
        return UserResponse.from(saved, profile);
    }

    @Override
    public void delete(Long id) {
        requireHrOrAdmin();
        User user = findById(id);
        Long uid = user.getId();

        if (projectServiceClient.hasActiveTasks(uid)) {
            throw new BusinessException("Cannot delete user because he is assigned to active tasks");
        }

        for (Team team : teamRepository.findAllByTeamLeader_Id(uid)) {
            team.setTeamLeader(null);
            teamRepository.save(team);
        }

        employeeProfileRepository.findByUserId(uid).ifPresent(employeeProfileRepository::delete);
        userRepository.delete(user);
        userEventPublisher.publishUserDeleted(uid);
    }

    private void requireHrOrAdmin() {
        User creator = currentUserProvider.requireCurrentUser();
        if (creator.getRole() == null) {
            throw new BusinessException("Not allowed");
        }
        RoleName name = creator.getRole().getName();
        if (name != RoleName.HR && name != RoleName.ADMIN) {
            throw new BusinessException("Only HR or ADMIN can manage users");
        }
    }

    private void sendWelcomeEmailIfHr(User creator, User saved, String rawPassword) {
        try {
            if (creator.getRole() != null && creator.getRole().getName() == RoleName.HR) {
                String subject = "Welcome to the Company";
                String body = ""
                        + "Hello,\n\n"
                        + "Your account has been created successfully.\n\n"
                        + "Username: " + saved.getUsername() + "\n"
                        + "Use the password provided by HR during onboarding.\n\n"
                        + "You can login here:\n"
                        + "http://localhost:4200/login\n\n"
                        + "Welcome to the team!\n";
                emailService.sendEmail(saved.getEmail(), subject, body);
            }
        } catch (Exception e) {
            log.error("Welcome email failed for createdUserId={} createdUserEmail={}", saved.getId(), saved.getEmail(), e);
        }
    }
}
