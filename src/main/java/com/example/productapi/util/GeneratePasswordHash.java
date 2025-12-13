package com.example.productapi.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate hashes
        String adminHash = encoder.encode("password");
        String userHash = encoder.encode("123456");
        
        System.out.println("=== GENERATED HASHES ===");
        System.out.println("\nAdmin (password):");
        System.out.println(adminHash);
        
        System.out.println("\nUser (123456):");
        System.out.println(userHash);
        
        // Verify they work
        System.out.println("\n=== VERIFICATION ===");
        System.out.println("Admin verify: " + encoder.matches("password", adminHash));
        System.out.println("User verify: " + encoder.matches("123456", userHash));
        
        // JSON format
        System.out.println("\n=== JSON FORMAT ===");
        System.out.println("[");
        System.out.println("  {");
        System.out.println("    \"username\": \"admin\",");
        System.out.println("    \"password\": \"" + adminHash + "\",");
        System.out.println("    \"roles\": [\"ADMIN\", \"USER\"]");
        System.out.println("  },");
        System.out.println("  {");
        System.out.println("    \"username\": \"user\",");
        System.out.println("    \"password\": \"" + userHash + "\",");
        System.out.println("    \"roles\": [\"USER\"]");
        System.out.println("  }");
        System.out.println("]");
    }
}
