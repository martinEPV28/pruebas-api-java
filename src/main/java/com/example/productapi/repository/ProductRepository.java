package com.example.productapi.repository;

import com.example.productapi.model.Product;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.*;

@Repository
public class ProductRepository {
    private final Map<String, Product> store = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        // seed sample data
        Product p1 = new Product("1","Silla Ergonomica","https://example.com/silla.jpg","Silla con soporte lumbar",199.99,4.5, Map.of("color","negro","peso","12kg"));
        Product p2 = new Product("2","Teclado Mecánico","https://example.com/teclado.jpg","Teclado con switches azules",79.99,4.2, Map.of("layout","es","conectividad","USB"));
        store.put(p1.getId(), p1);
        store.put(p2.getId(), p2);
    }

    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public Product save(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(UUID.randomUUID().toString());
        }
        store.put(product.getId(), product);
        return product;
    }

    public void delete(String id) {
        store.remove(id);
    }
}
