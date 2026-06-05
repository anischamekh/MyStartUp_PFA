package tn.iteam.common.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JwtSigningKeysTest {

    @Test
    void hmacShaKey_accepts256BitSecret() {
        String secret = "change-this-secret-in-real-projects-change-this-secret-key-32";
        assertNotNull(JwtSigningKeys.hmacShaKey(secret));
    }

    @Test
    void hmacShaKey_rejectsShortSecret() {
        assertThrows(IllegalArgumentException.class, () -> JwtSigningKeys.hmacShaKey("replace-me"));
    }

    @Test
    void hmacShaKey_rejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> JwtSigningKeys.hmacShaKey("  "));
    }

    @Test
    void hmacShaKey_rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> JwtSigningKeys.hmacShaKey(null));
    }

    @Test
    void hmacShaKey_acceptsBase64ProductionSecret() {
        String secret = "9f3niTAR1XzzLrHfP/zTRT4dkU+Xp4ReMhdi1+xPYl8RK41jGRxhrc03uN3HC3tkzZPi5Wp9LtNQgxZaWr4+vA==";
        assertNotNull(JwtSigningKeys.hmacShaKey(secret));
    }
}
