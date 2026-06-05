package tn.iteam.backend.security;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tn.iteam.backend.entity.AuditLog;
import tn.iteam.backend.repository.AuditLogRepository;

@ExtendWith(MockitoExtension.class)
class AuditLoggingFilterTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLoggingFilter filter;

    @Test
    void doFilterInternal_savesAuditLogForApiPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        request.addHeader("X-User-Name", "hr.user");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(auditLogRepository).save(argThat((AuditLog audit) ->
                "hr.user".equals(audit.getUsername())
                        && "GET".equals(audit.getAction())
                        && "/api/users".equals(audit.getEndpoint())
                        && "SUCCESS".equals(audit.getResult())));
    }

    @Test
    void doFilterInternal_skipsNonApiPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(auditLogRepository);
    }

    @Test
    void doFilterInternal_usesAnonymousWhenHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/teams/1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(204);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(auditLogRepository).save(argThat((AuditLog audit) ->
                audit.getUsername() == null && "DELETE".equals(audit.getAction())));
    }

    @Test
    void doFilterInternal_recordsFailureFor4xx() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(401);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(auditLogRepository).save(argThat((AuditLog audit) -> "FAILURE".equals(audit.getResult())));
    }
}
