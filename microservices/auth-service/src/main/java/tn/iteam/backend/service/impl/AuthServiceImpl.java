package tn.iteam.backend.service.impl;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.dto.LoginResponse;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.security.PasswordPolicyValidator;
import tn.iteam.backend.security.RefreshTokenService;
import tn.iteam.backend.service.AuthService;
import tn.iteam.common.security.SharedJwtService;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final SharedJwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordPolicyValidator passwordPolicyValidator;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs,
            UserRepository userRepository,
            RefreshTokenService refreshTokenService,
            PasswordPolicyValidator passwordPolicyValidator
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = new SharedJwtService(secret, expirationMs, refreshExpirationMs);
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    @Override
    public LoginResponse login(String username, String password) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        UserDetails principal = (UserDetails) auth.getPrincipal();
        User user = userRepository.findByUsername(username).orElseThrow();
        return buildLoginResponse(principal, user);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken) || !refreshTokenService.isActive(refreshToken)) {
            throw new BusinessException("Invalid or expired refresh token");
        }
        refreshTokenService.revoke(refreshToken);
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username).orElseThrow();
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().getName().name())
                .build();
        return buildLoginResponse(principal, user);
    }

    private LoginResponse buildLoginResponse(UserDetails principal, User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().getName().name());
        claims.put("userId", user.getId());
        claims.put("tokenType", "access");

        Map<String, Object> refreshClaims = new HashMap<>(claims);
        refreshClaims.put("tokenType", "refresh");

        String access = jwtService.generateAccessToken(principal.getUsername(), claims);
        String refresh = jwtService.generateRefreshToken(principal.getUsername(), refreshClaims);
        refreshTokenService.persist(user.getId(), refresh);

        return new LoginResponse(
                access,
                refresh,
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().getName().name()
        );
    }
}
