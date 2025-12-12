package com.example.productapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "products")
@Schema(name = "Product", description = "Modelo de producto para comparación de artículos")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del producto", example = "1")
    private Long id;

    @NotBlank
    @Column(nullable = false)
    @Schema(description = "Nombre del producto", example = "Samsung Galaxy S21", required = true)
    private String name;

    @Column(length = 500)
    @Schema(description = "URL de la imagen del producto", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Descripción detallada del producto", example = "Smartphone con pantalla AMOLED de 6.2 pulgadas")
    private String description;

    @Min(0)
    @Column(nullable = false)
    @Schema(description = "Precio del producto", example = "799.99", required = true)
    private double price;

    @Schema(description = "Calificación del producto (0-5)", example = "4.5")
    private double rating;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Schema(description = "Especificaciones del producto en formato JSON", example = "{\"RAM\":\"8GB\",\"Storage\":\"256GB\"}")
    private Map<String, String> specifications;

    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    public Product() {}

    public Product(String name, String imageUrl, String description, double price, double rating, Map<String,String> specifications) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.price = price;
        this.rating = rating;
        this.specifications = specifications;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public Map<String, String> getSpecifications() { return specifications; }
    public void setSpecifications(Map<String, String> specifications) { this.specifications = specifications; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}