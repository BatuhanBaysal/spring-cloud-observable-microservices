package com.batuhan.emg_api_gateway.filter;

import com.batuhan.emg_api_gateway.util.JwtValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component("AuthHeaderFilter")
@Slf4j
public class AuthHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthHeaderGatewayFilterFactory.Config> {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtValidator jwtValidator;

    public AuthHeaderGatewayFilterFactory(JwtValidator jwtValidator) {
        super(Config.class);
        this.jwtValidator = jwtValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(AUTHORIZATION_HEADER)) {
                log.warn("Authorization header missing.");
                return this.onError(exchange, "Authorization header missing", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                log.warn("Invalid or missing Bearer token prefix.");
                return this.onError(exchange, "Invalid or missing Bearer token prefix", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(BEARER_PREFIX.length());
            if (!jwtValidator.validateToken(token)) {
                log.warn("JWT validation failed for token: {}", token);
                return this.onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
            }

            try {
                String username = jwtValidator.extractUsername(token);
                String authorities = jwtValidator.extractAuthorities(token);

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-Auth-User", username)
                        .header("X-Auth-Authorities", authorities)
                        .build();

                log.info("Successfully authenticated user: {} and added headers.", username);

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                log.error("Error processing JWT claims: {}", e.getMessage());
                return this.onError(exchange, "JWT processing error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        String responseBody = String.format("{\"error\": \"%s\", \"status\": %d}", err, httpStatus.value());
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(responseBody.getBytes()))
        );
    }

    public static class Config {}
}