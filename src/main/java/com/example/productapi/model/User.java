package com.example.productapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "users")
@Schema(name = "User", description = "Modelo de usuario para autenticación")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del usuario", example = "1")
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    @NotBlank
    @Schema(description = "Nombre de usuario único", example = "admin", required = true)
    private String username;

    @Column(nullable = false)
    @NotBlank
    @Schema(description = "Hash bcrypt de la contraseña (nunca se retorna en respuestas)", example = "$2a$10$...", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String passwordHash;

    @Column(nullable = false)
    @Schema(description = "Indica si el usuario está activo", example = "true")
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "Fecha y hora de creación del usuario", example = "2025-12-12T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Schema(description = "Fecha y hora de última actualización del usuario", example = "2025-12-12T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = createdAt;
        if (active == null) active = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
