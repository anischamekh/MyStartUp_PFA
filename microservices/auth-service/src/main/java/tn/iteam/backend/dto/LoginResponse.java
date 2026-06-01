package tn.iteam.backend.dto;

public record LoginResponse(
        String token,
        String refreshToken,
        Long userId,
        String username,
        String fullName,
        String role
) {}
