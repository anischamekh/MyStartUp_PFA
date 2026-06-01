package tn.iteam.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuditLoggingFilter auditLoggingFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuditLoggingFilter auditLoggingFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.auditLoggingFilter = auditLoggingFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${app.cors.enabled:true}") boolean corsEnabled
    ) throws Exception {
        http.csrf(csrf -> csrf.disable());
        if (corsEnabled) {
            http.cors(cors -> {});
        } else {
            http.cors(AbstractHttpConfigurer::disable);
        }
        http
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()
                        // Own notifications: mark read / delete (all roles including ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/notifications/**")
                                .hasAnyAuthority("EMPLOYEE", "TEAM_LEADER", "MANAGER", "HR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/notifications/**")
                                .hasAnyAuthority("EMPLOYEE", "TEAM_LEADER", "MANAGER", "HR", "ADMIN")
                        // ADMIN is read-only: domain mutations for non-admin roles only
                        .requestMatchers(HttpMethod.POST, "/api/**")
                                .hasAnyAuthority("EMPLOYEE", "TEAM_LEADER", "MANAGER", "HR")
                        .requestMatchers(HttpMethod.PUT, "/api/**")
                                .hasAnyAuthority("EMPLOYEE", "TEAM_LEADER", "MANAGER", "HR")
                        .requestMatchers(HttpMethod.PATCH, "/api/**")
                                .hasAnyAuthority("EMPLOYEE", "TEAM_LEADER", "MANAGER", "HR")
                        .requestMatchers(HttpMethod.DELETE, "/api/**")
                                .hasAnyAuthority("EMPLOYEE", "TEAM_LEADER", "MANAGER", "HR")
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(auditLoggingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    @ConditionalOnProperty(name = "app.cors.enabled", havingValue = "true", matchIfMissing = true)
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(java.util.List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200"
        ));
        cfg.addAllowedHeader("*");
        cfg.addAllowedMethod("*");
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}

