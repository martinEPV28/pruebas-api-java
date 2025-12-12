package com.example.productapi.controller;

import com.example.productapi.exception.SuccessResponse;
import com.example.productapi.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * Simple auth controller to issue short-lived JWT tokens.
 * Expects environment variables AUTH_USER and AUTH_PASS for basic validation.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/token")
    public ResponseEntity<SuccessResponse> token(@RequestBody Map<String, Object> body) {
        String user = (String) body.get("username");
        String pass = (String) body.get("password");
        Integer ttl = body.get("ttlSeconds") instanceof Number ? ((Number) body.get("ttlSeconds")).intValue() : 900;

        String expectedUser = System.getenv("AUTH_USER");
        String expectedPass = System.getenv("AUTH_PASS");

        if (expectedUser == null || expectedPass == null || !expectedUser.equals(user) || !expectedPass.equals(pass)) {
            return ResponseEntity.status(401).body(new SuccessResponse(401, "Unauthorized", "UNAUTHORIZED", "/api/auth/token"));
        }

        String token = JwtUtil.generateToken(user, ttl);
        long expiresAt = Instant.now().getEpochSecond() + ttl;

        return ResponseEntity.ok(new SuccessResponse(200, "Token issued", "SUCCESS", "/api/auth/token", Map.of("token", token, "expiresAt", expiresAt)));
    }
}
