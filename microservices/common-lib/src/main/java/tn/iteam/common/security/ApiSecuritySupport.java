package tn.iteam.common.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Shared Spring Security helpers (explicit CORS, CSRF for cookie-backed JWT APIs).
 */
public final class ApiSecuritySupport {

    /**
     * POST login/refresh/logout run before any XSRF-TOKEN cookie exists (JWT bootstrap).
     * CSRF stays enabled for all other state-changing requests.
     */
    private static final RequestMatcher CSRF_BOOTSTRAP_AUTH = new OrRequestMatcher(
            new AntPathRequestMatcher("/api/auth/login", HttpMethod.POST.name()),
            new AntPathRequestMatcher("/api/auth/refresh", HttpMethod.POST.name()),
            new AntPathRequestMatcher("/api/auth/logout", HttpMethod.POST.name())
    );

    private ApiSecuritySupport() {}

    /**
     * CSRF enabled with {@link CookieCsrfTokenRepository}; only bootstrap auth POSTs are exempt.
     */
    public static void configureApiCsrf(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieName("XSRF-TOKEN");
        repository.setHeaderName("X-XSRF-TOKEN");
        repository.setCookiePath("/");
        http.csrf(csrf -> csrf
                .csrfTokenRepository(repository)
                .requireCsrfProtectionMatcher(new AndRequestMatcher(
                        CsrfFilter.DEFAULT_CSRF_MATCHER,
                        new NegatedRequestMatcher(CSRF_BOOTSTRAP_AUTH)
                )));
    }

    /** XSS / clickjacking mitigations for servlet services. */
    public static void configureSecurityHeaders(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentTypeOptions(withDefaults())
                .xssProtection(withDefaults())
                .referrerPolicy(referrer -> referrer.policy(
                        org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
        );
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
