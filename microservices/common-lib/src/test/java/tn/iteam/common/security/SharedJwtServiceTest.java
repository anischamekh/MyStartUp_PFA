package tn.iteam.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SharedJwtServiceTest {

    private static final String SECRET =
            "change-this-secret-in-real-projects-change-this-secret-key-32";

    private SharedJwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new SharedJwtService(SECRET, 60_000L, 120_000L);
    }

    @Test
    void generateAccessToken_containsClaims() {
        String token = jwtService.generateAccessToken("alice", Map.of("role", "ADMIN", "userId", 42L));

        assertNotNull(token);
        assertEquals("alice", jwtService.extractUsername(token));
        assertEquals("ADMIN", jwtService.extractRole(token));
        assertEquals(42L, jwtService.extractUserId(token));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_rejectsGarbage() {
        assertFalse(jwtService.isTokenValid("not-a-jwt"));
    }

    @Test
    void generateRefreshToken_isValid() {
        String refresh = jwtService.generateRefreshToken("bob", Map.of("role", "EMPLOYEE"));
        assertTrue(jwtService.isTokenValid(refresh));
        assertEquals("bob", jwtService.extractUsername(refresh));
    }
}
