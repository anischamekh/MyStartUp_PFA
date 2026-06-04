package tn.iteam.backend.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import tn.iteam.common.security.JwtTokenResolver;
import tn.iteam.common.security.SharedJwtService;

class JwtAuthenticationFilterTest {

    private static final String SECRET =
            "change-this-secret-in-real-projects-change-this-secret-key-32";

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_setsAuthenticationForValidToken() throws Exception {
        SharedJwtService jwt = new SharedJwtService(SECRET, 3600_000L, 3600_000L);
        String token = jwt.generateAccessToken("user", Map.of("role", "MANAGER", "userId", 3L));

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET, 3600_000L, 3600_000L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_skipsWhenNoToken() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET, 3600_000L, 3600_000L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_acceptsCookieToken() throws Exception {
        SharedJwtService jwt = new SharedJwtService(SECRET, 3600_000L, 3600_000L);
        String token = jwt.generateAccessToken("cookie-user", Map.of("role", "HR", "userId", 1L));

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET, 3600_000L, 3600_000L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie(JwtTokenResolver.ACCESS_TOKEN_COOKIE, token));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }
}
