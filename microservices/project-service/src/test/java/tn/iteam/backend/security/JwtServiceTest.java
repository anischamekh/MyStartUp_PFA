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
        var user = User.withUsername("bob").password("x").roles("EMPLOYEE").build();
        String token = jwtService.generateToken(user, Map.of("role", "EMPLOYEE", "userId", 2L));
        assertEquals("bob", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_rejectsWrongUser() {
        var user = User.withUsername("bob").password("x").roles("EMPLOYEE").build();
        var other = User.withUsername("other").password("x").roles("EMPLOYEE").build();
        String token = jwtService.generateToken(user, Map.of());
        assertFalse(jwtService.isTokenValid(token, other));
    }
}
