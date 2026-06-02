package tn.iteam.backend.dto;

public record SessionResponse(
        Long userId,
        String username,
        String fullName,
        String role
) {}
