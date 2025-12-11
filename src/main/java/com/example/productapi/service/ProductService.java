package com.example.productapi.service;

import com.example.productapi.model.Product;
import com.example.productapi.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<Product> listAll() {
        return repo.findAll();
    }

    public Optional<Product> getById(String id) {
        return repo.findById(id);
    }

    public Product create(Product product) {
        return repo.save(product);
    }

    public void delete(String id) {
        repo.delete(id);
    }
}
