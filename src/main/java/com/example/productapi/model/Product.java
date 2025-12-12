package com.example.productapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Min(0)
    @Column(nullable = false)
    private double price;

    private double rating;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> specifications;

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
}