package com.example.productapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Component for creating and validating JWT tokens.
 * Reads API_TOKEN_SECRET from environment or application.yml
 */
@Component
public class JwtUtil {

    @Value("${API_TOKEN_SECRET:a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2}")
    private String secret;

    private Key getSigningKey() {
        byte[] bytes = secret.getBytes();
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(String subject, long ttlSeconds) {
        Key key = getSigningKey();
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date exp = new Date(now + ttlSeconds * 1000L);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(exp)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        Key key = getSigningKey();
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
