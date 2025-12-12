package com.example.productapi.controller;

import com.example.productapi.exception.SuccessResponse;
import com.example.productapi.security.JwtUtil;
import com.example.productapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * Auth controller to issue JWT tokens validating against database.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints para autenticación y generación de tokens JWT")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    @PostMapping("/token")
    @Operation(summary = "Generar token JWT", description = "Autentica un usuario contra la base de datos y retorna un token JWT válido")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token generado exitosamente", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "400", description = "Datos requeridos faltantes")
    })
    public ResponseEntity<SuccessResponse> token(@RequestBody Map<String, Object> body) {
        String user = (String) body.get("username");
        String pass = (String) body.get("password");
        Integer ttl = body.get("ttlSeconds") instanceof Number ? ((Number) body.get("ttlSeconds")).intValue() : 900;

        log.info("🔐 AuthController: Petición de token para usuario: {}", user);

        // Validar contra base de datos
        boolean valid = authService.validateCredentials(user, pass);
        log.info("🔐 AuthController: Resultado validación BD: {}", valid ? "VÁLIDO" : "INVÁLIDO");
        
        if (!valid) {
            return ResponseEntity.status(401).body(new SuccessResponse(401, "Unauthorized", "UNAUTHORIZED", "/api/auth/token"));
        }

        String token = jwtUtil.generateToken(user, ttl);
        long expiresAt = Instant.now().getEpochSecond() + ttl;

        return ResponseEntity.ok(new SuccessResponse(200, "Token issued", "SUCCESS", "/api/auth/token", Map.of("token", token, "expiresAt", expiresAt)));
    }
}
