package com.example.productapi.service;

import com.example.productapi.model.Product;
import com.example.productapi.repository.FileProductRepository;
import com.example.productapi.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final FileProductRepository repo;

    public ProductService(FileProductRepository repo) {
        this.repo = repo;
    }

    @Cacheable(value = "products", key = "'all'")
    public List<Product> listAll() {
        return repo.findAll();
    }

    @Cacheable(value = "products", key = "#id")
    public Optional<Product> getById(Long id) {
        return repo.findById(id);
    }

    @CachePut(value = "products", key = "#result.id")
    @CacheEvict(value = "products", key = "'all'")
    public Product create(Product product) {
        return repo.save(product);
    }

    @CachePut(value = "products", key = "#id")
    @CacheEvict(value = "products", key = "'all'")
    public Product update(Long id, Product productDetails) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (productDetails.getName() != null) {
            product.setName(productDetails.getName());
        }
        if (productDetails.getImageUrl() != null) {
            product.setImageUrl(productDetails.getImageUrl());
        }
        if (productDetails.getDescription() != null) {
            product.setDescription(productDetails.getDescription());
        }
        if (productDetails.getPrice() > 0) {
            product.setPrice(productDetails.getPrice());
        }
        if (productDetails.getRating() >= 0) {
            product.setRating(productDetails.getRating());
        }
        if (productDetails.getSpecifications() != null) {
            product.setSpecifications(productDetails.getSpecifications());
        }

        return repo.save(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        repo.deleteById(id);
    }
}
