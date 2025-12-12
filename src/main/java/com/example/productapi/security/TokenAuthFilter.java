package com.example.productapi.security;

import com.example.productapi.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Simple token-based authentication filter.
 * Expects header: Authorization: Bearer <token>
 * Token value is read from environment variable API_TOKEN.
 */
@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip auth endpoints
        if (path != null && path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Apply only to API endpoints
        if (path != null && path.startsWith("/api/")) {
            String auth = request.getHeader("Authorization");
            String token = null;
            if (auth != null && auth.startsWith("Bearer ")) {
                token = auth.substring(7);
            }

            if (token == null || !com.example.productapi.security.JwtUtil.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");

                ErrorResponse err = new ErrorResponse(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized",
                        "UNAUTHORIZED",
                        path
                );

                mapper.writeValue(response.getWriter(), err);
                return;
            }
            // token is valid; optionally set attribute with subject
            try {
                var claims = com.example.productapi.security.JwtUtil.parseToken(token);
                request.setAttribute("jwtSubject", claims.getSubject());
            } catch (Exception ex) {
                // ignore - token was validated earlier
            }
        }

        filterChain.doFilter(request, response);
    }
}
