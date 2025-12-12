package com.example.productapi;

import com.example.productapi.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para JwtUtil
 * Valida la generación y validación de tokens JWT
 */
@DisplayName("JwtUtil Tests")
public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testUsername = "testuser";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Configurar secret key usando reflexión
        ReflectionTestUtils.setField(jwtUtil, "secret", "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2");
    }

    // ============ Token Generation ============

    @Test
    @DisplayName("Debe generar token válido")
    public void testGenerateValidToken() {
        // Act
        String token = jwtUtil.generateToken(testUsername, 86400);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    @DisplayName("Debe generar diferentes tokens para diferentes usuarios")
    public void testGenerateDifferentTokensForDifferentUsers() {
        // Act
        String token1 = jwtUtil.generateToken("user1", 86400);
        String token2 = jwtUtil.generateToken("user2", 86400);

        // Assert
        assertNotEquals(token1, token2);
    }

    // ============ Token Validation ============

    @Test
    @DisplayName("Debe validar token recién generado como válido")
    public void testValidateValidToken() {
        // Arrange
        String token = jwtUtil.generateToken(testUsername, 86400);

        // Act
        boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Debe rechazar token malformado")
    public void testValidateMalformedToken() {
        // Act
        boolean isValid = jwtUtil.validateToken("token.malformado.invalido");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Debe rechazar token vacío")
    public void testValidateEmptyToken() {
        // Act
        boolean isValid = jwtUtil.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Debe rechazar token null")
    public void testValidateNullToken() {
        // Act
        boolean isValid = jwtUtil.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    // ============ Token Parsing ============

    @Test
    @DisplayName("Debe extraer subject (username) del token")
    public void testParseToken() {
        // Arrange
        String token = jwtUtil.generateToken(testUsername, 86400);

        // Act
        Claims claims = jwtUtil.parseToken(token);

        // Assert
        assertNotNull(claims);
        assertEquals(testUsername, claims.getSubject());
    }

    @Test
    @DisplayName("Debe extraer issued date del token")
    public void testParseTokenIssuedDate() {
        // Arrange
        String token = jwtUtil.generateToken(testUsername, 86400);

        // Act
        Claims claims = jwtUtil.parseToken(token);

        // Assert
        assertNotNull(claims.getIssuedAt());
    }

    @Test
    @DisplayName("Debe extraer expiration del token")
    public void testParseTokenExpiration() {
        // Arrange
        String token = jwtUtil.generateToken(testUsername, 86400);

        // Act
        Claims claims = jwtUtil.parseToken(token);

        // Assert
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().getTime() > System.currentTimeMillis());
    }

    // ============ Token Structure ============

    @Test
    @DisplayName("Token debe tener 3 partes separadas por puntos")
    public void testTokenStructure() {
        // Arrange
        String token = jwtUtil.generateToken(testUsername, 86400);

        // Act
        String[] parts = token.split("\\.");

        // Assert
        assertEquals(3, parts.length, "JWT debe tener 3 partes: header, payload, signature");
    }
}
