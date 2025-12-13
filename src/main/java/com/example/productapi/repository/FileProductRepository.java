package com.example.productapi.repository;

import com.example.productapi.model.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class FileProductRepository {
    
    @Value("${product.storage.file:data/products.json}")
    private String storageFilePath;
    
    private final ObjectMapper objectMapper;
    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public FileProductRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @PostConstruct
    public void init() {
        loadFromFile();
    }
    
    private void loadFromFile() {
        File file = new File(storageFilePath);
        
        // Create parent directory if it doesn't exist
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        if (file.exists() && file.length() > 0) {
            try {
                List<Product> productList = objectMapper.readValue(file, new TypeReference<List<Product>>() {});
                products.clear();
                
                long maxId = 0;
                for (Product product : productList) {
                    products.put(product.getId(), product);
                    if (product.getId() > maxId) {
                        maxId = product.getId();
                    }
                }
                idGenerator.set(maxId + 1);
                
                System.out.println("Loaded " + products.size() + " products from file: " + storageFilePath);
            } catch (IOException e) {
                System.err.println("Error loading products from file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No existing data file found. Starting with empty repository.");
            saveToFile(); // Create empty file
        }
    }
    
    private synchronized void saveToFile() {
        try {
            File file = new File(storageFilePath);
            List<Product> productList = new ArrayList<>(products.values());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, productList);
        } catch (IOException e) {
            System.err.println("Error saving products to file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }
    
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }
    
    public Product save(Product product) {
        if (product.getId() == null) {
            // New product
            product.setId(idGenerator.getAndIncrement());
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
        } else {
            // Update existing
            product.setUpdatedAt(LocalDateTime.now());
        }
        
        products.put(product.getId(), product);
        saveToFile();
        return product;
    }
    
    public boolean existsById(Long id) {
        return products.containsKey(id);
    }
    
    public void deleteById(Long id) {
        products.remove(id);
        saveToFile();
    }
    
    public long count() {
        return products.size();
    }
    
    public void deleteAll() {
        products.clear();
        saveToFile();
    }
}
