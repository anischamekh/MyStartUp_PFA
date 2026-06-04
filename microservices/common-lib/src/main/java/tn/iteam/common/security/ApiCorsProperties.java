package tn.iteam.common.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public class ApiCorsProperties {

    private boolean enabled = true;
    private List<String> allowedOrigins = List.of("http://localhost:4200", "http://127.0.0.1:4200");
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    private List<String> allowedHeaders = List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "X-XSRF-TOKEN",
            "X-Internal-Api-Key"
    );

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public void setAllowedOrigins(String commaSeparated) {
        if (commaSeparated == null || commaSeparated.isBlank()) {
            this.allowedOrigins = List.of();
            return;
        }
        this.allowedOrigins = Arrays.stream(commaSeparated.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }
}
