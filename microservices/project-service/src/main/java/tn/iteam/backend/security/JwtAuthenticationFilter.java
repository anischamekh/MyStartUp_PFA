package tn.iteam.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tn.iteam.common.security.JwtTokenResolver;
import tn.iteam.common.security.JwtUserPrincipal;
import tn.iteam.common.security.SharedJwtService;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SharedJwtService jwtService;

    public JwtAuthenticationFilter(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs
    ) {
        this.jwtService = new SharedJwtService(secret, expirationMs, refreshExpirationMs);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = JwtTokenResolver.resolveAccessToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        JwtUserPrincipal principal = new JwtUserPrincipal(
                jwtService.extractUserId(token),
                jwtService.extractUsername(token),
                jwtService.extractRole(token),
                jwtService.extractUsername(token)
        );
        var auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.role() == null ? List.of() : List.of(new SimpleGrantedAuthority(principal.role()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}
