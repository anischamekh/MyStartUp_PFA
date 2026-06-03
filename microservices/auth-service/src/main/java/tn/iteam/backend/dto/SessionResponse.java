package tn.iteam.backend.dto;

/**
 * User profile returned to the SPA. {@code accessToken} is for in-memory use only (never localStorage);
 * refresh remains in HttpOnly cookies.
 */
public record SessionResponse(
        Long userId,
        String username,
        String fullName,
        String role,
        String accessToken
) {}
