package tn.iteam.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

class JwtServiceTest {

    private static final String SECRET =
            "change-this-secret-in-real-projects-change-this-secret-key-32";

    private final JwtService jwtService = new JwtService(SECRET, 3600_000L);

    @Test
    void generateAndValidateToken() {
        var user = User.withUsername("alice").password("x").roles("HR").build();
        String token = jwtService.generateToken(user, Map.of("role", "HR", "userId", 1L));

        assertEquals("alice", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_rejectsWrongUser() {
        var user = User.withUsername("alice").password("x").roles("HR").build();
        var other = User.withUsername("bob").password("x").roles("HR").build();
        String token = jwtService.generateToken(user, Map.of());

        assertFalse(jwtService.isTokenValid(token, other));
    }
}
