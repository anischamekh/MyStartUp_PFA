package tn.iteam.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    private static final String SECRET =
            "change-this-secret-in-real-projects-change-this-secret-key-32";

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3600_000L);
        userDetails = User.withUsername("alice")
                .password("pwd")
                .roles("EMPLOYEE")
                .build();
    }

    @Test
    void generateToken_andExtractUsername() {
        String token = jwtService.generateToken(userDetails, Map.of("role", "EMPLOYEE"));
        assertEquals("alice", jwtService.extractUsername(token));
    }

    @Test
    void isTokenValid_returnsTrueForFreshToken() {
        String token = jwtService.generateToken(userDetails, Map.of());
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_returnsFalseForWrongUser() {
        String token = jwtService.generateToken(userDetails, Map.of());
        UserDetails other = User.withUsername("bob").password("p").roles("EMPLOYEE").build();
        assertFalse(jwtService.isTokenValid(token, other));
    }

    @Test
    void isTokenValid_returnsFalseForExpiredToken() {
        JwtService shortLived = new JwtService(SECRET, -1L);
        String token = shortLived.generateToken(userDetails, Map.of());
        assertFalse(shortLived.isTokenValid(token, userDetails));
    }
}
