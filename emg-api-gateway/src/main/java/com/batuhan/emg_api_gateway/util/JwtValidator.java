package com.batuhan.emg_api_gateway.util;

import com.batuhan.emg_api_gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtValidator {

    private final Key key;
    private final String secretKeyRaw;
    private static final Logger log = LoggerFactory.getLogger(JwtValidator.class);

    @Autowired
    public JwtValidator(JwtProperties jwtProperties) {
        this.secretKeyRaw = jwtProperties.getSecretKey();
        Key tempKey;

        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKeyRaw);
            tempKey = Keys.hmacShaKeyFor(keyBytes);
            log.info("DEBUG: JWT Secret Key successfully decoded. Decoded Length: {}", keyBytes.length);

        } catch (IllegalArgumentException e) {
            log.error("CRITICAL ERROR: Failed to decode JWT secret key from Base64. Check for illegal characters or spaces.", e);
            tempKey = null;
            throw new RuntimeException("JWT secret key decode error.", e);
        }
        this.key = tempKey;
    }

    @PostConstruct
    public void logKeyDetails() {
        log.info("DEBUG: Gateway JWT Secret Key (RAW): [{}]", this.secretKeyRaw);
        if (this.key == null) {
            log.error("DEBUG: Key is NULL, JWT validation will fail.");
        }
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
}