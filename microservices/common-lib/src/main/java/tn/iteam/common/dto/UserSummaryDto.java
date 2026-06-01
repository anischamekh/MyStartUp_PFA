package tn.iteam.common.dto;

public record UserSummaryDto(
        Long id,
        String username,
        String fullName,
        String email,
        String role,
        Long teamId,
        String teamName
) {}
