package com.example.productapi;

import com.example.productapi.controller.ProductController;
import com.example.productapi.model.Product;
import com.example.productapi.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductControllerTest {

    @Test
    public void testListReturnsProducts() {
        ProductService svc = mock(ProductService.class);
        when(svc.listAll()).thenReturn(List.of(new Product("A","A","descr",10.0,4.0, Map.of())));
        ProductController ctrl = new ProductController(svc);

        List<Product> result = ctrl.list();
        assertEquals(1, result.size());
        verify(svc).listAll();
    }

    @Test
    public void testGetNotFound() {
        ProductService svc = mock(ProductService.class);
        when(svc.getById(1L)).thenReturn(Optional.empty());
        ProductController ctrl = new ProductController(svc);

        ResponseEntity<?> res = ctrl.get(1L);
        assertEquals(404, res.getStatusCodeValue());
    }
}
