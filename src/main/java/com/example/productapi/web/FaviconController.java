package com.example.productapi.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FaviconController {

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        // No favicon asset; respond 204 to suppress errors
        return ResponseEntity.noContent().build();
    }
}
