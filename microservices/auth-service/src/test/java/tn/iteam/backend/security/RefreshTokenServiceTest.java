package tn.iteam.backend.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.RefreshToken;
import tn.iteam.backend.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void isActive_returnsTrueForValidToken() {
        RefreshTokenService service = new RefreshTokenService(refreshTokenRepository, 604800000L);
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(token));

        assertTrue(service.isActive("raw-token-value"));
    }

    @Test
    void isActive_returnsFalseWhenMissing() {
        RefreshTokenService service = new RefreshTokenService(refreshTokenRepository, 604800000L);
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.empty());
        assertFalse(service.isActive("missing"));
    }
}
