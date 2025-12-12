package com.example.productapi;

import com.example.productapi.controller.AuthController;
import com.example.productapi.exception.SuccessResponse;
import com.example.productapi.security.JwtUtil;
import com.example.productapi.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuthController
 * Valida la autenticación y generación de tokens JWT
 */
@DisplayName("Auth Controller Tests")
public class AuthControllerTest {

    private AuthController authController;
    private AuthService authService;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        jwtUtil = mock(JwtUtil.class);
        authController = new AuthController();
        // Inyectar mocks usando reflexión (porque @Autowired no funciona en tests sin Spring Context)
        ReflectionTestUtils.setField(authController, "authService", authService);
        ReflectionTestUtils.setField(authController, "jwtUtil", jwtUtil);
    }

    @Test
    @DisplayName("POST /api/auth/token - Debe generar token con credenciales válidas")
    public void testGenerateTokenWithValidCredentials() {
        // Arrange
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("username", "admin");
        credentials.put("password", "newpass1234");
        when(authService.validateCredentials("admin", "newpass1234")).thenReturn(true);
        when(jwtUtil.generateToken("admin", 900)).thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");

        // Act
        ResponseEntity<SuccessResponse> response = authController.token(credentials);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Token issued"));
        verify(authService).validateCredentials("admin", "newpass1234");
        verify(jwtUtil).generateToken("admin", 900);
    }

    @Test
    @DisplayName("POST /api/auth/token - Debe rechazar credenciales inválidas (401)")
    public void testGenerateTokenWithInvalidCredentials() {
        // Arrange
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("username", "admin");
        credentials.put("password", "wrongpassword");
        when(authService.validateCredentials("admin", "wrongpassword")).thenReturn(false);

        // Act
        ResponseEntity<SuccessResponse> response = authController.token(credentials);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(authService).validateCredentials("admin", "wrongpassword");
        verify(jwtUtil, never()).generateToken(anyString(), anyLong());
    }

    @Test
    @DisplayName("POST /api/auth/token - Debe rechazar username faltante (401)")
    public void testGenerateTokenWithMissingUsername() {
        // Arrange
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("password", "newpass1234");
        when(authService.validateCredentials(null, "newpass1234")).thenReturn(false);

        // Act
        ResponseEntity<SuccessResponse> response = authController.token(credentials);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(authService).validateCredentials(null, "newpass1234");
    }

    @Test
    @DisplayName("POST /api/auth/token - Debe rechazar password faltante (401)")
    public void testGenerateTokenWithMissingPassword() {
        // Arrange
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("username", "admin");
        when(authService.validateCredentials("admin", null)).thenReturn(false);

        // Act
        ResponseEntity<SuccessResponse> response = authController.token(credentials);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(authService).validateCredentials("admin", null);
    }
}
