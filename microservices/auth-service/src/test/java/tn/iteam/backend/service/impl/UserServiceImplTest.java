package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.iteam.backend.client.ProjectServiceClient;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.messaging.UserEventPublisher;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.repository.RoleRepository;
import tn.iteam.backend.repository.TeamRepository;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.security.PasswordPolicyValidator;
import tn.iteam.backend.service.email.EmailService;

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
    private EmailService emailService;
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
}
