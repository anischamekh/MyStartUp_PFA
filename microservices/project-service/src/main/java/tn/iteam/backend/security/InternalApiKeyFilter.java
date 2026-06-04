package tn.iteam.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Internal-Api-Key";

    private final String expectedKey;

    public InternalApiKeyFilter(@Value("${app.internal-api-key:}") String expectedKey) {
        this.expectedKey = expectedKey == null ? "" : expectedKey.trim();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (expectedKey.isEmpty() || !expectedKey.equals(request.getHeader(HEADER_NAME))) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid internal API key");
            return;
        }
        chain.doFilter(request, response);
    }
}
