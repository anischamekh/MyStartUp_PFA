package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import tn.iteam.backend.dto.LoginResponse;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.entity.Role;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.security.PasswordPolicyValidator;
import tn.iteam.backend.security.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private PasswordPolicyValidator passwordPolicyValidator;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                authenticationManager,
                "change-this-secret-in-real-projects-change-this-secret",
                86400000L,
                604800000L,
                userRepository,
                refreshTokenService,
                passwordPolicyValidator
        );
    }

    @Test
    void login_returnsTokens() {
        UserDetails principal = User.withUsername("john")
                .password("pwd")
                .authorities(new SimpleGrantedAuthority("EMPLOYEE"))
                .build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        tn.iteam.backend.entity.User entity = new tn.iteam.backend.entity.User();
        entity.setId(1L);
        entity.setUsername("john");
        entity.setFullName("John Doe");
        Role role = new Role();
        role.setName(RoleName.EMPLOYEE);
        entity.setRole(role);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(entity));

        LoginResponse response = authService.login("john", "pwd");

        assertNotNull(response.token());
        assertNotNull(response.refreshToken());
        assertEquals("john", response.username());
        assertEquals("EMPLOYEE", response.role());
    }

    @Test
    void refresh_withInvalidToken_throws() {
        assertThrows(BusinessException.class, () -> authService.refresh("not-a-valid-jwt"));
    }

    @Test
    void refresh_withValidActiveToken_returnsNewTokens() {
        tn.iteam.backend.entity.User entity = new tn.iteam.backend.entity.User();
        entity.setId(1L);
        entity.setUsername("john");
        entity.setPassword("encoded");
        Role role = new Role();
        role.setName(RoleName.EMPLOYEE);
        entity.setRole(role);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(entity));

        UserDetails principal = User.withUsername("john")
                .password("pwd")
                .authorities(new SimpleGrantedAuthority("EMPLOYEE"))
                .build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        LoginResponse login = authService.login("john", "pwd");
        when(refreshTokenService.isActive(login.refreshToken())).thenReturn(true);

        LoginResponse refreshed = authService.refresh(login.refreshToken());

        assertNotNull(refreshed.token());
        assertNotNull(refreshed.refreshToken());
        assertEquals("john", refreshed.username());
    }
}
