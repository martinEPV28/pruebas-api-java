package com.example.productapi;

import com.example.productapi.controller.ProductController;
import com.example.productapi.model.Product;
import com.example.productapi.service.ProductService;
import com.example.productapi.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProductController
 * Valida la lógica de negocio de cada endpoint sin levantar servidor HTTP
 */
@DisplayName("Product Controller Tests")
public class ProductControllerTest {

    private ProductService productService;
    private ProductController productController;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        productController = new ProductController(productService);
    }

    // ============ GET /api/products ============

    @Test
    @DisplayName("GET /api/products - Debe retornar lista de productos")
    public void testListReturnsProducts() {
        // Arrange
        Product product1 = new Product("Laptop", "https://img.com/1", "Gaming laptop", 1599.99, 4.8, Map.of("ram", "16GB"));
        Product product2 = new Product("Mouse", "https://img.com/2", "Wireless mouse", 29.99, 4.5, Map.of("dpi", "3200"));
        when(productService.listAll()).thenReturn(Arrays.asList(product1, product2));

        // Act
        List<Product> result = productController.list();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals("Mouse", result.get(1).getName());
        verify(productService, times(1)).listAll();
    }

    @Test
    @DisplayName("GET /api/products - Lista vacía si no hay productos")
    public void testListReturnsEmptyWhenNoProducts() {
        // Arrange
        when(productService.listAll()).thenReturn(List.of());

        // Act
        List<Product> result = productController.list();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productService).listAll();
    }

    // ============ GET /api/products/{id} ============

    @Test
    @DisplayName("GET /api/products/{id} - Debe retornar producto cuando existe")
    public void testGetReturnsProductWhenExists() {
        // Arrange
        Long productId = 1L;
        Product product = new Product("Laptop", "url", "desc", 1599.99, 4.8, Map.of());
        product.setId(productId);
        when(productService.getById(productId)).thenReturn(Optional.of(product));

        // Act
        ResponseEntity<Product> response = productController.get(productId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Laptop", response.getBody().getName());
        verify(productService).getById(productId);
    }

    @Test
    @DisplayName("GET /api/products/{id} - Debe lanzar ResourceNotFoundException cuando no existe")
    public void testGetThrowsExceptionWhenNotFound() {
        // Arrange
        Long productId = 999L;
        when(productService.getById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productController.get(productId));
        verify(productService).getById(productId);
    }

    // ============ POST /api/products ============

    @Test
    @DisplayName("POST /api/products - Debe crear producto exitosamente")
    public void testCreateProductSuccessfully() {
        // Arrange
        Product newProduct = new Product("New Laptop", "url", "desc", 1999.99, 4.9, Map.of());
        Product savedProduct = new Product("New Laptop", "url", "desc", 1999.99, 4.9, Map.of());
        savedProduct.setId(1L);
        when(productService.create(any(Product.class))).thenReturn(savedProduct);

        // Act
        ResponseEntity<Product> response = productController.create(newProduct);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("New Laptop", response.getBody().getName());
        assertEquals(1L, response.getBody().getId());
        verify(productService).create(any(Product.class));
    }

    // ============ PUT /api/products/{id} ============

    @Test
    @DisplayName("PUT /api/products/{id} - Debe actualizar producto exitosamente")
    public void testUpdateProductSuccessfully() {
        // Arrange
        Long productId = 1L;
        Product updatedProduct = new Product("Updated Laptop", "url", "desc", 1799.99, 5.0, Map.of());
        when(productService.update(eq(productId), any(Product.class))).thenReturn(updatedProduct);

        // Act
        ResponseEntity<Product> response = productController.update(productId, updatedProduct);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Laptop", response.getBody().getName());
        verify(productService).update(eq(productId), any(Product.class));
    }

    // ============ DELETE /api/products/{id} ============

    @Test
    @DisplayName("DELETE /api/products/{id} - Debe eliminar producto exitosamente")
    public void testDeleteProductSuccessfully() {
        // Arrange
        Long productId = 1L;
        doNothing().when(productService).delete(productId);

        // Act
        var response = productController.delete(productId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("deleted successfully"));
        verify(productService).delete(productId);
    }
}
