package tn.iteam.common.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Shared Spring Security helpers (explicit CORS, CSRF for cookie-backed JWT APIs).
 */
public final class ApiSecuritySupport {

    private ApiSecuritySupport() {}

    /**
     * CSRF enabled with cookie token; authentication and actuator/swagger paths exempt.
     */
    public static void configureApiCsrf(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieName("XSRF-TOKEN");
        repository.setHeaderName("X-XSRF-TOKEN");
        repository.setCookiePath("/");
        http.csrf(csrf -> csrf
                .csrfTokenRepository(repository)
                .ignoringRequestMatchers(
                        "/api/auth/**",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**"
                ));
    }

    public static void applyCors(HttpSecurity http, boolean corsEnabled) throws Exception {
        if (corsEnabled) {
            http.cors(cors -> {});
        } else {
            http.cors(AbstractHttpConfigurer::disable);
        }
    }

    public static CorsConfigurationSource corsConfigurationSource(ApiCorsProperties properties) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(properties.getAllowedOrigins());
        cfg.setAllowedMethods(properties.getAllowedMethods());
        cfg.setAllowedHeaders(properties.getAllowedHeaders());
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
