package tn.iteam.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tn.iteam.common.security.JwtTokenResolver;
import tn.iteam.common.security.SharedJwtService;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    private final SharedJwtService jwtService;

    public JwtGatewayFilter(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs,
            @Value("${app.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs
    ) {
        this.jwtService = new SharedJwtService(secret, expirationMs, refreshExpirationMs);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(exchange.getRequest());
        if (token == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        if (!jwtService.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String username = jwtService.extractUsername(token);
        String role = jwtService.extractRole(token);
        Long userId = jwtService.extractUserId(token);

        ServerHttpRequest.Builder builder = exchange.getRequest().mutate()
                .header("X-User-Name", username == null ? "" : username)
                .header("X-User-Role", role == null ? "" : role)
                .header("X-User-Id", userId == null ? "" : String.valueOf(userId));

        if (exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION) == null) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    private String resolveToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        HttpCookie access = cookies.getFirst(JwtTokenResolver.ACCESS_TOKEN_COOKIE);
        if (access != null && access.getValue() != null && !access.getValue().isBlank()) {
            return access.getValue();
        }
        return null;
    }

    private boolean isPublic(String path) {
        return path.startsWith("/api/auth/")
                || path.contains("/swagger-ui")
                || path.contains("/api-docs")
                || path.startsWith("/actuator");
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
