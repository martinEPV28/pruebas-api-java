package com.example.productapi.service;

import com.example.productapi.model.User;
import com.example.productapi.repository.FileUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final FileUserRepository fileUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(FileUserRepository fileUserRepository) {
        this.fileUserRepository = fileUserRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Validates credentials against users.json file.
     * Passwords are stored as BCrypt hashes.
     */
    public boolean validateCredentials(String username, String password) {
        log.info("🔍 AuthService.validateCredentials() for user: {}", username);
        
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            log.warn("❌ Username or password is empty");
            return false;
        }
        
        Optional<User> userOpt = fileUserRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("❌ User not found: {}", username);
            return false;
        }
        
        User user = userOpt.get();
        String storedHash = user.getPassword();
        if (!StringUtils.hasText(storedHash)) {
            log.warn("❌ Password hash is empty for user: {}", username);
            return false;
        }
        
        log.debug("✓ User found. Password hash prefix: {}...", storedHash.substring(0, Math.min(20, storedHash.length())));

        // Validate BCrypt hash
        try {
            boolean match = passwordEncoder.matches(password, storedHash);
            if (match) {
                log.info("✅ Password valid for user: {}", username);
            } else {
                log.warn("❌ Invalid password for user: {}", username);
            }
            return match;
        } catch (Throwable t) {
            log.error("❌ Error validating BCrypt password", t);
            return false;
        }
    }
}
