package com.batuhan.emg_api_gateway.filter;

import com.batuhan.emg_api_gateway.util.JwtValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class AuthorizationFilter implements GlobalFilter, Ordered {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String X_AUTH_USER = "X-Auth-User";
    private static final String X_AUTH_AUTHORITIES = "X-Auth-Authorities";

    private final JwtValidator jwtValidator;

    private static final List<String> PUBLIC_URLS = Arrays.asList(
            "/api/account/login",
            "/api/account/register"
    );

    public AuthorizationFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        if (shouldSkipFilter(path, method)) {
            return chain.filter(exchange);
        }

        if (!request.getHeaders().containsKey(AUTHORIZATION_HEADER)) {
            log.warn("Authorization header missing for path: {}", path);
            return this.onError(exchange, "Authorization header missing", HttpStatus.UNAUTHORIZED);
        }

        final String authHeader = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
        String token;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            log.warn("Invalid Authorization header format.");
            return this.onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
        }

        if (!jwtValidator.validateToken(token)) {
            log.warn("Invalid or expired JWT token for path: {}", path);
            return this.onError(exchange, "Invalid or expired JWT token", HttpStatus.FORBIDDEN);
        }

        try {
            String username = jwtValidator.extractUsername(token);
            String authorities = jwtValidator.extractAuthorities(token);

            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header(X_AUTH_USER, username)
                    .header(X_AUTH_AUTHORITIES, authorities)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("Error processing JWT: {}", e.getMessage());
            return this.onError(exchange, "Error processing JWT", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean shouldSkipFilter(String path, String method) {
        if (PUBLIC_URLS.contains(path)) {
            return true;
        }

        if (path.startsWith("/api/product/v1/products") && method.equalsIgnoreCase(HttpMethod.GET.name())) {
            return true;
        }

        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error("Gateway Auth Error: {} - {}", httpStatus, err);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}