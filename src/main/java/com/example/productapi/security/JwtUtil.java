package com.example.productapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

/**
 * Utility for creating and validating JWT tokens.
 */
public class JwtUtil {

    private static final String SECRET_ENV = "API_TOKEN_SECRET";

    private static Key getSigningKey() {
        String secret = System.getenv(SECRET_ENV);
        if (secret == null || secret.isEmpty()) {
            // fallback to a generated key (not for production)
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
        byte[] bytes = secret.getBytes();
        return Keys.hmacShaKeyFor(bytes);
    }

    public static String generateToken(String subject, long ttlSeconds) {
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

    public static Claims parseToken(String token) {
        Key key = getSigningKey();
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
