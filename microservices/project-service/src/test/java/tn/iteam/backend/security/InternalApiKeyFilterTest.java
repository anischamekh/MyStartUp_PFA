package tn.iteam.backend.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class InternalApiKeyFilterTest {

    @Test
    void rejectsMissingApiKey() throws Exception {
        InternalApiKeyFilter filter = new InternalApiKeyFilter("secret-key");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/internal/users/1/has-active-tasks");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void allowsValidApiKey() throws Exception {
        InternalApiKeyFilter filter = new InternalApiKeyFilter("secret-key");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/internal/users/1/has-active-tasks");
        request.addHeader(InternalApiKeyFilter.HEADER_NAME, "secret-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);
        verify(chain).doFilter(request, response);
    }
}
