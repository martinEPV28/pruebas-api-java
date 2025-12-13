package com.example.productapi.repository;

import com.example.productapi.model.Product;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface adapter for ProductRepository to maintain compatibility with existing code
 */
@Repository
public interface ProductRepository {
    List<Product> findAll();
    Optional<Product> findById(Long id);
    Product save(Product product);
    boolean existsById(Long id);
    void deleteById(Long id);
}