package tn.iteam.common.security;

public record JwtUserPrincipal(Long userId, String username, String role, String fullName) {}
