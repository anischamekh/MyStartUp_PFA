package tn.iteam.backend.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tn.iteam.common.security.SharedJwtService;

class JwtAuthenticationFilterTest {

    private static final String SECRET =
            "change-this-secret-in-real-projects-change-this-secret-key-32";

    @Test
    void doFilterInternal_setsAuthenticationForValidBearerToken() throws Exception {
        SharedJwtService jwtService = new SharedJwtService(SECRET, 3600_000L, 3600_000L);
        String token = jwtService.generateAccessToken("alice", java.util.Map.of("role", "HR", "userId", 1L));

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET, 3600_000L, 3600_000L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);
        verify(chain).doFilter(request, response);
    }
}
