package tn.iteam.backend.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.RefreshToken;
import tn.iteam.backend.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(refreshTokenRepository, 604800000L);
    }

    @Test
    void isActive_returnsTrueForValidToken() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(token));

        assertTrue(service.isActive("raw-token-value"));
    }

    @Test
    void isActive_returnsFalseWhenMissing() {
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.empty());
        assertFalse(service.isActive("missing"));
    }

    @Test
    void isActive_returnsFalseWhenExpired() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(Instant.now().minusSeconds(60));
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(token));
        assertFalse(service.isActive("expired-token"));
    }

    @Test
    void persist_revokesPreviousAndSavesNew() {
        service.persist(42L, "refresh-raw");

        verify(refreshTokenRepository).revokeAllActiveForUser(42L);
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();
        assertTrue(saved.getUserId().equals(42L));
        assertFalse(saved.isRevoked());
        assertTrue(saved.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void revoke_marksTokenRevoked() {
        RefreshToken token = new RefreshToken();
        token.setRevoked(false);
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(token));

        service.revoke("token-to-revoke");

        assertTrue(token.isRevoked());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void cleanupExpired_deletesStaleTokens() {
        service.cleanupExpired();
        verify(refreshTokenRepository).deleteExpiredOrRevoked(any(Instant.class));
    }
}
