package tn.iteam.gateway.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tn.iteam.common.security.JwtTokenResolver;
import tn.iteam.common.security.SharedJwtService;

class JwtGatewayFilterTest {

    private static final String SECRET =
            "change-this-secret-in-real-projects-change-this-secret-key-32";

    private final JwtGatewayFilter filter = new JwtGatewayFilter(SECRET, 3600_000L, 604800_000L);

    @Test
    void filter_allowsOptionsWithoutToken() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.options("/api/users").build());
        org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(
                org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void filter_allowsPublicAuthPath() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/auth/login").build());
        org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(
                org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void filter_returnsUnauthorizedWhenTokenMissing() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users").build());

        filter.filter(exchange, mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class)).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_returnsUnauthorizedWhenTokenInvalid() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-valid")
                        .build());

        filter.filter(exchange, mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class)).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_forwardsWithUserHeadersForValidBearer() {
        SharedJwtService jwt = new SharedJwtService(SECRET, 3600_000L, 604800_000L);
        String token = jwt.generateAccessToken("dana", Map.of("role", "HR", "userId", 4L));

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/leaves")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build());
        org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(
                org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
        when(chain.filter(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void filter_acceptsAccessTokenCookie() {
        SharedJwtService jwt = new SharedJwtService(SECRET, 3600_000L, 604800_000L);
        String token = jwt.generateAccessToken("erin", Map.of("role", "EMPLOYEE", "userId", 2L));

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/tasks/mine")
                        .cookie(new HttpCookie(JwtTokenResolver.ACCESS_TOKEN_COOKIE, token))
                        .build());
        org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(
                org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
        when(chain.filter(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getOrder_isEarly() {
        assertEquals(-100, filter.getOrder());
    }
}
