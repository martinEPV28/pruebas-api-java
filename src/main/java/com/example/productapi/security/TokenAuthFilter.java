package com.example.productapi.security;

import com.example.productapi.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Token-based authentication filter.
 * Expects header: Authorization: Bearer <token>
 * Validates JWT tokens using JwtUtil component.
 */
@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

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

        // Skip Swagger UI and API docs
        if (path != null && (path.startsWith("/swagger-ui") || 
                    path.startsWith("/api-docs") ||
                    path.startsWith("/v3/api-docs") ||
                    path.startsWith("/api/docs") ||
                            path.contains("swagger") ||
                            path.contains("favicon") ||
                            path.startsWith("/webjars") ||
                            path.equals("/"))) {
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

            if (token == null || !jwtUtil.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");

                ErrorResponse err = new ErrorResponse(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized",
                        "UNAUTHORIZED",
                        path
                );

                mapper.writeValue(response.getWriter(), err);
                response.getWriter().flush();
                return; // <-- MUST RETURN HERE to prevent calling filterChain.doFilter()
            }
            // token is valid; optionally set attribute with subject
            try {
                var claims = jwtUtil.parseToken(token);
                request.setAttribute("jwtSubject", claims.getSubject());
            } catch (Exception ex) {
                // ignore - token was validated earlier
            }
        }

        filterChain.doFilter(request, response);
    }
}
