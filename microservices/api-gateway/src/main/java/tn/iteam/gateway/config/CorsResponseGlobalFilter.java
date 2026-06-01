package tn.iteam.gateway.config;

import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Ensures CORS headers are present on every gateway response (including 401/429/503),
 * so the browser does not report a misleading CORS failure when the root cause is elsewhere.
 */
@Component
public class CorsResponseGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:4200",
            "http://127.0.0.1:4200"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> applyCorsHeaders(exchange)));
    }

    private void applyCorsHeaders(ServerWebExchange exchange) {
        String origin = exchange.getRequest().getHeaders().getOrigin();
        if (origin == null || !ALLOWED_ORIGINS.contains(origin)) {
            return;
        }

        HttpHeaders headers = exchange.getResponse().getHeaders();
        if (!headers.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)) {
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())
                && exchange.getResponse().getStatusCode() == null) {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
        }
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER + 1;
    }
}
