package tn.iteam.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tn.iteam.backend.entity.AuditLog;
import tn.iteam.backend.repository.AuditLogRepository;

@Component
public class AuditLoggingFilter extends OncePerRequestFilter {

    private final AuditLogRepository auditLogRepository;

    public AuditLoggingFilter(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);
        if (!request.getRequestURI().startsWith("/api/")) {
            return;
        }
        AuditLog log = new AuditLog();
        log.setUsername(request.getHeader("X-User-Name"));
        log.setAction(request.getMethod());
        log.setEndpoint(request.getRequestURI());
        log.setResult(response.getStatus() < 400 ? "SUCCESS" : "FAILURE");
        auditLogRepository.save(log);
    }
}
