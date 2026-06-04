package tn.iteam.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class ApiSecuritySupportTest {

    @Test
    void corsConfigurationSource_usesExplicitOriginsAndHeaders() {
        ApiCorsProperties props = new ApiCorsProperties();
        props.setAllowedOrigins(List.of("http://localhost:4200"));
        props.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        CorsConfigurationSource source = ApiSecuritySupport.corsConfigurationSource(props);
        CorsConfiguration cfg = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/users"));

        assertEquals(List.of("http://localhost:4200"), cfg.getAllowedOrigins());
        assertEquals(List.of("Authorization", "Content-Type"), cfg.getAllowedHeaders());
        assertFalse(cfg.getAllowedHeaders().contains("*"));
        assertTrue(cfg.getAllowCredentials());
    }
}
