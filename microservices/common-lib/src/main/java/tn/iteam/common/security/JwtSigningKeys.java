package tn.iteam.common.security;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

/**
 * Centralizes HMAC key creation and enforces a minimum 256-bit secret length (JJWT / Sonar).
 */
public final class JwtSigningKeys {

    public static final int MIN_SECRET_BYTES = 32;

    private JwtSigningKeys() {}

    public static SecretKey hmacShaKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT secret is missing. Set environment variable JWT_SECRET to at least "
                            + MIN_SECRET_BYTES + " random bytes (256 bits).");
        }
        byte[] bytes = secret.trim().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException(
                    "JWT secret is too short (" + bytes.length + " bytes). "
                            + "Use at least " + MIN_SECRET_BYTES + " bytes (256 bits). "
                            + "Update Kubernetes secret mystartup-secrets / JWT_SECRET.");
        }
        try {
            return Keys.hmacShaKeyFor(bytes);
        } catch (WeakKeyException ex) {
            throw new IllegalArgumentException(
                    "JWT secret is not strong enough for HS256. Use at least "
                            + MIN_SECRET_BYTES + " bytes of random data in JWT_SECRET.",
                    ex);
        }
    }
}
