package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.iteam.backend.client.ProjectServiceClient;
import tn.iteam.backend.dto.CreateUserRequest;
import tn.iteam.backend.dto.UpdateUserRequest;
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

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private EmployeeProfileRepository employeeProfileRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private tn.iteam.backend.service.email.EmailService emailService;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private UserEventPublisher userEventPublisher;
    @Mock
    private PasswordPolicyValidator passwordPolicyValidator;
    @Mock
    private ProjectServiceClient projectServiceClient;

    @InjectMocks
    private UserServiceImpl userService;

    private User hrUser;
    private Role employeeRole;

    @BeforeEach
    void setUp() {
        hrUser = new User();
        hrUser.setId(99L);
        Role hrRole = new Role();
        hrRole.setName(RoleName.HR);
        hrUser.setRole(hrRole);

        employeeRole = new Role();
        employeeRole.setId(2L);
        employeeRole.setName(RoleName.EMPLOYEE);
    }

    @Test
    void findById_returnsUser() {
        User user = new User();
        user.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        assertEquals(2L, userService.findById(2L).getId());
    }

    @Test
    void findById_missing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> userService.findById(1L));
    }

    @Test
    void create_hrUser_persistsEmployee() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(hrUser);
        doNothing().when(passwordPolicyValidator).validate(any());

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("new.user");
        request.setPassword("Password1!");
        request.setFullName("New User");
        request.setEmail("new@test.com");
        request.setRole(RoleName.EMPLOYEE);
        request.setTeamId(5L);

        when(userRepository.existsByUsername("new.user")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        Team team = new Team();
        team.setId(5L);
        when(teamRepository.findById(5L)).thenReturn(Optional.of(team));
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded");

        User saved = new User();
        saved.setId(10L);
        saved.setUsername("new.user");
        saved.setFullName("New User");
        saved.setEmail("new@test.com");
        saved.setRole(employeeRole);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        EmployeeProfile profile = new EmployeeProfile();
        when(employeeProfileRepository.save(any(EmployeeProfile.class))).thenReturn(profile);

        assertEquals("new.user", userService.create(request).getUsername());
        verify(userEventPublisher).publishUserUpdated(any(), any(), org.mockito.ArgumentMatchers.eq("CREATED"));
    }

    @Test
    void create_rejectsNonHrOrAdmin() {
        User employee = new User();
        Role role = new Role();
        role.setName(RoleName.EMPLOYEE);
        employee.setRole(role);
        when(currentUserProvider.requireCurrentUser()).thenReturn(employee);

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("x");
        request.setPassword("Password1!");
        request.setFullName("X");
        request.setEmail("x@test.com");
        request.setRole(RoleName.EMPLOYEE);

        assertThrows(BusinessException.class, () -> userService.create(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_duplicateUsername() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(hrUser);
        doNothing().when(passwordPolicyValidator).validate(any());

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("dup");
        request.setPassword("Password1!");
        request.setFullName("Dup");
        request.setEmail("dup@test.com");
        request.setRole(RoleName.EMPLOYEE);
        when(userRepository.existsByUsername("dup")).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.create(request));
    }

    @Test
    void update_hrUser_savesChanges() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(hrUser);

        User existing = new User();
        existing.setId(3L);
        existing.setUsername("old");
        existing.setFullName("Old");
        existing.setEmail("old@test.com");
        when(userRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(roleRepository.findByName(RoleName.EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(userRepository.save(existing)).thenReturn(existing);
        when(employeeProfileRepository.findByUserId(3L)).thenReturn(Optional.empty());
        when(employeeProfileRepository.save(any(EmployeeProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("newname");
        request.setFullName("New Name");
        request.setEmail("new@test.com");
        request.setRole(RoleName.EMPLOYEE);

        assertEquals("newname", userService.update(3L, request).getUsername());
    }

    @Test
    void delete_hrUser_removesUser() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(hrUser);

        User user = new User();
        user.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(projectServiceClient.hasActiveTasks(7L)).thenReturn(false);
        when(teamRepository.findAllByTeamLeader_Id(7L)).thenReturn(List.of());

        userService.delete(7L);

        verify(userRepository).delete(user);
        verify(userEventPublisher).publishUserDeleted(7L);
    }

    @Test
    void delete_rejectsWhenActiveTasks() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(hrUser);
        User user = new User();
        user.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(projectServiceClient.hasActiveTasks(7L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.delete(7L));
    }
}
