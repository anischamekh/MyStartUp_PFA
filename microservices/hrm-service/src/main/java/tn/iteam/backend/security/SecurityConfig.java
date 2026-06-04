package tn.iteam.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import tn.iteam.common.security.ApiCorsProperties;
import tn.iteam.common.security.ApiSecuritySupport;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiCorsProperties corsProperties;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ApiCorsProperties corsProperties) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsProperties = corsProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        ApiSecuritySupport.configureApiCsrf(http);
        ApiSecuritySupport.configureSecurityHeaders(http);
        ApiSecuritySupport.applyCors(http, corsProperties.isEnabled());
        http
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/actuator/**").permitAll()

                        .requestMatchers(HttpMethod.PUT, "/api/notifications/**")
                                .hasAnyAuthority("EMPLOYEE", "TEAM_LEADER", "MANAGER", "HR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/notifications/**")
                                .hasAnyAuthority("EMPLOYEE", "TEAM_LEADER", "MANAGER", "HR", "ADMIN")

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
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return ApiSecuritySupport.corsConfigurationSource(corsProperties);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
