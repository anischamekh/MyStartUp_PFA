package tn.iteam.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tn.iteam.backend.entity.Role;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_notFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("ghost"));
    }

    @Test
    void loadUserByUsername_usesRoleAuthority() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("hash");
        Role role = new Role();
        role.setName(RoleName.HR);
        user.setRole(role);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        var details = userDetailsService.loadUserByUsername("alice");

        assertEquals("alice", details.getUsername());
        assertEquals("HR", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_defaultsToEmployeeWhenRoleMissing() {
        User user = new User();
        user.setUsername("bob");
        user.setPassword("hash");
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        var details = userDetailsService.loadUserByUsername("bob");

        assertEquals("EMPLOYEE", details.getAuthorities().iterator().next().getAuthority());
    }
}
