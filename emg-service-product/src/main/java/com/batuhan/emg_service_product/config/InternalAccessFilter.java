package com.batuhan.emg_service_product.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class InternalAccessFilter extends OncePerRequestFilter {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";
    private static final String AUTH_USER_HEADER = "X-Auth-User";
    private static final String AUTH_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Value("${service.internal.secret}")
    private String internalSecret;

    @Value("${application.security.jwt.secret-key}")
    private String jwtSecretKey;

    private Key signInKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String incomingSecret = request.getHeader(INTERNAL_SECRET_HEADER);
        if (incomingSecret == null || !incomingSecret.equals(internalSecret)) {
            sendErrorResponse(response, "Access denied. Request did not originate from Gateway or provided wrong internal key.", HttpStatus.FORBIDDEN);
            return;
        }

        final String authHeader = request.getHeader(AUTH_HEADER);
        Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
        String username = request.getHeader(AUTH_USER_HEADER);

        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            final String jwt = authHeader.substring(TOKEN_PREFIX.length());

            try {
                if (signInKey == null) {
                    byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
                    signInKey = Keys.hmacShaKeyFor(keyBytes);
                }

                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(signInKey)
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();

                String authoritiesString = (String) claims.get("authorities");

                if (authoritiesString != null) {
                    authorities = Arrays.stream(authoritiesString.split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                }
                username = claims.getSubject();

            } catch (Exception e) {
                logger.warn("An error occurred while verifying the JWT: " + e.getMessage());
            }
        }

        if (username != null && !username.isEmpty()) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    authorities
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}