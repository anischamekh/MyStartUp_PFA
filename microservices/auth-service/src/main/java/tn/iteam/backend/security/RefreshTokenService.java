package tn.iteam.backend.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.RefreshToken;
import tn.iteam.backend.repository.RefreshTokenRepository;

@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public void persist(Long userId, String rawRefreshToken) {
        refreshTokenRepository.revokeAllActiveForUser(userId);
        RefreshToken entity = new RefreshToken();
        entity.setUserId(userId);
        entity.setTokenHash(hash(rawRefreshToken));
        entity.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        entity.setRevoked(false);
        refreshTokenRepository.save(entity);
    }

    public boolean isActive(String rawRefreshToken) {
        return refreshTokenRepository
                .findByTokenHashAndRevokedFalse(hash(rawRefreshToken))
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }

    public void revoke(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHashAndRevokedFalse(hash(rawRefreshToken)).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpired() {
        refreshTokenRepository.deleteExpiredOrRevoked(Instant.now().minusSeconds(1));
    }

    private static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot hash refresh token", ex);
        }
    }
}
